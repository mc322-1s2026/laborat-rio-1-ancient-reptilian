package com.nexus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.TaskStatus;
import com.nexus.model.User;

public class Workspace {
    private final List<Task> tasks = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();

    /**
     * Funções auxiliares para manipulação de usuarios
     */
    public void addUser(User user, List<User> users) {
        users.add(user);
    }

    public User getUserByName(String name, List<User> users) {
        for (User u : users) {
            if (u.consultUsername().equals(name)) {
                return u;
            }
        }
        throw new NexusValidationException("Usuário com nome '" + name + "' não encontrado.");
    }

    public List<String> topPerformUsers(List<User> users) {
        // Retorna os 3 usuários com mais tarefas concluídas
        return users.stream()
            .sorted((u1, u2) -> Integer.compare(doneTasksByOwner(u2).size(), doneTasksByOwner(u1).size()))
            .limit(3)
            .map(User::consultUsername)
            .toList();
    }

    public List<String> overloadedUsers(List<User> users) {
        // Retorna usuários com mais de 5 tarefas IN_PROGRESS
        return users.stream()
            .filter(u -> u.calculateWorkload(tasks) > 10)
            .map(User::consultUsername)
            .toList();
    }


    /**
     * Funções auxiliares para manipulação de tarefas
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        // Retorna uma visão não modificável para garantir encapsulamento
        return Collections.unmodifiableList(tasks);
    }

    public Task getTaskById(int id) {
        for (Task t : tasks) {
            if (t.getId() == id) {
                return t;
            }
        }
        throw new NexusValidationException("Tarefa com ID " + id + " não encontrada.");
    }

    public List<Task> doneTasksByOwner(User owner) {
        return tasks.stream()
            .filter(task -> task.getOwner().equals(owner) && task.getStatus() == TaskStatus.DONE)
            .toList();
    }

    public String globalBottleNeck() {
        int to_do = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.TO_DO).count();
        int in_progress = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS).count();
        int blocked = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.BLOCKED).count();

        int maior = Math.max(to_do, Math.max(in_progress, blocked));
        if (maior == 0) {
            return "Sem tarefas.";
        }
        
        List<String> bottlenecks = new ArrayList<>();
        if (to_do == maior) bottlenecks.add("TO_DO");
        if (in_progress == maior) bottlenecks.add("IN_PROGRESS");
        if (blocked == maior) bottlenecks.add("BLOCKED");

        if (bottlenecks.size() == 3) {
            return "TO_DO, IN_PROGRESS e BLOCKED empatados em ocorrências.";
        }
        
        return String.join(" e ", bottlenecks) + " com maiores ocorrências.";
    }


    /**
     * Funções auxiliares para manipulação de projetos
     */
    public void addProject(Project project) {
        projects.add(project);
    }

    public List<Project> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    public Project getProjectByName(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        throw new NexusValidationException("Projeto com nome '" + name + "' não encontrado.");
    }

    public String projectsHealth(List<Project> projects) {
        List<String> reports = projects.stream()
            .map(project -> {
                long totalTasks = project.viewTasks().size();
                long doneTasks = project.viewTasks().stream()
                    .mapToLong(task -> task.getStatus() == TaskStatus.DONE ? 1 : 0)
                    .sum();
                double percentage = totalTasks == 0 ? 0 : (doneTasks * 100.0) / totalTasks;
                return String.format("Projeto: %s - Conclusão: %.1f%% (%d/%d tarefas)",
                    project.getName(), percentage, doneTasks, totalTasks);
            })
            .collect(Collectors.toList());

        return String.join("\n", reports);
    }
    
}
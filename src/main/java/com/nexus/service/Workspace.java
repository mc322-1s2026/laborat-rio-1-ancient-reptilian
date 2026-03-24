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

/**
 * Representa o contêiner principal do sistema Nexus.
 *
 * Armazena e gerencia globalmente tarefas e projetos, oferecendo métodos para
 * manipulação, busca, filtragem e geração de relatórios analíticos usando a Stream API.
 * Garante encapsulamento e integridade dos dados, conforme as regras de negócio do Nexus.
 */
public class Workspace {
    private final List<Task> tasks = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();

    /**
     * Funções auxiliares para manipulação de usuarios
     */
    /**
     * Adiciona um usuário à lista global de usuários.
     *
     * @param user usuário a ser adicionado
     * @param users lista global de usuários
     */
    public void addUser(User user, List<User> users) {
        users.add(user);
    }

    /**
     * Busca um usuário pelo nome na lista global.
     *
     * @param name nome do usuário
     * @param users lista global de usuários
     * @return instância de User correspondente
     * @throws IllegalArgumentException se não encontrar o usuário
     */
    public User getUserByName(String name, List<User> users) {
        for (User u : users) {
            if (u.consultUsername().equals(name)) {
                return u;
            }
        }
        
        throw new IllegalArgumentException("Usuário com nome '" + name + "' não encontrado.");
    }

    /**
     * Retorna os 3 usuários com maior número de tarefas concluídas (status DONE).
     *
     * @param users lista global de usuários
     * @return lista de usernames dos top performers
     */
    public List<String> topPerformUsers(List<User> users) {
        // Retorna os 3 usuários com mais tarefas concluídas
        return users.stream()
            .sorted((u1, u2) -> Integer.compare(doneTasksByOwner(u2).size(), doneTasksByOwner(u1).size()))
            .limit(3)
            .map(User::consultUsername)
            .toList();
    }

    /**
     * Retorna os usuários cuja carga de trabalho IN_PROGRESS ultrapassa 10 tarefas.
     *
     * @param users lista global de usuários
     * @return lista de usernames sobrecarregados ou mensagem padrão se nenhum
     */
    public List<String> overloadedUsers(List<User> users) {
        // Retorna usuários com mais de 5 tarefas IN_PROGRESS
        List<String> overloaded = users.stream()
            .filter(u -> u.calculateWorkload(tasks) > 10)
            .map(User::consultUsername)
            .toList();
        if (overloaded.isEmpty()) {
            return List.of("Nenhum usuário sobrecarregado.");
        }
        return overloaded;
    }


    /**
     * Funções auxiliares para manipulação de tarefas
     */
    /**
     * Adiciona uma tarefa ao workspace.
     *
     * @param task tarefa a ser adicionada
     */
    public void addTask(Task task) {
        
        tasks.add(task);
    }

    /**
     * Retorna uma visão imutável da lista de tarefas do workspace.
     *
     * @return lista não modificável de tarefas
     */
    public List<Task> getTasks() {
        // Retorna uma visão não modificável para garantir encapsulamento
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Busca uma tarefa pelo ID.
     *
     * @param id identificador da tarefa
     * @return instância de Task correspondente
     * @throws IllegalArgumentException se não encontrar a tarefa
     */
    public Task getTaskById(int id) {
        for (Task t : tasks) {
            if (t.getId() == id) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tarefa com ID " + id + " não encontrada.");
    }

    /**
     * Retorna a lista de tarefas concluídas (DONE) de um usuário.
     *
     * @param owner usuário responsável
     * @return lista de tarefas concluídas deste usuário
     */
    public List<Task> doneTasksByOwner(User owner) {
        return tasks.stream()
            .filter(task -> task.getOwner() == owner && task.getStatus() == TaskStatus.DONE)
            .toList();
    }

    /**
     * Identifica qual status (TO_DO, IN_PROGRESS, BLOCKED) possui o maior número de tarefas.
     *
     * @return descrição do(s) status com maior ocorrência ou mensagem padrão
     */
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
        
        return String.join(" e ", bottlenecks) + " com maior(es) ocorrência(s).";
    }


    /**
     * Adiciona um projeto ao workspace.
     *
     * @param project projeto a ser adicionado
     */
    public void addProject(Project project) {
        projects.add(project);
    }

    /**
     * Retorna uma visão imutável da lista de projetos do workspace.
     *
     * @return lista não modificável de projetos
     */
    public List<Project> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    /**
     * Busca um projeto pelo nome.
     *
     * @param name nome do projeto
     * @return instância de Project correspondente
     * @throws IllegalArgumentException se não encontrar o projeto
     */
    public Project getProjectByName(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Projeto com nome '" + name + "' não encontrado.");
    }

    /**
     * Busca um projeto pelo ID.
     *
     * @param id identificador do projeto
     * @return instância de Project correspondente
     * @throws IllegalArgumentException se não encontrar o projeto
     */
    public Project getProjectById(int id) {
        for (Project p : projects) {
            if (p.getId() == id) {
                return p;
            }
        }
        throw new IllegalArgumentException("Projeto com id '" + id + "' não encontrado.");
    }

    /**
     * Calcula o percentual de conclusão de um projeto (tarefas DONE / total).
     *
     * @param id identificador do projeto
     * @return string formatada com percentual de conclusão
     */
    public String getProjectHealthByProjectId(int id) {
        Project project = getProjectById(id);

        long totalTasks = project.viewTasks().size();
        long doneTasks = project.viewTasks().stream()
            .mapToLong(task -> task.getStatus() == TaskStatus.DONE ? 1 : 0)
            .sum();
        double percentage = totalTasks == 0 ? 0 : (doneTasks * 100.0) / totalTasks;
        return String.format("Projeto: %s - Conclusão: %.1f%% (%d/%d tarefas)", project.getName(), percentage, doneTasks, totalTasks);
    }

    /**
     * Gera um relatório de saúde (percentual de conclusão) para todos os projetos informados.
     *
     * @param projects lista de projetos
     * @return relatório formatado de saúde dos projetos
     */
    public String projectsHealth(List<Project> projects) {
        List<String> reports = projects.stream()
            .map(project -> {
                return getProjectHealthByProjectId(project.getId());
                // long totalTasks = project.viewTasks().size();
                // long doneTasks = project.viewTasks().stream()
                //     .mapToLong(task -> task.getStatus() == TaskStatus.DONE ? 1 : 0)
                //     .sum();
                // double percentage = totalTasks == 0 ? 0 : (doneTasks * 100.0) / totalTasks;
                // return String.format("Projeto: %s - Conclusão: %.1f%% (%d/%d tarefas)",
                //     project.getName(), percentage, doneTasks, totalTasks);
            })
            .collect(Collectors.toList());

        return String.join("\n", reports);
    }
    
}
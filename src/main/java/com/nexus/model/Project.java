package com.nexus.model;

import java.util.ArrayList;
import java.util.List;

import com.nexus.exception.NexusValidationException;

public class Project {
    private final int id;
    private final String name;
    private final List<Task> tasks = new ArrayList<>();
    private final int totalBudget;

    private static int nextId = 1;
    private static boolean active = true; // Flag para controle de status do projeto (pode ser usada para bloqueios futuros)

    private int effortSum = 0; // Soma do esforço das tarefas IN_PROGRESS (facilitar validação)

    public Project(String name, int totalBudget) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do projeto não pode ser vazio.");
        }
        if (totalBudget < 0) {
            throw new IllegalArgumentException("Orçamento total não pode ser negativo.");
        }
        this.id = nextId++;
        this.name = name;
        this.totalBudget = totalBudget;
    }

    public void updateWorkload() { 
        // Método auxiliar para recalcular activeWorkload
        // Pode ser chamado após mudanças de status em tarefas isoladas
        int newWorkload = 0;
        for (Task t : tasks) {
            newWorkload += t.getEffort();
        }
        effortSum = newWorkload;
    }
    
    public void addTask(Task task) {
        // Validação de orçamento ao adicionar nova tarefa
        // A lógica desconsidera o status da tarefa
        int newWorkload = task.getEffort();
        if (effortSum + newWorkload <= totalBudget) {
            effortSum += newWorkload;
        } else {
            throw new NexusValidationException(
                "Não é possível adicionar tarefa '" + task.getTitle() +
                "' ao projeto '" + name + "'. Orçamento excedido."
            );
        }
    
        tasks.add(task);
    }   

    public void removeTask(Task task) {
        effortSum -= task.getEffort();
        tasks.remove(task);
    }

    public void deactivate() {
        active = false;
    }
    
    // Getters
    public int getId() {return id;}
    public String getName() {return name;}
    public boolean isActive() {return active;}
    public List<Task> viewTasks() {return tasks;}
    public int viewTotalBudget() {return totalBudget;}
}





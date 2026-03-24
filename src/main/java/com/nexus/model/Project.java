package com.nexus.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.nexus.exception.NexusValidationException;

/**
 * Representa um projeto dentro do sistema Nexus.
 *
 * Cada projeto possui um identificador, nome, lista de tarefas e um orçamento total.
 * A classe também mantém contadores e estado auxiliar usados nas validações do sistema.
 */
public class Project {
    private final int id;
    private final String name;
    private final List<Task> tasks = new ArrayList<>();
    private final int totalBudget;

    private static int nextId = 1;
    private static boolean active = true; // Flag para controle de status do projeto (pode ser usada para bloqueios futuros)

    private int effortSum = 0; // Soma do esforço das tarefas IN_PROGRESS (facilitar validação)

    /**
     * Cria um novo projeto com nome e orçamento total informados.
     *
     * @param name nome do projeto
     * @param totalBudget orçamento total do projeto (em horas)
     * @throws IllegalArgumentException se o nome for vazio ou o orçamento negativo
     */
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

    /**
     * Recalcula a soma dos esforços das tarefas associadas ao projeto.
     */
    public void updateWorkload() { 
        // Método auxiliar para recalcular activeWorkload
        // Pode ser chamado após mudanças de status em tarefas isoladas
        int newWorkload = 0;
        for (Task t : tasks) {
            newWorkload += t.getEffort();
        }
        effortSum = newWorkload;
    }
    
    /**
     * Tenta adicionar uma nova tarefa ao projeto respeitando o orçamento total.
     * Se o orçamento for excedido, lança {@link com.nexus.exception.NexusValidationException}
     * e incrementa o contador de erros de validação.
     *
     * @param task tarefa a ser adicionada
     * @throws NexusValidationException quando o orçamento é ultrapassado
     */
    public Task addTask(String title, LocalDate deadline, int effort) {
        // Validação de orçamento ao adicionar nova tarefa
        // A lógica desconsidera o status da tarefa
        Task t = new Task(title, deadline);
        t.defineEffort(effort);

        int newWorkload = t.getEffort();
        if (effortSum + newWorkload <= totalBudget) {
            effortSum += newWorkload;
        } else {
            Task.totalValidationErrors++;
            throw new NexusValidationException(
                "Não é possível adicionar tarefa '" + t.getTitle() +
                "' ao projeto '" + name + "'. Orçamento excedido."
            );
        }
    
        tasks.add(t);
        return t;
    }   

    /**
     * Remove uma tarefa do projeto e atualiza a soma de esforços.
     *
     * @param task tarefa a ser removida
     */
    public void removeTask(Task task) {
        effortSum -= task.getEffort();
        tasks.remove(task);
    }

    /**
     * Desativa o projeto, tornando-o inativo para futuras operações.
     */
    public void deactivate() {
        active = false;
    }
    
    // Getters
    /** Retorna o identificador do projeto. */
    public int getId() {return id;}
    /** Retorna o nome do projeto. */
    public String getName() {return name;}
    /** Indica se o projeto está ativo. */
    public boolean isActive() {return active;}
    /** Retorna a lista de tarefas do projeto. */
    public List<Task> viewTasks() {return tasks;}
    /** Retorna o orçamento total do projeto. */
    public int viewTotalBudget() {return totalBudget;}
}





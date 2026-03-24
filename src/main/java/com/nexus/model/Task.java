package com.nexus.model;

import java.time.LocalDate;
import com.nexus.exception.NexusValidationException;

/**
 * Representa uma tarefa do sistema Nexus.
 *
 * Cada tarefa possui título, prazo, esforço estimado, status e um usuário responsável (owner).
 * Opera como uma máquina de estados finitos.
 */
public class Task {
    // Métricas Globais (Alunos implementam a lógica de incremento/decremento)
    public static int totalTasksCreated = 0;
    public static int totalValidationErrors = 0;
    public static int activeWorkload = 0;

    private static int nextId = 1;

    private int id;
    private LocalDate deadline; // Imutável após o nascimento
    private int effort; // Esforço estimado em horas (definido no construtor)
    private String title;
    private TaskStatus status;
    private User owner;

    /**
     * Cria uma nova tarefa com título e prazo informados.
     * O esforço é inicializado com zero e o status padrão é TO_DO.
     *
     * @param title título da tarefa
     * @param deadline data limite da tarefa
     */
    public Task(String title, LocalDate deadline) {
        this.id = nextId++;
        this.deadline = deadline;
        this.effort = 0; // Valor padrão, pode ser atualizado posteriormente
        this.title = title;
        this.status = TaskStatus.TO_DO;
        
        // Ação do Aluno:
        totalTasksCreated++;
    }

    /**
     * Define o esforço estimado para a tarefa.
     *
     * @param effort esforço em horas (deve ser não negativo)
     * @throws IllegalArgumentException se o esforço for negativo
     */
    public void defineEffort(int effort) {
        if (effort < 0) {
            throw new IllegalArgumentException("Esforço não pode ser negativo.");
        }
        this.effort = effort;
    }

    /**
     * Move a tarefa para o status IN_PROGRESS.
     * Só é permitido se houver owner atribuído e a tarefa não estiver BLOCKED.
     *
     * @throws NexusValidationException se não houver owner ou se estiver BLOCKED
     */
    public void moveToInProgress() {
       // TODO: Implementar lógica de proteção e atualizar activeWorkload
        // Se falhar, incrementar totalValidationErrors e lançar NexusValidationException

        if(this.owner == null) {
            totalValidationErrors++;
            throw new NexusValidationException("Não é possível mover uma tarefa para 'in progress' sem usuário associado a tarefa.");
        } else if(this.status == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Não é possível mover uma tarefa do estado 'blocked' para 'in progress'.");
        } else {
            this.status = TaskStatus.IN_PROGRESS;
            activeWorkload++;
        }
    }
    
    /**
     * Finaliza a tarefa, movendo para o status DONE.
     * Só é permitido se a tarefa não estiver BLOCKED.
     *
     * @throws NexusValidationException se a tarefa estiver BLOCKED
     */
    public void markAsDone() {
        // TODO: Implementar lógica de proteção e atualizar activeWorkload (decrementar)
        if(this.status == TaskStatus.BLOCKED) {
            totalValidationErrors++;
            throw new NexusValidationException("Não é possível mover uma tarefa do status 'blocked' para 'done'.");
        }
        activeWorkload--;
        this.status = TaskStatus.DONE;
    }

    /**
     * Marca ou desmarca o status BLOCKED da tarefa.
     * Só pode ser movida para BLOCKED se não estiver DONE.
     *
     * @param blocked true para bloquear, false para desbloquear
     * @throws NexusValidationException se a transição não for permitida
     */
    public void setBlocked(boolean blocked) {

        if (blocked) {
            if(this.status == TaskStatus.DONE) {
                totalValidationErrors++;
                throw new NexusValidationException("Não é possível mover uma tarefa do status 'done' para 'blocked'.");
            }
            this.status = TaskStatus.BLOCKED;
        } else {
            if(this.status != TaskStatus.BLOCKED) {
                totalValidationErrors++;
                throw new NexusValidationException("A tarefa não tem status blocked");
            }
            this.status = TaskStatus.TO_DO; // Simplificação para o Lab
        }
    }

    /**
     * Atribui um usuário responsável pela tarefa.
     *
     * @param user usuário responsável
     */
    public void assignOwner(User user) {
        this.owner = user;
    }

    // Getters
    /** Retorna o identificador da tarefa. */
    public int getId() { return id; }
    /** Retorna o status atual da tarefa. */
    public TaskStatus getStatus() { return status; }
    /** Retorna o título da tarefa. */
    public String getTitle() { return title; }
    /** Retorna o esforço estimado (em horas). */
    public int getEffort() {return this.effort;}
    /** Retorna o prazo (deadline) da tarefa. */
    public LocalDate getDeadline() { return deadline; }
    /** Retorna o usuário responsável (owner), ou null se não houver. */
    public User getOwner() { return owner; }
}
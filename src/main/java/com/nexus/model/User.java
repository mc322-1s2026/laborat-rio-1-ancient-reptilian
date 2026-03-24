package com.nexus.model;

import java.util.List;

/**
 * Representa um usuário (agente executor) do sistema Nexus.
 *
 * Cada usuário possui um nome de usuário (username) e um e-mail válidos.
 * Garante integridade de identidade e validação rigorosa do e-mail.
 * O método {@link #calculateWorkload(List)} permite calcular a carga de trabalho
 * considerando apenas tarefas IN_PROGRESS sob responsabilidade deste usuário.
 */
public class User {
    private final String username;
    private final String email;

    /**
     * Cria um novo usuário com username e e-mail informados.
     *
     * @param username nome de usuário (não pode ser nulo ou vazio)
     * @param email e-mail válido no formato usuario@dominio.com
     * @throws IllegalArgumentException se username ou email forem inválidos
     */
    public User(String username, String email) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio.");
        }

        if (email == null || !validEmail(email)) {
            throw new IllegalArgumentException("Endereço de e-mail inválido.");
        }

        this.username = username;
        this.email = email;
    }

    private boolean validEmail(String email) {

        if (email.isBlank()) return false;

        int arroba = email.indexOf('@');
        if (arroba != email.lastIndexOf('@') || arroba < 1) return false;

        String usuario = email.substring(0, arroba);
        if (usuario.contains(" ") || usuario.contains(".")) return false;
        
        String dominio = email.substring(arroba + 1);
        if (dominio.contains(" ") || !(dominio.contains("."))) return false;
        if (dominio.startsWith(".") || dominio.endsWith(".")) return false;
        if (dominio.indexOf('.') != dominio.lastIndexOf('.')) return false;

        return true;
    }

    /**
     * Retorna o e-mail do usuário.
     * @return e-mail cadastrado
     */
    public String consultEmail() {
        return email;
    }

    /**
     * Retorna o nome de usuário (username).
     * @return username cadastrado
     */
    public String consultUsername() {
        return username;
    }

    /**
     * Calcula a carga de trabalho do usuário, considerando apenas tarefas
     * em IN_PROGRESS sob sua responsabilidade.
     *
     * @param tasks lista de tarefas a serem avaliadas
     * @return quantidade de tarefas IN_PROGRESS deste usuário
     */
    public long calculateWorkload(List<Task> tasks) {
        long total = 0;
        for (Task task : tasks) {
            if (this.equals(task.getOwner()) && task.getStatus() == TaskStatus.IN_PROGRESS) {
                total++;
            }
        }
        return total; 
    }
}
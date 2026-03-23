package com.nexus.model;

import java.util.List;

public class User {
    private final String username;
    private final String email;

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

    public String consultEmail() {
        return email;
    }

    public String consultUsername() {
        return username;
    }

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
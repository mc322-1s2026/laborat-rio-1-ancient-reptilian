package com.nexus.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.nexus.exception.NexusValidationException;
import com.nexus.model.Project;
import com.nexus.model.Task;
import com.nexus.model.User;

/**
 * Processador de logs do sistema Nexus.
 *
 * Responsável por ler arquivos de log contendo comandos de criação e alteração de entidades
 * (usuários, projetos, tarefas) e executar as operações correspondentes no sistema.
 */
public class LogProcessor {

    /**
     * Processa um arquivo de log de comandos, executando operações de criação e alteração
     * de usuários, projetos e tarefas, além de relatórios analíticos.
     *
     * O método garante que exceções de negócio (NexusValidationException) e de entrada
     * (IllegalArgumentException) sejam capturadas e relatadas, sem interromper o processamento
     * do lote, conforme a estratégia fail-fast do Nexus.
     *
     * @param fileName nome do arquivo de log a ser processado (deve estar em resources)
     * @param workspace instância do Workspace onde as entidades serão manipuladas
     * @param users lista global de usuários do sistema
     */
    public void processLog(String fileName, Workspace workspace, List<User> users) {
        try {
            // Busca o arquivo dentro da pasta de recursos do projeto (target/classes)
            var resource = getClass().getClassLoader().getResourceAsStream(fileName);
            
            if (resource == null) {
                throw new IOException("Arquivo não encontrado no classpath: " + fileName);
            }

            try (java.util.Scanner s = new java.util.Scanner(resource).useDelimiter("\\A")) {
                String content = s.hasNext() ? s.next() : "";
                List<String> lines = List.of(content.split("\\R"));
                
                for (String line : lines) {
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] p = line.split(";");
                    String action = p[0];

                    try {
                        switch (action) {
                            case "CREATE_USER" -> {
                                users.add(new User(p[1], p[2]));
                                System.out.println("[LOG] Usuário criado: " + p[1]);
                            }
                            case "CREATE_PROJECT" -> {
                                Project project = new Project(p[1], Integer.parseInt(p[2]));
                                workspace.addProject(project);
                                System.out.println("[LOG] Projeto criado: " + p[1]);
                            }
                            case "CREATE_TASK" -> {
                                Project project = workspace.getProjectByName(p[4]);
                                Task t = project.addTask(p[1], LocalDate.parse(p[2]), Integer.parseInt(p[3]));
                                workspace.addTask(t);
                                System.out.println("[LOG] Tarefa criada: " + p[1]);
                            }
                            case "ASSIGN_USER" -> {
                                Task task = workspace.getTaskById(Integer.parseInt(p[1]));
                                User user = workspace.getUserByName(p[2], users);
                                task.assignOwner(user);
                                System.out.println("[LOG] Tarefa '" + p[1] + "' atribuída a " + p[2]);
                            }
                            case "CHANGE_STATUS" -> {
                                Task task = workspace.getTaskById(Integer.parseInt(p[1]));
                                String newStatus = p[2];
                                switch (newStatus) {
                                    case "TO_DO" -> {
                                        // Caso não existente?
                                        // task.markAsDone();
                                        // System.out.println("[LOG] Tarefa '" + p[1] + "' marcada como TO_DO");
                                    }
                                    case "IN_PROGRESS" -> {
                                        task.moveToInProgress();
                                        System.out.println("[LOG] Tarefa '" + p[1] + "' marcada como IN_PROGRESS");
                                    }
                                    case "BLOCKED" -> {
                                        task.setBlocked(true);
                                        System.out.println("[LOG] Tarefa '" + p[1] + "' marcada como BLOCKED");
                                    }
                                    case "DONE" -> {
                                        task.markAsDone();
                                        System.out.println("[LOG] Tarefa '" + p[1] + "' marcada como DONE");
                                    }
                                    default -> throw new IllegalArgumentException("Status desconhecido: " + newStatus);
                                }
                            }
                            case "REPORT_STATUS" -> {
                                System.out.println(
                                    "[LOG] Usuários com mais tarefas concluídas: " 
                                    + String.join(", ", workspace.topPerformUsers(users)));
                                
                                System.out.println(
                                    "[LOG] Usuários sobrecarregados: " 
                                    + String.join(", ", workspace.overloadedUsers(users)));
                                    
                                System.out.println(
                                    "[LOG] Status de tarefas: " + workspace.globalBottleNeck());

                                System.out.println(
                                    "[LOG] Percentual de conclusão de tarefas:\n" 
                                    + workspace.projectsHealth(workspace.getProjects()));
                                
                            }
                            default -> System.err.println("[WARN] Ação desconhecida: " + action);
                        }
                    } catch (NexusValidationException e) {
                        System.err.println("[ERRO DE REGRAS] Falha no comando '" + line + "': " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.err.println("[ERRO DE ENTRADA] Falha ao criar usuário: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERRO FATAL] " + e.getMessage());
        }
    }
}
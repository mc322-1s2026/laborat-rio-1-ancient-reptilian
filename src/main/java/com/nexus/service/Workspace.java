package com.nexus.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nexus.model.Project;
import com.nexus.model.Task;

public class Workspace {
    private final List<Task> tasks = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();


    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        // Retorna uma visão não modificável para garantir encapsulamento
        return Collections.unmodifiableList(tasks);
    }


    public void addProject(Project project) {
        projects.add(project);
    }

    public List<Project> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    
}
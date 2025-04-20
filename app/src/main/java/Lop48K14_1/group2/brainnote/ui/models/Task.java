package Lop48K14_1.group2.brainnote.ui.models;

import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private String date;
    private boolean completed;
    private int priority; // 0: normal, 1: medium, 2: high
    private String dueDate;

    public Task() {
        // Empty constructor needed for Firebase
        this.id = UUID.randomUUID().toString();
    }

    public Task(String id, String title, String description, String date, boolean completed, int priority, String dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.completed = completed;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    // Constructor without id (will generate a new one)
    public Task(String title, String description, String date, boolean completed, int priority, String dueDate) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.date = date;
        this.completed = completed;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}


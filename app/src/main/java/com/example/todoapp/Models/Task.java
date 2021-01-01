package com.example.todoapp.Models;

public class Task {

    private String taskId;
    private String title;
    private String date;
    private String description;
    private boolean isDone;
    private String categoryId;

    public Task() {}

    public Task(String categoryId, String title, String date) {
        this.categoryId = categoryId;
        this.title = title;
        this.date = date;
        this.description = "No Description";
        this.isDone = false;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}

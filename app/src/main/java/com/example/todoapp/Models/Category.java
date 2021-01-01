package com.example.todoapp.Models;

public class Category {

    private String categoryId;
    private String name;
    private int numOfTasks;

    public Category() {}

    public Category(String name) {
        this.name = name;
        this.numOfTasks = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumOfTasks() {
        return numOfTasks;
    }

    public void setNumOfTasks(int numOfTasks) {
        this.numOfTasks = numOfTasks;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}

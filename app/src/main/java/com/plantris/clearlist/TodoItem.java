package com.plantris.clearlist;

public class TodoItem {

    private final String title;
    private final String description;
    private final String date;

    public TodoItem(String title, String description, String date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }
}
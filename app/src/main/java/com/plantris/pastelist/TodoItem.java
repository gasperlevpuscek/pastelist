package com.plantris.pastelist;

public class TodoItem {

    private final long id;
    private final String title;
    private final String description;
    private final String date;
    private final String time;
    private final Integer reminderMinutesBefore;
    private boolean isCompleted;

    public TodoItem(String title, String description, String date, String time) {
        this(-1L, title, description, date, time, null, false);
    }

    public TodoItem(String title, String description, String date, String time, boolean isCompleted) {
        this(-1L, title, description, date, time, null, isCompleted);
    }

    public TodoItem(long id, String title, String description, String date, String time, Integer reminderMinutesBefore, boolean isCompleted
    ) {this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.reminderMinutesBefore = reminderMinutesBefore;
        this.isCompleted = isCompleted;
    }

    public long getId() {
        return id;
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

    public String getTime() {
        return time;
    }

    public Integer getReminderMinutesBefore() {
        return reminderMinutesBefore;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
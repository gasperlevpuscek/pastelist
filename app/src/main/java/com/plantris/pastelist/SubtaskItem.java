package com.plantris.pastelist;

public class SubtaskItem {

    private final long id;
    private final long taskId;
    private final String title;
    private final String description;
    private boolean isCompleted;

    public SubtaskItem(long id, long taskId, String title, String description, boolean isCompleted) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.isCompleted = isCompleted;
    }

    public long getId() {
        return id;
    }

    public long getTaskId() {
        return taskId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}


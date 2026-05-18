package com.plantris.pastelist;

import android.provider.BaseColumns;

public class DatabaseManager {

    private DatabaseManager() {
    }

    // TASKS
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "plt_task_db";
        public static final String COLUMN_NAME_TITLE = "plt_title";
        public static final String COLUMN_NAME_DESCRIPTION = "plt_description";
        public static final String COLUMN_NAME_DATE = "plt_date";
        public static final String COLUMN_NAME_TIME = "plt_time";
        public static final String COLUMN_UNTIL_REMINDER = "plt_until_reminder";
        public static final String COLUMN_NAME_COMPLETED = "plt_completed";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                    FeedEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    FeedEntry.COLUMN_NAME_DATE + " TEXT," +
                    FeedEntry.COLUMN_NAME_TIME + " TEXT," +
                    FeedEntry.COLUMN_UNTIL_REMINDER + " INTEGER," +
                    FeedEntry.COLUMN_NAME_COMPLETED + " INTEGER NOT NULL DEFAULT 0)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;


    // SUBTASKS
    // Subtasks table for storing child subtasks linked to a parent task
    public static class SubtaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "plt_subtask_db";
        public static final String COLUMN_NAME_TASK_ID = "plt_task_id"; // FK to FeedEntry._ID
        public static final String COLUMN_NAME_TITLE = "plt_subtask_title";
        public static final String COLUMN_NAME_DESCRIPTION = "plt_subtask_description";
        public static final String COLUMN_NAME_COMPLETED = "plt_subtask_completed";
    }

    // Create subtasks table with a foreign key referencing the main tasks table and ON DELETE CASCADE
    public static final String SQL_CREATE_SUBTASK_ENTRIES =
            "CREATE TABLE " + SubtaskEntry.TABLE_NAME + " (" +
                    SubtaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SubtaskEntry.COLUMN_NAME_TASK_ID + " INTEGER NOT NULL," +
                    SubtaskEntry.COLUMN_NAME_TITLE + " TEXT," +
                    SubtaskEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    SubtaskEntry.COLUMN_NAME_COMPLETED + " INTEGER NOT NULL DEFAULT 0," +
                    "FOREIGN KEY(" + SubtaskEntry.COLUMN_NAME_TASK_ID + ") REFERENCES " + FeedEntry.TABLE_NAME + "(" + FeedEntry._ID + ") ON DELETE CASCADE)";

    public static final String SQL_DELETE_SUBTASK_ENTRIES =
            "DROP TABLE IF EXISTS " + SubtaskEntry.TABLE_NAME;

    // SETTINGS
    public static class SettingsEntry implements BaseColumns {
        public static final String TABLE_NAME = "plt_settings_db";
        public static final String COLUMN_NAME_EMAIL = "plt_email";
        public static final String COLUMN_NAME_PASSWORD = "plt_password";
    }

    public static final String SQL_CREATE_SETTINGS_ENTRIES =
            "CREATE TABLE " + SettingsEntry.TABLE_NAME + " (" +
                    SettingsEntry._ID + " INTEGER PRIMARY KEY," +
                    SettingsEntry.COLUMN_NAME_EMAIL + " TEXT," +
                    SettingsEntry.COLUMN_NAME_PASSWORD + " TEXT)";

    public static final String SQL_DELETE_SETTINGS_ENTRIES =
            "DROP TABLE IF EXISTS " + SettingsEntry.TABLE_NAME;
}
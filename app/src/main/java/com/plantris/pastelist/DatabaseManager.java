package com.plantris.pastelist;

import android.provider.BaseColumns;

public final class DatabaseManager {

    private DatabaseManager() {
    }

    // Contract class
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "plt_task_db";
        public static final String COLUMN_NAME_TITLE = "plt_title";
        public static final String COLUMN_NAME_DESCRIPTION = "plt_description";
        public static final String COLUMN_NAME_DATE = "plt_date";
        public static final String COLUMN_NAME_TIME = "plt_time";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                    FeedEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    FeedEntry.COLUMN_NAME_DATE + " TEXT," +
                    FeedEntry.COLUMN_NAME_TIME + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
}
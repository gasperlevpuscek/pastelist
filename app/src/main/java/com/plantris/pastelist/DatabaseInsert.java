package com.plantris.pastelist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseInsert extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "PasteList.db";

    public DatabaseInsert(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseManager.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE " + DatabaseManager.FeedEntry.TABLE_NAME +
                            " ADD COLUMN " + DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED +
                            " INTEGER NOT NULL DEFAULT 0"
            );
        }
        if (oldVersion < 3) {
            db.execSQL(
                    "ALTER TABLE " + DatabaseManager.FeedEntry.TABLE_NAME +
                            " ADD COLUMN " + DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER +
                            " INTEGER"
            );
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertEntry(String title, String description, String date, String time) {
        return insertEntry(title, description, date, time, false, null);
    }

    public long insertEntry(String title, String description, String date, String time, boolean isCompleted) {
        return insertEntry(title, description, date, time, isCompleted, null);
    }

    public long insertEntry(
            String title,
            String description,
            String date,
            String time,
            boolean isCompleted,
            Integer reminderMinutesBefore
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_TIME, time);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED, isCompleted ? 1 : 0);
        if (reminderMinutesBefore == null) {
            values.putNull(DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER);
        } else {
            values.put(DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER, reminderMinutesBefore);
        }

        return db.insert(DatabaseManager.FeedEntry.TABLE_NAME, null, values);
    }

    public int updateCompleted(long id, boolean isCompleted) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED, isCompleted ? 1 : 0);

        String selection = DatabaseManager.FeedEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return db.update(DatabaseManager.FeedEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public int updateEntry(long id, String title, String description, String date, String time) {
        return updateEntry(id, title, description, date, time, null);
    }

    public int updateEntry(
            long id,
            String title,
            String description,
            String date,
            String time,
            Integer reminderMinutesBefore
    ) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_TIME, time);
        if (reminderMinutesBefore == null) {
            values.putNull(DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER);
        } else {
            values.put(DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER, reminderMinutesBefore);
        }

        String selection = DatabaseManager.FeedEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return db.update(DatabaseManager.FeedEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public int deleteEntry(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = DatabaseManager.FeedEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.delete(DatabaseManager.FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    /**
     * Permanently delete entries that have been marked completed.
     * This is intended to be called on app stop/close to clean up completed items.
     */
    public int deleteCompletedEntries() {
        SQLiteDatabase db = getWritableDatabase();
        String selection = DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED + " = ?";
        String[] selectionArgs = {"1"};
        return db.delete(DatabaseManager.FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    public ArrayList<TodoItem> readAllEntries() {
        ArrayList<TodoItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        boolean hasReminderColumn = hasColumn(db, DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER);
        boolean hasCompletedColumn = hasColumn(db, DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED);

        ArrayList<String> projectionList = new ArrayList<>();
        projectionList.add(DatabaseManager.FeedEntry._ID);
        projectionList.add(DatabaseManager.FeedEntry.COLUMN_NAME_TITLE);
        projectionList.add(DatabaseManager.FeedEntry.COLUMN_NAME_DESCRIPTION);
        projectionList.add(DatabaseManager.FeedEntry.COLUMN_NAME_DATE);
        projectionList.add(DatabaseManager.FeedEntry.COLUMN_NAME_TIME);
        if (hasReminderColumn) {
            projectionList.add(DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER);
        }
        if (hasCompletedColumn) {
            projectionList.add(DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED);
        }

        String[] projection = projectionList.toArray(new String[0]);

        Cursor cursor = db.query(
                DatabaseManager.FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                DatabaseManager.FeedEntry._ID + " DESC"
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry._ID)
            );
            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry.COLUMN_NAME_TITLE)
            );
            String subtitle = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry.COLUMN_NAME_DESCRIPTION)
            );
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry.COLUMN_NAME_DATE)
            );
            String time = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry.COLUMN_NAME_TIME)
            );
            Integer reminderMinutesBefore = null;
            if (hasReminderColumn) {
                int reminderIndex = cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry.COLUMN_UNTIL_REMINDER);
                reminderMinutesBefore = cursor.isNull(reminderIndex) ? null : cursor.getInt(reminderIndex);
            }
            boolean isCompleted = false;
            if (hasCompletedColumn) {
                isCompleted = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DatabaseManager.FeedEntry.COLUMN_NAME_COMPLETED)
                ) == 1;
            }

            items.add(new TodoItem(id, title, subtitle, date, time, reminderMinutesBefore, isCompleted));
        }

        cursor.close();
        return items;
    }

    private boolean hasColumn(SQLiteDatabase db, String columnName) {
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + DatabaseManager.FeedEntry.TABLE_NAME + ")", null)) {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(nameIndex))) {
                    return true;
                }
            }
            return false;
        }
    }
}
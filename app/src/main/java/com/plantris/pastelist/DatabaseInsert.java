package com.plantris.pastelist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseInsert extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
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
        db.execSQL(DatabaseManager.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long insertEntry(String title, String description, String date, String time) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(DatabaseManager.FeedEntry.COLUMN_NAME_TIME, time);

        return db.insert(DatabaseManager.FeedEntry.TABLE_NAME, null, values);
    }

    public ArrayList<TodoItem> readAllEntries() {
        ArrayList<TodoItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                DatabaseManager.FeedEntry.COLUMN_NAME_TITLE,
                DatabaseManager.FeedEntry.COLUMN_NAME_DESCRIPTION,
                DatabaseManager.FeedEntry.COLUMN_NAME_DATE,
                DatabaseManager.FeedEntry.COLUMN_NAME_TIME
        };

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

            items.add(new TodoItem(title, subtitle, date, time));
        }

        cursor.close();
        return items;
    }
}
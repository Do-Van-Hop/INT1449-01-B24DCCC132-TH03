package com.example.bai1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bai1.model.Note;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes_db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    private static final String CREATE_TABLE_NOTES = "CREATE TABLE " + TABLE_NOTES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_CONTENT + " TEXT, "
            + COLUMN_CREATED_AT + " INTEGER, "
            + COLUMN_UPDATED_AT + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
        insertSampleNotes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    private void insertSampleNotes(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        long day = 86400000L;
        long hour = 3600000L;

        Note[] sampleNotes = new Note[]{
                new Note("Họp nhóm đồ án Android",
                        "Thảo luận về cấu trúc ứng dụng Quản lý Ghi chú, thống nhất giao diện chuẩn Material Design và hoàn thiện các chức năng CRUD cơ bản.",
                        now - day * 2, now - day * 2),
                new Note("Danh sách công việc tuần này",
                        "1. Ôn tập kiến thức Lập trình Android.\n2. Thực hành xây dựng giao diện với ConstraintLayout và RecyclerView.\n3. Tìm hiểu cách sử dụng SQLiteOpenHelper lưu trữ dữ liệu.\n4. Báo cáo tiến độ đồ án.",
                        now - day, now - day),
                new Note("Mẹo tối ưu hiệu năng Android",
                        "Sử dụng ViewHolder pattern trong RecyclerView để tái sử dụng View. Tránh thực hiện các thao tác I/O hoặc truy vấn cơ sở dữ liệu nặng trên UI Thread.",
                        now - hour * 5, now - hour * 5),
                new Note("Ý tưởng ứng dụng cá nhân",
                        "Phát triển ứng dụng theo dõi thói quen hàng ngày kết hợp thông báo nhắc nhở thông minh và biểu đồ thống kê trực quan.",
                        now - hour * 2, now - hour * 2)
        };

        int id = 1;
        for (Note note : sampleNotes) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, id++);
            values.put(COLUMN_TITLE, note.getTitle());
            values.put(COLUMN_CONTENT, note.getContent());
            values.put(COLUMN_CREATED_AT, note.getCreatedAt());
            values.put(COLUMN_UPDATED_AT, note.getUpdatedAt());
            db.insert(TABLE_NOTES, null, values);
        }
    }

    public void seedSampleNotesIfEmpty() {
        if (getNotesCount() == 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            insertSampleNotes(db);
            db.close();
        } else {
            resequenceIds();
        }
    }

    public void resequenceIds() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_ID + " ASC", null);
            List<Long> oldIds = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    oldIds.add(cursor.getLong(0));
                } while (cursor.moveToNext());
            }
            cursor.close();

            for (int i = 0; i < oldIds.size(); i++) {
                long oldId = oldIds.get(i);
                long newId = i + 1;
                if (oldId != newId) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ID, newId);
                    db.update(TABLE_NOTES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(oldId)});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public long insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        long nextId = 1;
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_ID + ") FROM " + TABLE_NOTES, null);
        if (cursor != null && cursor.moveToFirst()) {
            nextId = cursor.getLong(0) + 1;
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, nextId);
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_CREATED_AT, note.getCreatedAt());
        values.put(COLUMN_UPDATED_AT, note.getUpdatedAt());

        long insertedId = db.insert(TABLE_NOTES, null, values);
        db.close();
        return insertedId;
    }

    public Note getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_CREATED_AT, COLUMN_UPDATED_AT},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Note note = null;
        if (cursor != null && cursor.moveToFirst()) {
            note = new Note(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
            );
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        db.close();
        return note;
    }

    public List<Note> getAllNotes(String searchQuery) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery;
        String[] selectionArgs = null;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE "
                    + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?"
                    + " ORDER BY " + COLUMN_UPDATED_AT + " DESC";
            String queryArg = "%" + searchQuery.trim() + "%";
            selectionArgs = new String[]{queryArg, queryArg};
        } else {
            selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + COLUMN_UPDATED_AT + " DESC";
        }

        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
                );
                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notes;
    }

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_UPDATED_AT, note.getUpdatedAt());

        int count = db.update(TABLE_NOTES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
        return count;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Delete note
            db.delete(TABLE_NOTES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

            // 2. Decrement IDs of all subsequent notes by 1
            db.execSQL("UPDATE " + TABLE_NOTES + " SET " + COLUMN_ID + " = " + COLUMN_ID + " - 1 WHERE " + COLUMN_ID + " > " + id);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public int getNotesCount() {
        String countQuery = "SELECT * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}

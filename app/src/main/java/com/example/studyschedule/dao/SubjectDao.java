package com.example.studyschedule.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.database.DatabaseHelper;
import com.example.studyschedule.model.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng Subject (Phụ trách chính: Thành viên 3 - Subject Management).
 * Thành viên 4 cũng gọi `getAllSubjects()` để đổ dữ liệu vào Spinner khi đặt lịch học.
 */
public class SubjectDao {

    private final DatabaseHelper dbHelper;

    public SubjectDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SUBJECT, null, null, null, null, null,
                DatabaseHelper.COL_SUB_CODE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSubject(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Subject> searchSubjects(String keyword) {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String pattern = "%" + keyword.trim() + "%";
        String selection = DatabaseHelper.COL_SUB_CODE + " LIKE ? OR "
                + DatabaseHelper.COL_SUB_NAME + " LIKE ? OR "
                + DatabaseHelper.COL_SUB_DESC + " LIKE ?";
        String[] args = {pattern, pattern, pattern};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SUBJECT, null, selection, args, null, null,
                DatabaseHelper.COL_SUB_CODE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSubject(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public long insertSubject(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SUB_CODE, subject.getSubjectCode());
        values.put(DatabaseHelper.COL_SUB_NAME, subject.getSubjectName());
        values.put(DatabaseHelper.COL_SUB_DESC, subject.getDescription());
        values.put(DatabaseHelper.COL_SUB_CREDITS, subject.getCredits());
        return db.insert(DatabaseHelper.TABLE_SUBJECT, null, values);
    }

    public int updateSubject(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SUB_CODE, subject.getSubjectCode());
        values.put(DatabaseHelper.COL_SUB_NAME, subject.getSubjectName());
        values.put(DatabaseHelper.COL_SUB_DESC, subject.getDescription());
        values.put(DatabaseHelper.COL_SUB_CREDITS, subject.getCredits());
        return db.update(DatabaseHelper.TABLE_SUBJECT, values,
                DatabaseHelper.COL_SUB_ID + " = ?",
                new String[]{String.valueOf(subject.getId())});
    }

    public int deleteSubject(int subjectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SUBJECT,
                DatabaseHelper.COL_SUB_ID + " = ?",
                new String[]{String.valueOf(subjectId)});
    }

    private Subject mapCursorToSubject(Cursor cursor) {
        return new Subject(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUB_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUB_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUB_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUB_DESC)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SUB_CREDITS))
        );
    }
}

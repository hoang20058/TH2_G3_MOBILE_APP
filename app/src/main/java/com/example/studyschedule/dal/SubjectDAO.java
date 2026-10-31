package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng Subject (Người 3 - feature/subject-management).
 */
public class SubjectDAO {

    private final DBHelper dbHelper;

    public SubjectDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DBHelper.TABLE_SUBJECT, null, null, null, null, null,
                DBHelper.COL_SUB_CODE + " ASC")) {
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
        String selection = DBHelper.COL_SUB_CODE + " LIKE ? OR "
                + DBHelper.COL_SUB_NAME + " LIKE ? OR "
                + DBHelper.COL_SUB_DESC + " LIKE ?";
        String[] args = {pattern, pattern, pattern};

        try (Cursor cursor = db.query(DBHelper.TABLE_SUBJECT, null, selection, args, null, null,
                DBHelper.COL_SUB_CODE + " ASC")) {
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
        values.put(DBHelper.COL_SUB_CODE, subject.getSubjectCode());
        values.put(DBHelper.COL_SUB_NAME, subject.getSubjectName());
        values.put(DBHelper.COL_SUB_DESC, subject.getDescription());
        values.put(DBHelper.COL_SUB_CREDITS, subject.getCredits());
        return db.insert(DBHelper.TABLE_SUBJECT, null, values);
    }

    public int updateSubject(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SUB_CODE, subject.getSubjectCode());
        values.put(DBHelper.COL_SUB_NAME, subject.getSubjectName());
        values.put(DBHelper.COL_SUB_DESC, subject.getDescription());
        values.put(DBHelper.COL_SUB_CREDITS, subject.getCredits());
        return db.update(DBHelper.TABLE_SUBJECT, values,
                DBHelper.COL_SUB_ID + " = ?",
                new String[]{String.valueOf(subject.getId())});
    }

    public int deleteSubject(int subjectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DBHelper.TABLE_SUBJECT,
                DBHelper.COL_SUB_ID + " = ?",
                new String[]{String.valueOf(subjectId)});
    }

    private Subject mapCursorToSubject(Cursor cursor) {
        return new Subject(
                cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_DESC)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_CREDITS))
        );
    }
}

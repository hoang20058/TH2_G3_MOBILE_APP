package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.Student;
import com.example.studyschedule.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng Student (Người 2 - feature/student-management).
 */
public class StudentDAO {

    private final DBHelper dbHelper;

    public StudentDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public Student getStudentByAccountId(int accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null,
                DBHelper.COL_STU_ACCOUNT_ID + " = ?",
                new String[]{String.valueOf(accountId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToStudent(cursor);
            }
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null, null, null, null, null,
                DBHelper.COL_STU_CODE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToStudent(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<Student> searchStudents(String keyword) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String likePattern = "%" + keyword.trim() + "%";
        String selection = DBHelper.COL_STU_CODE + " LIKE ? OR "
                + DBHelper.COL_STU_FULLNAME + " LIKE ? OR "
                + DBHelper.COL_STU_EMAIL + " LIKE ? OR "
                + DBHelper.COL_STU_PHONE + " LIKE ?";
        String[] args = {likePattern, likePattern, likePattern, likePattern};

        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null, selection, args, null, null,
                DBHelper.COL_STU_CODE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToStudent(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public long insertStudentWithAccount(Student student, String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long studentId = -1;
        db.beginTransaction();
        try {
            ContentValues accValues = new ContentValues();
            accValues.put(DBHelper.COL_ACC_USERNAME, username);
            accValues.put(DBHelper.COL_ACC_PASSWORD, password);
            accValues.put(DBHelper.COL_ACC_ROLE, Constants.ROLE_STUDENT);
            long accountId = db.insert(DBHelper.TABLE_ACCOUNT, null, accValues);

            if (accountId != -1) {
                ContentValues stuValues = new ContentValues();
                stuValues.put(DBHelper.COL_STU_CODE, student.getStudentCode());
                stuValues.put(DBHelper.COL_STU_FULLNAME, student.getFullname());
                stuValues.put(DBHelper.COL_STU_EMAIL, student.getEmail());
                stuValues.put(DBHelper.COL_STU_PHONE, student.getPhone());
                stuValues.put(DBHelper.COL_STU_ACCOUNT_ID, (int) accountId);

                studentId = db.insert(DBHelper.TABLE_STUDENT, null, stuValues);
                if (studentId != -1) {
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
        }
        return studentId;
    }

    public int updateStudent(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_STU_CODE, student.getStudentCode());
        values.put(DBHelper.COL_STU_FULLNAME, student.getFullname());
        values.put(DBHelper.COL_STU_EMAIL, student.getEmail());
        values.put(DBHelper.COL_STU_PHONE, student.getPhone());

        return db.update(DBHelper.TABLE_STUDENT, values,
                DBHelper.COL_STU_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
    }

    public int deleteStudent(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (student.getAccountId() > 0) {
            return db.delete(DBHelper.TABLE_ACCOUNT,
                    DBHelper.COL_ACC_ID + " = ?",
                    new String[]{String.valueOf(student.getAccountId())});
        }
        return db.delete(DBHelper.TABLE_STUDENT,
                DBHelper.COL_STU_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
    }

    private Student mapCursorToStudent(Cursor cursor) {
        return new Student(
                cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_STU_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STU_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STU_FULLNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STU_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_STU_PHONE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_STU_ACCOUNT_ID))
        );
    }
}

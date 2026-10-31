package com.example.studyschedule.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.database.DatabaseHelper;
import com.example.studyschedule.model.Student;
import com.example.studyschedule.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng Student (Phụ trách chính: Thành viên 2 - Student Management).
 */
public class StudentDao {

    private final DatabaseHelper dbHelper;

    public StudentDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Lấy thông tin Student dựa vào accountId (Dùng ngay sau khi Student đăng nhập).
     */
    public Student getStudentByAccountId(int accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT, null,
                DatabaseHelper.COL_STU_ACCOUNT_ID + " = ?",
                new String[]{String.valueOf(accountId)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToStudent(cursor);
            }
        }
        return null;
    }

    /**
     * Lấy danh sách toàn bộ Student.
     */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT, null, null, null, null, null,
                DatabaseHelper.COL_STU_CODE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToStudent(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Tìm kiếm Student theo mã sinh viên, họ tên, email hoặc số điện thoại.
     */
    public List<Student> searchStudents(String keyword) {
        List<Student> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String likePattern = "%" + keyword.trim() + "%";
        String selection = DatabaseHelper.COL_STU_CODE + " LIKE ? OR "
                + DatabaseHelper.COL_STU_FULLNAME + " LIKE ? OR "
                + DatabaseHelper.COL_STU_EMAIL + " LIKE ? OR "
                + DatabaseHelper.COL_STU_PHONE + " LIKE ?";
        String[] args = {likePattern, likePattern, likePattern, likePattern};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_STUDENT, null, selection, args, null, null,
                DatabaseHelper.COL_STU_CODE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToStudent(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Thêm mới Student kèm tạo tự động tài khoản đăng nhập (username = studentCode, password mặc định = "123").
     */
    public long insertStudentWithAccount(Student student, String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long studentId = -1;
        db.beginTransaction();
        try {
            ContentValues accValues = new ContentValues();
            accValues.put(DatabaseHelper.COL_ACC_USERNAME, username);
            accValues.put(DatabaseHelper.COL_ACC_PASSWORD, password);
            accValues.put(DatabaseHelper.COL_ACC_ROLE, Constants.ROLE_STUDENT);
            long accountId = db.insert(DatabaseHelper.TABLE_ACCOUNT, null, accValues);

            if (accountId != -1) {
                ContentValues stuValues = new ContentValues();
                stuValues.put(DatabaseHelper.COL_STU_CODE, student.getStudentCode());
                stuValues.put(DatabaseHelper.COL_STU_FULLNAME, student.getFullname());
                stuValues.put(DatabaseHelper.COL_STU_EMAIL, student.getEmail());
                stuValues.put(DatabaseHelper.COL_STU_PHONE, student.getPhone());
                stuValues.put(DatabaseHelper.COL_STU_ACCOUNT_ID, (int) accountId);

                studentId = db.insert(DatabaseHelper.TABLE_STUDENT, null, stuValues);
                if (studentId != -1) {
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
        }
        return studentId;
    }

    /**
     * Cập nhật thông tin Student.
     */
    public int updateStudent(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_STU_CODE, student.getStudentCode());
        values.put(DatabaseHelper.COL_STU_FULLNAME, student.getFullname());
        values.put(DatabaseHelper.COL_STU_EMAIL, student.getEmail());
        values.put(DatabaseHelper.COL_STU_PHONE, student.getPhone());

        return db.update(DatabaseHelper.TABLE_STUDENT, values,
                DatabaseHelper.COL_STU_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
    }

    /**
     * Xóa Student (Nếu xóa Account liên kết thì Student cũng tự động xóa nhờ ON DELETE CASCADE).
     */
    public int deleteStudent(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (student.getAccountId() > 0) {
            return db.delete(DatabaseHelper.TABLE_ACCOUNT,
                    DatabaseHelper.COL_ACC_ID + " = ?",
                    new String[]{String.valueOf(student.getAccountId())});
        }
        return db.delete(DatabaseHelper.TABLE_STUDENT,
                DatabaseHelper.COL_STU_ID + " = ?",
                new String[]{String.valueOf(student.getId())});
    }

    private Student mapCursorToStudent(Cursor cursor) {
        return new Student(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STU_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STU_CODE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STU_FULLNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STU_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STU_PHONE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STU_ACCOUNT_ID))
        );
    }
}

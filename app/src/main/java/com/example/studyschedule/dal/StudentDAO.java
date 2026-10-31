package com.example.studyschedule.dal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng Student.
 * Phụ trách: Người 2 (nhánh feature/student-management).
 */
public class StudentDAO {

    private final DBHelper dbHelper;

    public StudentDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public Student getStudentByAccountId(int accountId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DBHelper.TABLE_STUDENT, null,
                DBHelper.COL_STU_ACCOUNT_ID + " = ?", new String[]{String.valueOf(accountId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
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
        return null;
    }

    public List<Student> getAllStudents() {
        // TODO (Người 2): Lấy toàn bộ danh sách sinh viên từ bảng Student
        return new ArrayList<>();
    }

    public List<Student> searchStudents(String keyword) {
        // TODO (Người 2): Tìm kiếm sinh viên theo mã, tên, email, số điện thoại
        return new ArrayList<>();
    }

    public long insertStudentWithAccount(Student student, String username, String password) {
        // TODO (Người 2): Tạo tài khoản Student trong bảng Account rồi thêm sinh viên vào bảng Student
        return -1;
    }

    public int updateStudent(Student student) {
        // TODO (Người 2): Cập nhật thông tin sinh viên
        return 0;
    }

    public int deleteStudent(Student student) {
        // TODO (Người 2): Xóa sinh viên
        return 0;
    }
}

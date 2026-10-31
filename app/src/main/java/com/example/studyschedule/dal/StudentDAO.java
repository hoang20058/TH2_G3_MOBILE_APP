package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.studyschedule.models.Student;
import com.example.studyschedule.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final DBHelper dbHelper;
    public StudentDAO(Context context) {
        this(context, DBHelper.getInstance(context));
    }
    StudentDAO(Context context, DBHelper helper) {
        dbHelper = helper;
    }
    private Student read(Cursor c) {
        return new Student(c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("studentCode")),
                c.getString(c.getColumnIndexOrThrow("fullname")),
                c.getString(c.getColumnIndexOrThrow("email")),
                c.getString(c.getColumnIndexOrThrow("phone")),
                c.getInt(c.getColumnIndexOrThrow("accountId")));
    }
    public Student getStudentByAccountId(int accountId) {
        try (Cursor c = dbHelper.getReadableDatabase().query("Student", null,
                "accountId = ?", new String[]{String.valueOf(accountId)}, null, null, null)) {
            return c.moveToFirst() ? read(c) : null;
        }
    }
    public int getStudentCount() {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Student", null)) {
            c.moveToFirst();
            return c.getInt(0);
        }
    }
    public List<Student> getAllStudents() { return searchStudents(""); }
    public List<Student> searchStudents(String keyword) {
        List<Student> result = new ArrayList<>();
        String term = "%" + (keyword == null ? "" : keyword.trim())
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Student WHERE studentCode LIKE ? ESCAPE '\\' "
                        + "OR fullname LIKE ? ESCAPE '\\' OR email LIKE ? ESCAPE '\\' "
                        + "OR phone LIKE ? ESCAPE '\\' ORDER BY fullname COLLATE NOCASE, id",
                new String[]{term, term, term, term})) {
            while (c.moveToNext()) result.add(read(c));
        }
        return result;
    }
    public String getUsername(int studentId) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT a.username FROM Account a JOIN Student s ON s.accountId = a.id WHERE s.id = ?",
                new String[]{String.valueOf(studentId)})) {
            return c.moveToFirst() ? c.getString(0) : "";
        }
    }
    public boolean isStudentCodeTaken(String code, int excludedId) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM Student WHERE studentCode = ? AND id <> ? LIMIT 1",
                new String[]{code.trim(), String.valueOf(excludedId)})) { return c.moveToFirst(); }
    }
    public boolean isUsernameTaken(String username) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM Account WHERE username = ? LIMIT 1",
                new String[]{username.trim()})) { return c.moveToFirst(); }
    }
    private ContentValues profile(Student student) {
        if (student.getStudentCode() == null || student.getStudentCode().trim().isEmpty()
                || student.getFullname() == null || student.getFullname().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sinh viên và họ tên không được để trống.");
        }
        ContentValues values = new ContentValues();
        values.put("studentCode", student.getStudentCode().trim());
        values.put("fullname", student.getFullname().trim());
        values.put("email", student.getEmail());
        values.put("phone", student.getPhone());
        return values;
    }
    public long insertStudentWithAccount(Student student, String username, String password) {
        ContentValues values = profile(student);
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập và mật khẩu không được để trống.");
        }
        ContentValues account = new ContentValues();
        account.put("username", username.trim());
        account.put("password", password);
        account.put("role", Constants.ROLE_STUDENT);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            long accountId = db.insertOrThrow("Account", null, account);
            values.put("accountId", accountId);
            long id = db.insertOrThrow("Student", null, values);
            db.setTransactionSuccessful();
            return id;
        } finally { db.endTransaction(); }
    }
    public int updateStudent(Student student) {
        return dbHelper.getWritableDatabase().update("Student", profile(student),
                "id = ?", new String[]{String.valueOf(student.getId())});
    }
    public int deleteStudent(Student student) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted;
        db.beginTransaction();
        try {
            int accountId;
            try (Cursor c = db.rawQuery("SELECT accountId FROM Student WHERE id = ?",
                    new String[]{String.valueOf(student.getId())})) {
                if (!c.moveToFirst()) return 0;
                accountId = c.getInt(0);
            }
            db.delete("StudySchedule", "studentId = ?", new String[]{String.valueOf(student.getId())});
            deleted = db.delete("Student", "id = ?", new String[]{String.valueOf(student.getId())});
            db.delete("Account", "id = ? AND role = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM Student WHERE accountId = Account.id)",
                    new String[]{String.valueOf(accountId), Constants.ROLE_STUDENT});
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
        return deleted;
    }
}

package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.studyschedule.models.Subject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import static com.example.studyschedule.dal.DBHelper.*;

/** Dùng database chung; lỗi SQLite được chuyển cho bên gọi xử lý. */
public class SubjectDAO {
    public static final int DELETE_IN_USE = -2;
    private final DBHelper dbHelper;

    public static class DuplicateCodeException extends IllegalArgumentException {
        public DuplicateCodeException() { super("Duplicate subject code"); }
    }

    public SubjectDAO(Context context) { dbHelper = DBHelper.getInstance(context); }
    public List<Subject> getAllSubjects() { return query(null, null); }

    public List<Subject> searchSubjects(String keyword) {
        String text = clean(keyword);
        if (text.isEmpty()) return getAllSubjects();
        // Escape ký tự LIKE để tìm đúng nội dung người dùng nhập.
        String pattern = "%" + text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return query(COL_SUB_CODE + " LIKE ? ESCAPE '\\' OR " + COL_SUB_NAME
                + " LIKE ? ESCAPE '\\'", new String[]{pattern, pattern});
    }

    private List<Subject> query(String selection, String[] args) {
        List<Subject> result = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().query(TABLE_SUBJECT, null,
                selection, args, null, null, COL_SUB_CODE + " COLLATE NOCASE, " + COL_SUB_ID)) {
            while (c.moveToNext()) result.add(new Subject(
                    c.getInt(c.getColumnIndexOrThrow(COL_SUB_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_SUB_CODE)),
                    c.getString(c.getColumnIndexOrThrow(COL_SUB_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_SUB_DESC)),
                    c.getInt(c.getColumnIndexOrThrow(COL_SUB_CREDITS))));
        }
        return result;
    }

    public boolean isCodeExists(String code, int excludedId) {
        return codeExists(dbHelper.getReadableDatabase(), clean(code).toUpperCase(Locale.ROOT), excludedId);
    }

    private boolean codeExists(SQLiteDatabase db, String code, int excludedId) {
        // So sánh Java hỗ trợ chữ Unicode, kể cả dữ liệu cũ chưa chuẩn hóa.
        try (Cursor c = db.query(TABLE_SUBJECT, new String[]{COL_SUB_CODE},
                COL_SUB_ID + " <> ?", new String[]{String.valueOf(excludedId)}, null, null, null)) {
            while (c.moveToNext()) {
                if (clean(c.getString(0)).toUpperCase(Locale.ROOT).equals(code)) return true;
            }
        }
        return false;
    }

    private ContentValues validatedValues(Subject subject) {
        if (subject == null || clean(subject.getSubjectCode()).isEmpty()
                || clean(subject.getSubjectName()).isEmpty() || subject.getCredits() <= 0) {
            throw new IllegalArgumentException("Code, name and positive credits are required");
        }
        ContentValues values = new ContentValues();
        values.put(COL_SUB_CODE, clean(subject.getSubjectCode()).toUpperCase(Locale.ROOT));
        values.put(COL_SUB_NAME, clean(subject.getSubjectName()));
        values.put(COL_SUB_DESC, clean(subject.getDescription()));
        values.put(COL_SUB_CREDITS, subject.getCredits());
        return values;
    }

    public long insertSubject(Subject subject) {
        ContentValues values = validatedValues(subject);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (codeExists(db, values.getAsString(COL_SUB_CODE), -1)) throw new DuplicateCodeException();
            long id = db.insertOrThrow(TABLE_SUBJECT, null, values);
            db.setTransactionSuccessful();
            return id;
        } finally { db.endTransaction(); }
    }

    public int updateSubject(Subject subject) {
        ContentValues values = validatedValues(subject);
        if (subject.getId() <= 0) throw new IllegalArgumentException("Invalid subject ID");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (codeExists(db, values.getAsString(COL_SUB_CODE), subject.getId())) throw new DuplicateCodeException();
            int rows = db.update(TABLE_SUBJECT, values, COL_SUB_ID + " = ?",
                    new String[]{String.valueOf(subject.getId())});
            db.setTransactionSuccessful();
            return rows;
        } finally { db.endTransaction(); }
    }

    /** 1: đã xóa; 0: không tồn tại; -2: đang dùng; lỗi DB: SQLiteException. */
    public int deleteSubject(int subjectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Kiểm tra cả Cancelled trước khi CASCADE có thể xảy ra.
            try (Cursor c = db.query(TABLE_SCHEDULE, new String[]{COL_SCH_ID},
                    COL_SCH_SUBJECT_ID + " = ?", new String[]{String.valueOf(subjectId)},
                    null, null, null, "1")) {
                if (c.moveToFirst()) return DELETE_IN_USE;
            }
            int rows = db.delete(TABLE_SUBJECT, COL_SUB_ID + " = ?", new String[]{String.valueOf(subjectId)});
            db.setTransactionSuccessful();
            return rows;
        } finally { db.endTransaction(); }
    }

    private static String clean(String value) { return value == null ? "" : value.trim(); }
}

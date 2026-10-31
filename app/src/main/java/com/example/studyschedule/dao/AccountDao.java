package com.example.studyschedule.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.database.DatabaseHelper;
import com.example.studyschedule.model.Account;

/**
 * DAO phụ trách bảng Account (Phụ trách chính: Thành viên 1 - Auth & Thành viên 2 khi tạo tài khoản cho Student).
 */
public class AccountDao {

    private final DatabaseHelper dbHelper;

    public AccountDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Kiểm tra đăng nhập theo username và password.
     * @return Đối tượng Account nếu đúng, hoặc null nếu sai thông tin.
     */
    public Account login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COL_ACC_USERNAME + " = ? AND " + DatabaseHelper.COL_ACC_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNT, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return new Account(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ACC_ROLE))
                );
            }
        }
        return null;
    }

    /**
     * Tạo mới một Account (ví dụ khi Admin thêm mới một Student).
     * @return id của Account vừa tạo, hoặc -1 nếu lỗi (trùng username).
     */
    public long insertAccount(Account account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ACC_USERNAME, account.getUsername());
        values.put(DatabaseHelper.COL_ACC_PASSWORD, account.getPassword());
        values.put(DatabaseHelper.COL_ACC_ROLE, account.getRole());
        return db.insert(DatabaseHelper.TABLE_ACCOUNT, null, values);
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ACCOUNT,
                new String[]{DatabaseHelper.COL_ACC_ID},
                DatabaseHelper.COL_ACC_USERNAME + " = ?",
                new String[]{username}, null, null, null)) {
            return cursor != null && cursor.moveToFirst();
        }
    }
}

package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.Account;

/**
 * DAO phụ trách bảng Account.
 * Phụ trách: Người 1 (nhánh feature/auth-core).
 */
public class AccountDAO {

    private final DBHelper dbHelper;

    public AccountDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    /**
     * Trả về Account khớp username/password, hoặc null nếu sai thông tin đăng nhập.
     */
    public Account login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {
                DBHelper.COL_ACC_ID, DBHelper.COL_ACC_USERNAME,
                DBHelper.COL_ACC_PASSWORD, DBHelper.COL_ACC_ROLE
        };
        String selection = DBHelper.COL_ACC_USERNAME + " = ? AND " + DBHelper.COL_ACC_PASSWORD + " = ?";

        try (Cursor cursor = db.query(DBHelper.TABLE_ACCOUNT, columns, selection,
                new String[]{username, password}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Account(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_ACC_ROLE))
                );
            }
        }
        return null;
    }

    /**
     * Thêm Account mới. Trả về id vừa tạo, hoặc -1 nếu lỗi (ví dụ trùng username).
     */
    public long insertAccount(Account account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_ACC_USERNAME, account.getUsername());
        values.put(DBHelper.COL_ACC_PASSWORD, account.getPassword());
        values.put(DBHelper.COL_ACC_ROLE, account.getRole());
        return db.insert(DBHelper.TABLE_ACCOUNT, null, values);
    }
}

package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.Account;

/**
 * DAO phụ trách bảng Account (Người 1 - feature/auth-core).
 */
public class AccountDAO {

    private final DBHelper dbHelper;

    public AccountDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public Account login(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DBHelper.COL_ACC_USERNAME + " = ? AND " + DBHelper.COL_ACC_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        try (Cursor cursor = db.query(DBHelper.TABLE_ACCOUNT, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
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

    public long insertAccount(Account account) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_ACC_USERNAME, account.getUsername());
        values.put(DBHelper.COL_ACC_PASSWORD, account.getPassword());
        values.put(DBHelper.COL_ACC_ROLE, account.getRole());
        return db.insert(DBHelper.TABLE_ACCOUNT, null, values);
    }
}

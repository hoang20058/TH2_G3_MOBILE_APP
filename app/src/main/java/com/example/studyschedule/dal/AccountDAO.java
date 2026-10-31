package com.example.studyschedule.dal;

import android.content.Context;
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

    public Account login(String username, String password) {
        // TODO (Người 1): Viết câu lệnh SQL kiểm tra đăng nhập trong bảng Account
        return null;
    }

    public long insertAccount(Account account) {
        // TODO (Người 1 / Người 2): Viết câu lệnh thêm mới Account vào SQLite
        return -1;
    }
}

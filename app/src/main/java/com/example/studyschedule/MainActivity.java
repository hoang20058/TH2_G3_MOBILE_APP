package com.example.studyschedule;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity (Người 1 - nhánh feature/auth-core).
 * TODO (Người 1):
 * - Kiểm tra thông tin đăng nhập từ SessionManager (SharedPreferences)
 * - Phân quyền hiển thị giao diện điều hướng cho Admin hoặc Student
 * - Xử lý nút Logout
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO (Người 1): Code điều hướng theo vai trò Admin / Student tại đây
    }
}

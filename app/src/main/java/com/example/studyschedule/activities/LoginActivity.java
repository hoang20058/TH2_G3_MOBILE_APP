package com.example.studyschedule.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studyschedule.R;

/**
 * LoginActivity (Người 1 - nhánh feature/auth-core).
 * TODO (Người 1):
 * - Ánh xạ edtUsername, edtPassword, btnLogin từ R.layout.activity_login
 * - Kiểm tra đăng nhập qua AccountDAO
 * - Lưu thông tin tài khoản bằng SessionManager (SharedPreferences)
 * - Điều hướng sang MainActivity theo vai trò Admin / Student
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // TODO (Người 1): Xử lý sự kiện đăng nhập và lưu SharedPreferences tại đây
    }
}

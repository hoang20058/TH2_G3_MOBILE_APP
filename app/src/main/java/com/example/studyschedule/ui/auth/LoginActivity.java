package com.example.studyschedule.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.R;
import com.example.studyschedule.dao.AccountDao;
import com.example.studyschedule.dao.StudentDao;
import com.example.studyschedule.model.Account;
import com.example.studyschedule.model.Student;
import com.example.studyschedule.ui.dashboard.AdminDashboardActivity;
import com.example.studyschedule.ui.dashboard.StudentDashboardActivity;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.SessionManager;

/**
 * Màn hình Login (Phụ trách chính: Thành viên 1).
 * Tài khoản mẫu đã có sẵn trong DatabaseHelper:
 * - Admin: admin / 123
 * - Student: sv01 / 123 hoặc sv02 / 123
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private AccountDao accountDao;
    private StudentDao studentDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountDao = new AccountDao(this);
        studentDao = new StudentDao(this);
        sessionManager = new SessionManager(this);

        // Nếu đã đăng nhập trước đó trong SharedPreferences thì tự động chuyển màn hình
        if (sessionManager.isLoggedIn()) {
            navigateByRole(sessionManager.getRole());
            return;
        }

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Account account = accountDao.login(username, password);
        if (account == null) {
            Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
            return;
        }

        int studentId = -1;
        if (Constants.ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
            Student student = studentDao.getStudentByAccountId(account.getId());
            if (student != null) {
                studentId = student.getId();
            }
        }

        // Lưu thông tin đăng nhập vào SharedPreferences
        sessionManager.saveLoginSession(account.getId(), studentId, account.getUsername(), account.getRole());
        navigateByRole(account.getRole());
    }

    private void navigateByRole(String role) {
        Intent intent;
        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}

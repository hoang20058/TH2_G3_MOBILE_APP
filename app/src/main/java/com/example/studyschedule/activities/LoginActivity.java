package com.example.studyschedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.MainActivity;
import com.example.studyschedule.R;
import com.example.studyschedule.dal.AccountDAO;
import com.example.studyschedule.dal.StudentDAO;
import com.example.studyschedule.models.Account;
import com.example.studyschedule.models.Student;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.SessionManager;

/**
 * LoginActivity (Người 1 - nhánh feature/auth-core).
 * - Kiểm tra đăng nhập qua AccountDAO
 * - Lưu thông tin tài khoản bằng SessionManager (SharedPreferences)
 * - Điều hướng sang MainActivity theo vai trò Admin / Student
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;

    private AccountDAO accountDAO;
    private StudentDAO studentDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        // Đã đăng nhập từ trước thì vào thẳng MainActivity
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        accountDAO = new AccountDAO(this);
        studentDAO = new StudentDAO(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (username.isEmpty()) {
            edtUsername.setError(getString(R.string.login_error_username_empty));
            edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtPassword.setError(getString(R.string.login_error_password_empty));
            edtPassword.requestFocus();
            return;
        }

        Account account = accountDAO.login(username, password);
        if (account == null) {
            Toast.makeText(this, R.string.login_error_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        // Admin không gắn với Student nên studentId = -1
        int studentId = -1;
        if (Constants.ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
            Student student = studentDAO.getStudentByAccountId(account.getId());
            if (student == null) {
                Toast.makeText(this, R.string.login_error_no_student, Toast.LENGTH_SHORT).show();
                return;
            }
            studentId = student.getId();
        } else if (!Constants.ROLE_ADMIN.equalsIgnoreCase(account.getRole())) {
            Toast.makeText(this, R.string.login_error_invalid_role, Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.saveLoginSession(account.getId(), studentId, account.getUsername(), account.getRole());
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
        goToMain();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

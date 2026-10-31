package com.example.studyschedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
 * LoginActivity (Người 1 - feature/auth-core).
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
        setContentView(R.layout.activity_login);

        accountDAO = new AccountDAO(this);
        studentDAO = new StudentDAO(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
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

        Account account = accountDAO.login(username, password);
        if (account == null) {
            Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
            return;
        }

        int studentId = -1;
        if (Constants.ROLE_STUDENT.equalsIgnoreCase(account.getRole())) {
            Student student = studentDAO.getStudentByAccountId(account.getId());
            if (student != null) {
                studentId = student.getId();
            }
        }

        sessionManager.saveLoginSession(account.getId(), studentId, account.getUsername(), account.getRole());
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

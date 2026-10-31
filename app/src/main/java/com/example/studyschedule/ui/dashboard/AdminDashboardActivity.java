package com.example.studyschedule.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.R;
import com.example.studyschedule.ui.auth.LoginActivity;
import com.example.studyschedule.ui.student.StudentManagementActivity;
import com.example.studyschedule.ui.subject.SubjectManagementActivity;
import com.example.studyschedule.utils.SessionManager;

/**
 * Màn hình điều hướng cho ADMIN (Phụ trách chính: Thành viên 1).
 * Điều hướng sang:
 * - Student Management (Thành viên 2)
 * - Subject Management (Thành viên 3)
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        TextView tvWelcome = findViewById(R.id.tvAdminWelcome);
        Button btnManageStudents = findViewById(R.id.btnManageStudents);
        Button btnManageSubjects = findViewById(R.id.btnManageSubjects);
        Button btnLogout = findViewById(R.id.btnAdminLogout);

        tvWelcome.setText("Welcome Admin: " + sessionManager.getUsername());

        btnManageStudents.setOnClickListener(v ->
                startActivity(new Intent(this, StudentManagementActivity.class)));

        btnManageSubjects.setOnClickListener(v ->
                startActivity(new Intent(this, SubjectManagementActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

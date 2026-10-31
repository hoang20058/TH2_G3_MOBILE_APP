package com.example.studyschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.activities.CalendarScheduleActivity;
import com.example.studyschedule.activities.LoginActivity;
import com.example.studyschedule.activities.ScheduleBookingActivity;
import com.example.studyschedule.activities.ScheduleListActivity;
import com.example.studyschedule.activities.StudentManagementActivity;
import com.example.studyschedule.activities.SubjectManagementActivity;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.SessionManager;

/**
 * MainActivity (Người 1 - nhánh feature/auth-core).
 * - Kiểm tra thông tin đăng nhập từ SessionManager (SharedPreferences)
 * - Phân quyền hiển thị giao diện điều hướng cho Admin hoặc Student
 * - Xử lý nút Logout
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    private TextView tvWelcomeUser;
    private LinearLayout layoutAdminActions, layoutStudentActions;
    private Button btnManageStudents, btnManageSubjects;
    private Button btnMySchedules, btnBookSchedule, btnCalendarView;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        // Chưa đăng nhập thì quay về màn hình Login
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);
        bindViews();

        if (sessionManager.isAdmin()) {
            showAdminActions();
        } else if (sessionManager.isStudent()) {
            showStudentActions();
        } else {
            // Role không hợp lệ: xóa phiên đăng nhập để tránh kẹt ở màn hình trống
            sessionManager.logout();
            goToLogin();
            return;
        }

        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    private void bindViews() {
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        layoutAdminActions = findViewById(R.id.layoutAdminActions);
        layoutStudentActions = findViewById(R.id.layoutStudentActions);
        btnManageStudents = findViewById(R.id.btnManageStudents);
        btnManageSubjects = findViewById(R.id.btnManageSubjects);
        btnMySchedules = findViewById(R.id.btnMySchedules);
        btnBookSchedule = findViewById(R.id.btnBookSchedule);
        btnCalendarView = findViewById(R.id.btnCalendarView);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void showAdminActions() {
        tvWelcomeUser.setText("Xin chào, " + sessionManager.getUsername() + " (Admin)");
        layoutAdminActions.setVisibility(View.VISIBLE);
        layoutStudentActions.setVisibility(View.GONE);

        btnManageStudents.setOnClickListener(v ->
                startActivity(new Intent(this, StudentManagementActivity.class)));
        btnManageSubjects.setOnClickListener(v ->
                startActivity(new Intent(this, SubjectManagementActivity.class)));
    }

    private void showStudentActions() {
        tvWelcomeUser.setText("Xin chào, " + sessionManager.getUsername() + " (Sinh viên)");
        layoutAdminActions.setVisibility(View.GONE);
        layoutStudentActions.setVisibility(View.VISIBLE);

        btnMySchedules.setOnClickListener(v -> openStudentScreen(ScheduleListActivity.class));
        btnBookSchedule.setOnClickListener(v -> openStudentScreen(ScheduleBookingActivity.class));
        btnCalendarView.setOnClickListener(v -> openStudentScreen(CalendarScheduleActivity.class));
    }

    private void openStudentScreen(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.putExtra(Constants.EXTRA_STUDENT_ID, sessionManager.getStudentId());
        startActivity(intent);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.logout();
                    goToLogin();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Xóa back stack để không quay lại MainActivity sau khi đăng xuất
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

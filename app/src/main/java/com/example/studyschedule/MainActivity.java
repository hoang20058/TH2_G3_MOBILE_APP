package com.example.studyschedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.activities.CalendarScheduleActivity;
import com.example.studyschedule.activities.LoginActivity;
import com.example.studyschedule.activities.ScheduleBookingActivity;
import com.example.studyschedule.activities.ScheduleListActivity;
import com.example.studyschedule.activities.StudentManagementActivity;
import com.example.studyschedule.activities.SubjectManagementActivity;
import com.example.studyschedule.utils.SessionManager;

/**
 * MainActivity đặt ở thư mục gốc theo đúng mẫu cấu trúc đề thi:
 * com.example.studyschedule
 * ├── activities
 * ├── dal
 * ├── models
 * └── MainActivity.java
 *
 * Tự động hiển thị cụm chức năng Admin hoặc Student dựa vào Role lưu trong SharedPreferences.
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        TextView tvWelcome = findViewById(R.id.tvWelcomeUser);
        LinearLayout layoutAdminActions = findViewById(R.id.layoutAdminActions);
        LinearLayout layoutStudentActions = findViewById(R.id.layoutStudentActions);

        Button btnManageStudents = findViewById(R.id.btnManageStudents);
        Button btnManageSubjects = findViewById(R.id.btnManageSubjects);
        Button btnMySchedules = findViewById(R.id.btnMySchedules);
        Button btnBookSchedule = findViewById(R.id.btnBookSchedule);
        Button btnCalendarView = findViewById(R.id.btnCalendarView);
        Button btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Welcome " + sessionManager.getRole() + ": " + sessionManager.getUsername());

        if (sessionManager.isAdmin()) {
            layoutAdminActions.setVisibility(View.VISIBLE);
            layoutStudentActions.setVisibility(View.GONE);
        } else {
            layoutAdminActions.setVisibility(View.GONE);
            layoutStudentActions.setVisibility(View.VISIBLE);
        }

        btnManageStudents.setOnClickListener(v ->
                startActivity(new Intent(this, StudentManagementActivity.class)));

        btnManageSubjects.setOnClickListener(v ->
                startActivity(new Intent(this, SubjectManagementActivity.class)));

        btnMySchedules.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleListActivity.class)));

        btnBookSchedule.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleBookingActivity.class)));

        btnCalendarView.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarScheduleActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

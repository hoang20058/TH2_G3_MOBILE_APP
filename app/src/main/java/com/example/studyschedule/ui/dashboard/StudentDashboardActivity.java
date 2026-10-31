package com.example.studyschedule.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.R;
import com.example.studyschedule.ui.auth.LoginActivity;
import com.example.studyschedule.ui.calendar.CalendarScheduleActivity;
import com.example.studyschedule.ui.schedule.ScheduleBookingActivity;
import com.example.studyschedule.ui.schedule.ScheduleListActivity;
import com.example.studyschedule.utils.SessionManager;

/**
 * Màn hình điều hướng cho STUDENT (Phụ trách chính: Thành viên 1).
 * Điều hướng sang:
 * - My Study Schedules (Thành viên 4)
 * - Book Study Session (Thành viên 4)
 * - Calendar View & Reminders (Thành viên 5)
 */
public class StudentDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        sessionManager = new SessionManager(this);

        TextView tvWelcome = findViewById(R.id.tvStudentWelcome);
        Button btnMySchedules = findViewById(R.id.btnMySchedules);
        Button btnBookSchedule = findViewById(R.id.btnBookSchedule);
        Button btnCalendarView = findViewById(R.id.btnCalendarView);
        Button btnLogout = findViewById(R.id.btnStudentLogout);

        tvWelcome.setText("Welcome Student: " + sessionManager.getUsername()
                + " (ID: " + sessionManager.getStudentId() + ")");

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

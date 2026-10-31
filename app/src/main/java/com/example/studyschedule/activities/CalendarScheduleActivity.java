package com.example.studyschedule.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.adapters.ScheduleAdapter;
import com.example.studyschedule.dal.StudyScheduleDAO;
import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.utils.SessionManager;

import java.util.List;
import java.util.Locale;

/**
 * CalendarScheduleActivity (Khung cơ bản cho Người 5 - feature/calendar-notifications).
 */
public class CalendarScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvSelectedPeriod;
    private Button btnModeDay, btnModeWeek, btnModeMonth;
    private RecyclerView rvCalendarSchedules;

    private StudyScheduleDAO scheduleDAO;
    private SessionManager sessionManager;
    private ScheduleAdapter adapter;
    private String selectedDate = "2026-10-31";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_schedule);

        scheduleDAO = new StudyScheduleDAO(this);
        sessionManager = new SessionManager(this);

        calendarView = findViewById(R.id.calendarView);
        tvSelectedPeriod = findViewById(R.id.tvSelectedPeriod);
        btnModeDay = findViewById(R.id.btnModeDay);
        btnModeWeek = findViewById(R.id.btnModeWeek);
        btnModeMonth = findViewById(R.id.btnModeMonth);
        rvCalendarSchedules = findViewById(R.id.rvCalendarSchedules);

        rvCalendarSchedules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter();
        rvCalendarSchedules.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            loadDailySchedule(selectedDate);
        });

        // TODO (Người 5): Hoàn thiện xem theo Ngày/Tuần/Tháng, xem chi tiết lịch và nhắc lịch sắp tới
        loadDailySchedule(selectedDate);
    }

    private void loadDailySchedule(String date) {
        tvSelectedPeriod.setText("Daily Schedule: " + date);
        List<StudySchedule> list = scheduleDAO.getSchedulesByDate(sessionManager.getStudentId(), date);
        adapter.setScheduleList(list);
    }
}

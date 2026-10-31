package com.example.studyschedule.ui.calendar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.dao.StudyScheduleDao;
import com.example.studyschedule.model.StudySchedule;
import com.example.studyschedule.ui.schedule.ScheduleAdapter;
import com.example.studyschedule.utils.SessionManager;

import java.util.List;
import java.util.Locale;

/**
 * Màn hình Lịch học theo Ngày / Tuần / Tháng (Calendar View) (Phụ trách chính: Thành viên 5).
 * Chức năng yêu cầu:
 * - Hiển thị lịch học theo Ngày (Daily), Tuần (Weekly), hoặc Tháng (Monthly)
 * - Kết hợp với NotificationHelper & ScheduleAlarmReceiver để nhắc nhở lịch học sắp tới
 */
public class CalendarScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvSelectedPeriod;
    private Button btnModeDay, btnModeWeek, btnModeMonth;
    private RecyclerView rvCalendarSchedules;

    private StudyScheduleDao scheduleDao;
    private SessionManager sessionManager;
    private ScheduleAdapter adapter;
    private String selectedDate = "2026-11-01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_schedule);

        scheduleDao = new StudyScheduleDao(this);
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

        btnModeDay.setOnClickListener(v -> loadDailySchedule(selectedDate));

        btnModeWeek.setOnClickListener(v -> {
            // TODO (Thành viên 5): Tính ngày đầu tuần & cuối tuần của selectedDate rồi gọi scheduleDao.getSchedulesBetweenDates(...)
        });

        btnModeMonth.setOnClickListener(v -> {
            // TODO (Thành viên 5): Lấy từ yyyy-MM-01 đến yyyy-MM-31 bằng scheduleDao.getSchedulesBetweenDates(...)
            String monthPrefix = selectedDate.substring(0, 7);
            List<StudySchedule> list = scheduleDao.getSchedulesBetweenDates(
                    sessionManager.getStudentId(), monthPrefix + "-01", monthPrefix + "-31");
            tvSelectedPeriod.setText("Monthly Schedule: " + monthPrefix);
            adapter.setScheduleList(list);
        });

        loadDailySchedule(selectedDate);
    }

    private void loadDailySchedule(String date) {
        tvSelectedPeriod.setText("Daily Schedule: " + date);
        List<StudySchedule> list = scheduleDao.getSchedulesByDate(sessionManager.getStudentId(), date);
        adapter.setScheduleList(list);
    }
}

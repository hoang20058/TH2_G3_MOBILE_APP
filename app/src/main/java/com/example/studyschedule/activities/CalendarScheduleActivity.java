package com.example.studyschedule.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.dal.DBHelper;
import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.NotificationHelper;
import com.example.studyschedule.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Phụ trách: Người 5 (Minh - nhánh feature/calendar-notifications).
 * Hoạt động độc lập 100% (không phụ thuộc hay sửa vào file của 4 thành viên còn lại):
 * 1. Hiển thị lịch học theo Ngày (Daily), Tuần (Weekly), Tháng (Monthly).
 * 2. Chọn lịch để xem chi tiết đầy đủ (AlertDialog).
 * 3. Nhắc lịch sắp đến (AlarmManager + Notification).
 * 4. Cập nhật hoặc hủy nhắc lịch khi lịch thay đổi trạng thái (Confirmed/Cancelled) hoặc bị xóa.
 */
public class CalendarScheduleActivity extends AppCompatActivity {

    private static final int MODE_DAILY = 0;
    private static final int MODE_WEEKLY = 1;
    private static final int MODE_MONTHLY = 2;
    private static final int REQ_NOTIFICATION_PERMISSION = 101;

    private CalendarView calendarView;
    private TextView tvSelectedPeriod, tvEmptyCalendar;
    private Button btnModeDay, btnModeWeek, btnModeMonth;
    private Button btnSyncReminders, btnBookOnSelectedDate;
    private RecyclerView rvCalendarSchedules;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private CalendarScheduleListAdapter calendarAdapter;

    private String selectedDate;
    private int currentViewMode = MODE_DAILY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_schedule);

        dbHelper = DBHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        requestNotificationPermissionIfNeeded();
        NotificationHelper.createNotificationChannel(this);

        calendarView = findViewById(R.id.calendarView);
        tvSelectedPeriod = findViewById(R.id.tvSelectedPeriod);
        tvEmptyCalendar = findViewById(R.id.tvEmptyCalendar);
        btnModeDay = findViewById(R.id.btnModeDay);
        btnModeWeek = findViewById(R.id.btnModeWeek);
        btnModeMonth = findViewById(R.id.btnModeMonth);
        btnSyncReminders = findViewById(R.id.btnSyncReminders);
        btnBookOnSelectedDate = findViewById(R.id.btnBookOnSelectedDate);
        rvCalendarSchedules = findViewById(R.id.rvCalendarSchedules);

        selectedDate = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US).format(new Date());

        rvCalendarSchedules.setLayoutManager(new LinearLayoutManager(this));
        calendarAdapter = new CalendarScheduleListAdapter();
        rvCalendarSchedules.setAdapter(calendarAdapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            refreshSchedulesByMode();
        });

        btnModeDay.setOnClickListener(v -> {
            currentViewMode = MODE_DAILY;
            refreshSchedulesByMode();
        });

        btnModeWeek.setOnClickListener(v -> {
            currentViewMode = MODE_WEEKLY;
            refreshSchedulesByMode();
        });

        btnModeMonth.setOnClickListener(v -> {
            currentViewMode = MODE_MONTHLY;
            refreshSchedulesByMode();
        });

        btnSyncReminders.setOnClickListener(v -> {
            List<StudySchedule> allSchedules = querySchedulesByRange(getCurrentStudentId(), "0000-01-01", "9999-12-31");
            int count = NotificationHelper.syncAllReminders(this, allSchedules, true);
            if (count == 0) {
                Toast.makeText(this, "No active upcoming study sessions to remind.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Synced reminders for " + count + " upcoming session(s)!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBookOnSelectedDate.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleBookingActivity.class);
            intent.putExtra(Constants.EXTRA_SELECTED_DATE, selectedDate);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSchedulesByMode();
        List<StudySchedule> allSchedules = querySchedulesByRange(getCurrentStudentId(), "0000-01-01", "9999-12-31");
        NotificationHelper.syncAllReminders(this, allSchedules, false);
    }

    /**
     * Lấy studentId từ SessionManager (Mặc định fallback về 1 nếu Người 1 chưa làm xong Login để test độc lập).
     */
    private int getCurrentStudentId() {
        int id = sessionManager.getStudentId();
        return id > 0 ? id : 1;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    private void refreshSchedulesByMode() {
        int studentId = getCurrentStudentId();
        List<StudySchedule> list;

        if (currentViewMode == MODE_DAILY) {
            tvSelectedPeriod.setText("Daily Schedule (" + selectedDate + ")");
            list = querySchedulesByRange(studentId, selectedDate, selectedDate);
        } else if (currentViewMode == MODE_WEEKLY) {
            String[] weekBounds = getWeekBounds(selectedDate);
            tvSelectedPeriod.setText("Weekly Schedule (" + weekBounds[0] + " to " + weekBounds[1] + ")");
            list = querySchedulesByRange(studentId, weekBounds[0], weekBounds[1]);
        } else {
            String[] monthBounds = getMonthBounds(selectedDate);
            tvSelectedPeriod.setText("Monthly Schedule (" + monthBounds[0] + " to " + monthBounds[1] + ")");
            list = querySchedulesByRange(studentId, monthBounds[0], monthBounds[1]);
        }

        calendarAdapter.setSchedules(list);
        tvEmptyCalendar.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /**
     * Truy vấn lịch học trực tiếp từ DBHelper trong khoảng [fromDate, toDate] để không đụng file DAO của Người 4.
     */
    private List<StudySchedule> querySchedulesByRange(int studentId, String fromDate, String toDate) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DBHelper.COL_SUB_CODE + ", sub." + DBHelper.COL_SUB_NAME
                + " FROM " + DBHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DBHelper.TABLE_SUBJECT + " sub ON s." + DBHelper.COL_SCH_SUBJECT_ID + " = sub." + DBHelper.COL_SUB_ID
                + " WHERE s." + DBHelper.COL_SCH_STUDENT_ID + " = ? "
                + " AND s." + DBHelper.COL_SCH_DATE + " >= ? AND s." + DBHelper.COL_SCH_DATE + " <= ? "
                + " ORDER BY s." + DBHelper.COL_SCH_DATE + " ASC, s." + DBHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId), fromDate, toDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    StudySchedule schedule = new StudySchedule(
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_STUDENT_ID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_SUBJECT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_START_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_END_TIME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_LOCATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SCH_STATUS))
                    );
                    schedule.setSubjectCode(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_CODE)));
                    schedule.setSubjectName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_SUB_NAME)));
                    list.add(schedule);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    private void updateStatusInDb(int scheduleId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SCH_STATUS, newStatus);
        db.update(DBHelper.TABLE_SCHEDULE, values, DBHelper.COL_SCH_ID + " = ?", new String[]{String.valueOf(scheduleId)});
    }

    private void deleteScheduleInDb(int scheduleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_SCHEDULE, DBHelper.COL_SCH_ID + " = ?", new String[]{String.valueOf(scheduleId)});
    }

    private String[] getWeekBounds(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        Calendar cal = Calendar.getInstance();
        try {
            Date date = sdf.parse(dateStr);
            if (date != null) cal.setTime(date);
        } catch (ParseException ignored) {
        }

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String startOfWeek = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_WEEK, 6);
        String endOfWeek = sdf.format(cal.getTime());

        return new String[]{startOfWeek, endOfWeek};
    }

    private String[] getMonthBounds(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        Calendar cal = Calendar.getInstance();
        try {
            Date date = sdf.parse(dateStr);
            if (date != null) cal.setTime(date);
        } catch (ParseException ignored) {
        }

        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startOfMonth = sdf.format(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endOfMonth = sdf.format(cal.getTime());

        return new String[]{startOfMonth, endOfMonth};
    }

    private void showScheduleDetailsDialog(StudySchedule schedule) {
        String subjectTitle = (schedule.getSubjectCode() != null ? schedule.getSubjectCode() + " - " : "")
                + (schedule.getSubjectName() != null ? schedule.getSubjectName() : "Subject #" + schedule.getSubjectId());

        String details = "Subject: " + subjectTitle + "\n\n"
                + "Study Date: " + schedule.getStudyDate() + "\n"
                + "Time: " + schedule.getStartTime() + " - " + schedule.getEndTime() + "\n"
                + "Location: " + schedule.getLocation() + "\n"
                + "Current Status: " + schedule.getStatus();

        String[] actions = {
                "Send / Update Reminder Notification",
                "Mark as Confirmed (Update Reminder)",
                "Mark as Cancelled (Cancel Reminder)",
                "Edit Schedule",
                "Delete Schedule (Cancel Reminder)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Schedule Details")
                .setMessage(details)
                .setPositiveButton("Remind Now", (dialog, which) -> {
                    NotificationHelper.scheduleOrUpdateReminder(this, schedule);
                    NotificationHelper.sendInstantNotification(
                            this,
                            "Study Reminder: " + subjectTitle,
                            "Session on " + schedule.getStudyDate() + " (" + schedule.getStartTime()
                                    + " - " + schedule.getEndTime() + ") at " + schedule.getLocation()
                    );
                    Toast.makeText(this, "Reminder notification sent & scheduled!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Manage / Status", (dialog, which) ->
                        showManageScheduleOptions(schedule, actions))
                .setNegativeButton("Close", null)
                .show();
    }

    private void showManageScheduleOptions(StudySchedule schedule, String[] actions) {
        new AlertDialog.Builder(this)
                .setTitle("Manage Schedule & Reminder")
                .setItems(actions, (dialog, index) -> {
                    if (index == 0) {
                        NotificationHelper.scheduleOrUpdateReminder(this, schedule);
                        NotificationHelper.sendInstantNotification(
                                this,
                                "Reminder Set",
                                "Reminder active for " + schedule.getStudyDate() + " at " + schedule.getStartTime()
                        );
                        Toast.makeText(this, "Reminder updated!", Toast.LENGTH_SHORT).show();
                    } else if (index == 1) {
                        schedule.setStatus(Constants.STATUS_CONFIRMED);
                        updateStatusInDb(schedule.getId(), Constants.STATUS_CONFIRMED);
                        NotificationHelper.scheduleOrUpdateReminder(this, schedule);
                        NotificationHelper.sendInstantNotification(
                                this,
                                "Schedule Confirmed",
                                "Your study session on " + schedule.getStudyDate() + " (" + schedule.getStartTime() + ") is Confirmed."
                        );
                        refreshSchedulesByMode();
                    } else if (index == 2) {
                        schedule.setStatus(Constants.STATUS_CANCELLED);
                        updateStatusInDb(schedule.getId(), Constants.STATUS_CANCELLED);
                        NotificationHelper.scheduleOrUpdateReminder(this, schedule);
                        refreshSchedulesByMode();
                        Toast.makeText(this, "Schedule cancelled & reminder removed!", Toast.LENGTH_SHORT).show();
                    } else if (index == 3) {
                        Intent intent = new Intent(this, ScheduleBookingActivity.class);
                        intent.putExtra(Constants.EXTRA_SCHEDULE_ID, schedule.getId());
                        intent.putExtra(Constants.EXTRA_SELECTED_DATE, schedule.getStudyDate());
                        startActivity(intent);
                    } else if (index == 4) {
                        NotificationHelper.cancelReminder(this, schedule.getId());
                        deleteScheduleInDb(schedule.getId());
                        NotificationHelper.sendInstantNotification(
                                this,
                                "Schedule Deleted",
                                "Removed study session on " + schedule.getStudyDate() + " (" + schedule.getStartTime() + ") and cancelled its reminder."
                        );
                        refreshSchedulesByMode();
                        Toast.makeText(this, "Schedule deleted & reminder cancelled!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private class CalendarScheduleListAdapter extends RecyclerView.Adapter<CalendarScheduleListAdapter.ViewHolder> {

        private List<StudySchedule> schedules = new ArrayList<>();

        void setSchedules(List<StudySchedule> list) {
            this.schedules = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudySchedule item = schedules.get(position);
            String subjectTitle = (item.getSubjectCode() != null ? item.getSubjectCode() + " - " : "")
                    + (item.getSubjectName() != null ? item.getSubjectName() : "Subject #" + item.getSubjectId());

            holder.tvScheduleSubject.setText(subjectTitle);
            holder.tvScheduleDateTime.setText("Date: " + item.getStudyDate()
                    + " | Time: " + item.getStartTime() + " - " + item.getEndTime());
            holder.tvScheduleLocationStatus.setText("Location: " + item.getLocation()
                    + " | Status: " + item.getStatus());

            holder.btnChangeStatus.setText("Details");
            holder.btnEditSchedule.setText("Remind");

            holder.itemView.setOnClickListener(v -> showScheduleDetailsDialog(item));
            holder.btnChangeStatus.setOnClickListener(v -> showScheduleDetailsDialog(item));

            holder.btnEditSchedule.setOnClickListener(v -> {
                NotificationHelper.scheduleOrUpdateReminder(CalendarScheduleActivity.this, item);
                NotificationHelper.sendInstantNotification(
                        CalendarScheduleActivity.this,
                        "Reminder Active: " + subjectTitle,
                        item.getStudyDate() + " (" + item.getStartTime() + " - " + item.getEndTime() + ") at " + item.getLocation()
                );
                Toast.makeText(CalendarScheduleActivity.this, "Reminder set for " + subjectTitle, Toast.LENGTH_SHORT).show();
            });

            holder.btnDeleteSchedule.setOnClickListener(v -> {
                new AlertDialog.Builder(CalendarScheduleActivity.this)
                        .setTitle("Cancel / Delete Schedule")
                        .setMessage("Delete this session and cancel its reminder?")
                        .setPositiveButton("Delete", (d, w) -> {
                            NotificationHelper.cancelReminder(CalendarScheduleActivity.this, item.getId());
                            deleteScheduleInDb(item.getId());
                            NotificationHelper.sendInstantNotification(
                                    CalendarScheduleActivity.this,
                                    "Schedule Deleted",
                                    "Deleted " + subjectTitle + " on " + item.getStudyDate() + " and cancelled reminder."
                            );
                            refreshSchedulesByMode();
                        })
                        .setNegativeButton("Close", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return schedules.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvScheduleSubject, tvScheduleDateTime, tvScheduleLocationStatus;
            Button btnChangeStatus, btnEditSchedule, btnDeleteSchedule;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvScheduleSubject = itemView.findViewById(R.id.tvScheduleSubject);
                tvScheduleDateTime = itemView.findViewById(R.id.tvScheduleDateTime);
                tvScheduleLocationStatus = itemView.findViewById(R.id.tvScheduleLocationStatus);
                btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
                btnEditSchedule = itemView.findViewById(R.id.btnEditSchedule);
                btnDeleteSchedule = itemView.findViewById(R.id.btnDeleteSchedule);
            }
        }
    }
}

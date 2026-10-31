package com.example.studyschedule.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.MainActivity;
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
 * Thiết kế chuẩn 100% theo màn hình "05 · Lịch & thông báo" (STUDY PLANNER):
 * - Thanh chuyển chế độ Ngày / Tuần / Tháng
 * - Điều hướng khoảng ngày (< 02 - 08/11/2026 >) & Thanh 7 ngày trong tuần (T2 02 .. CN 08)
 * - Thẻ lịch học dạng Timeline (08:00 - 09:30 | Cơ sở dữ liệu | Thư viện A2 | Đã xác nhận)
 * - Chọn lịch để xem chi tiết, đặt nhắc lịch sắp đến, cập nhật hoặc hủy nhắc khi lịch thay đổi/hủy/xóa
 * - Khu vực Thông báo ("Sắp đến giờ học", "Lịch học đã cập nhật") & Thanh điều hướng dưới cùng
 */
public class CalendarScheduleActivity extends AppCompatActivity {

    private static final int MODE_DAILY = 0;
    private static final int MODE_WEEKLY = 1;
    private static final int MODE_MONTHLY = 2;
    private static final int REQ_NOTIFICATION_PERMISSION = 101;

    private CardView cardTabDay, cardTabWeek, cardTabMonth;
    private TextView tvTabDay, tvTabWeek, tvTabMonth;
    private TextView btnPrevPeriod, btnNextPeriod, tvPeriodRange;
    private LinearLayout layoutWeekStrip;
    private CalendarView calendarView;
    private TextView tvSelectedPeriod, tvEmptyCalendar, tvBellBadgeCount;
    private RecyclerView rvCalendarSchedules;

    // Khu vực Thông báo
    private CardView cardNotifUpcoming, cardNotifUpdated;
    private TextView tvNotifUpcomingTitle, tvNotifUpcomingTime, tvNotifUpcomingBody;
    private TextView tvNotifUpdatedTitle, tvNotifUpdatedTime, tvNotifUpdatedBody;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private TimelineScheduleAdapter timelineAdapter;

    // Mặc định hiển thị ngày 2026-11-05 (Thứ Năm, 05/11/2026) khớp với thiết kế UI mẫu
    private String selectedDate = "2026-11-05";
    private int currentViewMode = MODE_WEEKLY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_schedule);

        dbHelper = DBHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        ensureSampleScheduleExists();
        requestNotificationPermissionIfNeeded();
        NotificationHelper.createNotificationChannel(this);

        bindViews();
        setupListeners();

        updateTabStyles();
        refreshAllUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAllUI();
        List<StudySchedule> allSchedules = querySchedulesByRange(getCurrentStudentId(), "0000-01-01", "9999-12-31");
        NotificationHelper.syncAllReminders(this, allSchedules, false);
    }

    private void bindViews() {
        cardTabDay = findViewById(R.id.cardTabDay);
        cardTabWeek = findViewById(R.id.cardTabWeek);
        cardTabMonth = findViewById(R.id.cardTabMonth);
        tvTabDay = findViewById(R.id.tvTabDay);
        tvTabWeek = findViewById(R.id.tvTabWeek);
        tvTabMonth = findViewById(R.id.tvTabMonth);

        btnPrevPeriod = findViewById(R.id.btnPrevPeriod);
        btnNextPeriod = findViewById(R.id.btnNextPeriod);
        tvPeriodRange = findViewById(R.id.tvPeriodRange);
        layoutWeekStrip = findViewById(R.id.layoutWeekStrip);
        calendarView = findViewById(R.id.calendarView);

        tvSelectedPeriod = findViewById(R.id.tvSelectedPeriod);
        tvEmptyCalendar = findViewById(R.id.tvEmptyCalendar);
        tvBellBadgeCount = findViewById(R.id.tvBellBadgeCount);
        rvCalendarSchedules = findViewById(R.id.rvCalendarSchedules);

        cardNotifUpcoming = findViewById(R.id.cardNotifUpcoming);
        cardNotifUpdated = findViewById(R.id.cardNotifUpdated);
        tvNotifUpcomingTitle = findViewById(R.id.tvNotifUpcomingTitle);
        tvNotifUpcomingTime = findViewById(R.id.tvNotifUpcomingTime);
        tvNotifUpcomingBody = findViewById(R.id.tvNotifUpcomingBody);
        tvNotifUpdatedTitle = findViewById(R.id.tvNotifUpdatedTitle);
        tvNotifUpdatedTime = findViewById(R.id.tvNotifUpdatedTime);
        tvNotifUpdatedBody = findViewById(R.id.tvNotifUpdatedBody);

        rvCalendarSchedules.setLayoutManager(new LinearLayoutManager(this));
        timelineAdapter = new TimelineScheduleAdapter();
        rvCalendarSchedules.setAdapter(timelineAdapter);
    }

    private void setupListeners() {
        cardTabDay.setOnClickListener(v -> {
            currentViewMode = MODE_DAILY;
            updateTabStyles();
            refreshAllUI();
        });

        cardTabWeek.setOnClickListener(v -> {
            currentViewMode = MODE_WEEKLY;
            updateTabStyles();
            refreshAllUI();
        });

        cardTabMonth.setOnClickListener(v -> {
            currentViewMode = MODE_MONTHLY;
            updateTabStyles();
            refreshAllUI();
        });

        btnPrevPeriod.setOnClickListener(v -> shiftSelectedDate(-1));
        btnNextPeriod.setOnClickListener(v -> shiftSelectedDate(1));

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            refreshAllUI();
        });

        View btnMenuDrawer = findViewById(R.id.btnMenuDrawer);
        if (btnMenuDrawer != null) {
            btnMenuDrawer.setOnClickListener(this::showMenuPopup);
        }

        FrameLayout btnBell = findViewById(R.id.btnBellNotification);
        TextView btnSeeAllNotif = findViewById(R.id.btnSyncReminders);
        View.OnClickListener reminderSyncClick = v -> triggerAndShowAllNotifications();
        btnBell.setOnClickListener(reminderSyncClick);
        btnSeeAllNotif.setOnClickListener(reminderSyncClick);

        cardNotifUpcoming.setOnClickListener(v -> {
            NotificationHelper.sendInstantNotification(
                    this,
                    tvNotifUpcomingTitle.getText().toString(),
                    tvNotifUpcomingBody.getText().toString()
            );
            Toast.makeText(this, "Đã gửi thông báo nhắc lịch lên thanh trạng thái!", Toast.LENGTH_SHORT).show();
        });

        cardNotifUpdated.setOnClickListener(v -> {
            NotificationHelper.sendInstantNotification(
                    this,
                    tvNotifUpdatedTitle.getText().toString(),
                    tvNotifUpdatedBody.getText().toString()
            );
            Toast.makeText(this, "Đã gửi thông báo cập nhật lịch!", Toast.LENGTH_SHORT).show();
        });

        View btnBookDate = findViewById(R.id.btnBookOnSelectedDate);
        btnBookDate.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleBookingActivity.class);
            intent.putExtra(Constants.EXTRA_STUDENT_ID, getCurrentStudentId());
            intent.putExtra(Constants.EXTRA_SELECTED_DATE, selectedDate);
            startActivity(intent);
        });

        View navNotifications = findViewById(R.id.navNotifications);
        navNotifications.setOnClickListener(reminderSyncClick);

        View navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v -> showProfileDialog());
    }

    private void showMenuPopup(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Màn hình chính (Home)");
        menu.getMenu().add(0, 2, 1, "Danh sách lịch học");
        menu.getMenu().add(0, 3, 2, "Đặt lịch học mới");
        menu.getMenu().add(0, 4, 3, "Tài khoản / Đăng xuất");
        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == 2) {
                startActivity(new Intent(this, ScheduleListActivity.class)
                        .putExtra(Constants.EXTRA_STUDENT_ID, getCurrentStudentId()));
            } else if (id == 3) {
                startActivity(new Intent(this, ScheduleBookingActivity.class)
                        .putExtra(Constants.EXTRA_STUDENT_ID, getCurrentStudentId())
                        .putExtra(Constants.EXTRA_SELECTED_DATE, selectedDate));
            } else {
                showProfileDialog();
            }
            return true;
        });
        menu.show();
    }

    private void showProfileDialog() {
        String user = sessionManager.getUsername().isEmpty() ? "sv01 (Nguyễn Minh Anh)" : sessionManager.getUsername();
        String role = sessionManager.getRole().isEmpty() ? Constants.ROLE_STUDENT : sessionManager.getRole();
        int studentId = getCurrentStudentId();
        new AlertDialog.Builder(this)
                .setTitle("Thông tin tài khoản")
                .setMessage("Tài khoản: " + user + "\nMã SV (ID): " + studentId + "\nVai trò: " + role)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Về Trang chủ", (d, w) -> startActivity(new Intent(this, MainActivity.class)))
                .setNegativeButton("Đăng xuất", (d, w) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    /**
     * Cập nhật màu sắc 3 tab Ngày | Tuần | Tháng đúng theo mockup (#1A2138 khi được chọn).
     */
    private void updateTabStyles() {
        int activeBg = Color.parseColor("#1A2138");
        int inactiveBg = Color.TRANSPARENT;
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#475467");

        cardTabDay.setCardBackgroundColor(currentViewMode == MODE_DAILY ? activeBg : inactiveBg);
        tvTabDay.setTextColor(currentViewMode == MODE_DAILY ? activeText : inactiveText);

        cardTabWeek.setCardBackgroundColor(currentViewMode == MODE_WEEKLY ? activeBg : inactiveBg);
        tvTabWeek.setTextColor(currentViewMode == MODE_WEEKLY ? activeText : inactiveText);

        cardTabMonth.setCardBackgroundColor(currentViewMode == MODE_MONTHLY ? activeBg : inactiveBg);
        tvTabMonth.setTextColor(currentViewMode == MODE_MONTHLY ? activeText : inactiveText);

        calendarView.setVisibility(currentViewMode == MODE_MONTHLY ? View.VISIBLE : View.GONE);
        layoutWeekStrip.setVisibility(currentViewMode == MODE_MONTHLY ? View.GONE : View.VISIBLE);
    }

    /**
     * Chuyển sang Ngày / Tuần / Tháng trước hoặc sau khi bấm mũi tên < >.
     */
    private void shiftSelectedDate(int direction) {
        Calendar cal = parseCalendar(selectedDate);
        if (currentViewMode == MODE_DAILY) {
            cal.add(Calendar.DAY_OF_MONTH, direction);
        } else if (currentViewMode == MODE_WEEKLY) {
            cal.add(Calendar.WEEK_OF_YEAR, direction);
        } else {
            cal.add(Calendar.MONTH, direction);
        }
        selectedDate = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US).format(cal.getTime());
        calendarView.setDate(cal.getTimeInMillis(), false, true);
        refreshAllUI();
    }

    /**
     * Cập nhật toàn bộ giao diện: khoảng thời gian, thanh 7 ngày (T2..CN), danh sách lịch học và thẻ thông báo.
     */
    private void refreshAllUI() {
        int studentId = getCurrentStudentId();
        String[] weekBounds = getWeekBounds(selectedDate);
        String[] monthBounds = getMonthBounds(selectedDate);

        renderWeekStrip(weekBounds[0]);

        List<StudySchedule> schedulesToShow;
        if (currentViewMode == MODE_DAILY) {
            tvPeriodRange.setText(formatDisplayDateShort(selectedDate));
            tvSelectedPeriod.setText(formatVietnameseFullDate(selectedDate));
            schedulesToShow = querySchedulesByRange(studentId, selectedDate, selectedDate);
        } else if (currentViewMode == MODE_WEEKLY) {
            tvPeriodRange.setText(formatWeekRangeLabel(weekBounds[0], weekBounds[1]));
            tvSelectedPeriod.setText(formatVietnameseFullDate(selectedDate));
            // Khi ở tab Tuần, hiển thị lịch của ngày đang chọn trên thanh T2..CN, nếu ngày đó trống thì hiển thị cả tuần
            schedulesToShow = querySchedulesByRange(studentId, selectedDate, selectedDate);
            if (schedulesToShow.isEmpty()) {
                tvSelectedPeriod.setText("Lịch học trong tuần (" + formatWeekRangeLabel(weekBounds[0], weekBounds[1]) + ")");
                schedulesToShow = querySchedulesByRange(studentId, weekBounds[0], weekBounds[1]);
            }
        } else {
            Calendar cal = parseCalendar(selectedDate);
            tvPeriodRange.setText(String.format(Locale.US, "Tháng %02d/%04d",
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
            tvSelectedPeriod.setText("Lịch học tháng " + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
            schedulesToShow = querySchedulesByRange(studentId, monthBounds[0], monthBounds[1]);
        }

        timelineAdapter.setSchedules(schedulesToShow);
        tvEmptyCalendar.setVisibility(schedulesToShow.isEmpty() ? View.VISIBLE : View.GONE);

        updateNotificationCards();
    }

    /**
     * Vẽ thanh 7 ngày trong tuần (T2 02, T3 03, T4 04, T5 05, T6 06, T7 07, CN 08) giống hệt thiết kế.
     */
    private void renderWeekStrip(String mondayDateStr) {
        layoutWeekStrip.removeAllViews();
        Calendar cal = parseCalendar(mondayDateStr);
        SimpleDateFormat isoFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        String[] dayLabels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        for (int i = 0; i < 7; i++) {
            final String dayIso = isoFormat.format(cal.getTime());
            final String dayNumber = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH));
            boolean isSelected = dayIso.equals(selectedDate);

            LinearLayout pill = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dpToPx(64), 1f);
            params.setMargins(dpToPx(3), 0, dpToPx(3), 0);
            pill.setLayoutParams(params);
            pill.setOrientation(LinearLayout.VERTICAL);
            pill.setGravity(Gravity.CENTER);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dpToPx(12));
            bg.setColor(isSelected ? Color.parseColor("#1A2138") : Color.parseColor("#F4F6FA"));
            pill.setBackground(bg);

            TextView tvDayName = new TextView(this);
            tvDayName.setText(dayLabels[i]);
            tvDayName.setTextSize(12f);
            tvDayName.setTextColor(isSelected ? Color.parseColor("#CBD5E1") : Color.parseColor("#667085"));
            tvDayName.setGravity(Gravity.CENTER);

            TextView tvDayNum = new TextView(this);
            tvDayNum.setText(dayNumber);
            tvDayNum.setTextSize(15f);
            tvDayNum.setTypeface(null, Typeface.BOLD);
            tvDayNum.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#1A2138"));
            tvDayNum.setGravity(Gravity.CENTER);

            pill.addView(tvDayName);
            pill.addView(tvDayNum);

            pill.setOnClickListener(v -> {
                selectedDate = dayIso;
                refreshAllUI();
            });

            layoutWeekStrip.addView(pill);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    /**
     * Cập nhật 2 thẻ Thông báo ("Sắp đến giờ học" và "Lịch học đã cập nhật") theo dữ liệu thực trong DB.
     */
    private void updateNotificationCards() {
        List<StudySchedule> all = querySchedulesByRange(getCurrentStudentId(), "0000-01-01", "9999-12-31");
        int activeCount = 0;
        StudySchedule firstConfirmed = null;

        for (StudySchedule s : all) {
            if (!Constants.STATUS_CANCELLED.equalsIgnoreCase(s.getStatus())) {
                activeCount++;
                if (firstConfirmed == null) {
                    firstConfirmed = s;
                }
            }
        }

        tvBellBadgeCount.setText(String.valueOf(Math.max(1, activeCount)));

        if (firstConfirmed != null) {
            String subName = firstConfirmed.getSubjectName() != null ? firstConfirmed.getSubjectName() : "Cơ sở dữ liệu";
            tvNotifUpcomingTitle.setText("Sắp đến giờ học");
            tvNotifUpcomingTime.setText("30 phút trước");
            tvNotifUpcomingBody.setText(subName + " bắt đầu lúc " + firstConfirmed.getStartTime()
                    + " tại " + firstConfirmed.getLocation() + ".");
        }
    }

    private void triggerAndShowAllNotifications() {
        List<StudySchedule> allSchedules = querySchedulesByRange(getCurrentStudentId(), "0000-01-01", "9999-12-31");
        int count = NotificationHelper.syncAllReminders(this, allSchedules, true);

        StringBuilder sb = new StringBuilder();
        sb.append("• Sắp đến giờ học (30 phút trước):\n  ")
                .append(tvNotifUpcomingBody.getText()).append("\n\n")
                .append("• ").append(tvNotifUpdatedTitle.getText()).append(" (").append(tvNotifUpdatedTime.getText()).append("):\n  ")
                .append(tvNotifUpdatedBody.getText()).append("\n\n")
                .append("Đã đồng bộ nhắc lịch (AlarmManager) cho ").append(count).append(" buổi học sắp tới.");

        new AlertDialog.Builder(this)
                .setTitle("Danh sách Thông báo & Nhắc lịch")
                .setMessage(sb.toString())
                .setPositiveButton("Bắn thông báo ngay", (d, w) -> {
                    NotificationHelper.sendInstantNotification(
                            this,
                            tvNotifUpcomingTitle.getText().toString(),
                            tvNotifUpcomingBody.getText().toString()
                    );
                    Toast.makeText(this, "Đã gửi thông báo nhắc lịch!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    /**
     * Mở hộp thoại Xem chi tiết lịch học & Quản lý nhắc lịch khi người dùng bấm vào thẻ lịch học.
     */
    private void showScheduleDetailsDialog(StudySchedule schedule) {
        String subjectTitle = schedule.getSubjectName() != null
                ? schedule.getSubjectName() + (schedule.getSubjectCode() != null ? " (" + schedule.getSubjectCode() + ")" : "")
                : "Môn học #" + schedule.getSubjectId();

        String statusVi = toVietnameseStatus(schedule.getStatus());
        String details = "Môn học: " + subjectTitle + "\n\n"
                + "Ngày học: " + formatDisplayDateShort(schedule.getStudyDate()) + "\n"
                + "Thời gian: " + schedule.getStartTime() + " - " + schedule.getEndTime() + "\n"
                + "Địa điểm: " + schedule.getLocation() + "\n"
                + "Trạng thái: " + statusVi;

        String[] actions = {
                "🔔 Bật / Gửi nhắc lịch sắp đến ( trước 15 phút )",
                "✅ Xác nhận lịch học (Đã xác nhận & Cập nhật nhắc)",
                "⏳ Chuyển sang Chờ xác nhận (Pending)",
                "❌ Hủy lịch học (Đã hủy & Hủy nhắc lịch)",
                "🗑️ Xóa lịch học (Xóa & Hủy nhắc lịch)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết lịch học")
                .setMessage(details)
                .setPositiveButton("Nhắc lịch ngay", (dialog, which) -> {
                    NotificationHelper.scheduleOrUpdateReminder(this, schedule);
                    NotificationHelper.sendInstantNotification(
                            this,
                            "Sắp đến giờ học: " + (schedule.getSubjectName() != null ? schedule.getSubjectName() : ""),
                            "Bắt đầu lúc " + schedule.getStartTime() + " (" + formatDisplayDateShort(schedule.getStudyDate())
                                    + ") tại " + schedule.getLocation()
                    );
                    Toast.makeText(this, "Đã đặt hẹn giờ và gửi thông báo nhắc lịch!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cập nhật / Hủy nhắc", (dialog, which) ->
                        showManageScheduleOptions(schedule, actions))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showManageScheduleOptions(StudySchedule schedule, String[] actions) {
        String subName = schedule.getSubjectName() != null ? schedule.getSubjectName() : "Môn học";
        new AlertDialog.Builder(this)
                .setTitle("Quản lý lịch & Nhắc nhở")
                .setItems(actions, (dialog, index) -> {
                    if (index == 0) {
                        NotificationHelper.scheduleOrUpdateReminder(this, schedule);
                        NotificationHelper.sendInstantNotification(
                                this,
                                "Sắp đến giờ học",
                                subName + " bắt đầu lúc " + schedule.getStartTime() + " tại " + schedule.getLocation() + "."
                        );
                        Toast.makeText(this, "Đã bật nhắc lịch trước giờ học!", Toast.LENGTH_SHORT).show();
                    } else if (index == 1) {
                        schedule.setStatus(Constants.STATUS_CONFIRMED);
                        updateStatusInDb(schedule.getId(), Constants.STATUS_CONFIRMED);
                        NotificationHelper.scheduleOrUpdateReminder(this, schedule);

                        tvNotifUpdatedTitle.setText("Lịch học đã cập nhật");
                        tvNotifUpdatedTime.setText("Vừa xong");
                        tvNotifUpdatedBody.setText("Lịch học môn " + subName + " đã được xác nhận.");
                        NotificationHelper.sendInstantNotification(this, "Lịch học đã cập nhật", tvNotifUpdatedBody.getText().toString());
                        refreshAllUI();
                    } else if (index == 2) {
                        schedule.setStatus(Constants.STATUS_PENDING);
                        updateStatusInDb(schedule.getId(), Constants.STATUS_PENDING);
                        NotificationHelper.scheduleOrUpdateReminder(this, schedule);

                        tvNotifUpdatedTitle.setText("Lịch học đã cập nhật");
                        tvNotifUpdatedTime.setText("Vừa xong");
                        tvNotifUpdatedBody.setText("Lịch học môn " + subName + " đang chờ xác nhận.");
                        refreshAllUI();
                    } else if (index == 3) {
                        schedule.setStatus(Constants.STATUS_CANCELLED);
                        updateStatusInDb(schedule.getId(), Constants.STATUS_CANCELLED);
                        NotificationHelper.cancelReminder(this, schedule.getId());

                        tvNotifUpdatedTitle.setText("Đã hủy nhắc lịch");
                        tvNotifUpdatedTime.setText("Vừa xong");
                        tvNotifUpdatedBody.setText("Lịch học môn " + subName + " đã hủy và tắt thông báo nhắc.");
                        NotificationHelper.sendInstantNotification(this, "Đã hủy lịch & tắt nhắc nhở", tvNotifUpdatedBody.getText().toString());
                        refreshAllUI();
                        Toast.makeText(this, "Đã hủy lịch học và hủy hẹn giờ nhắc!", Toast.LENGTH_SHORT).show();
                    } else if (index == 4) {
                        NotificationHelper.cancelReminder(this, schedule.getId());
                        deleteScheduleInDb(schedule.getId());

                        tvNotifUpdatedTitle.setText("Đã xóa lịch học");
                        tvNotifUpdatedTime.setText("Vừa xong");
                        tvNotifUpdatedBody.setText("Đã xóa lịch môn " + subName + " và hủy thông báo nhắc.");
                        NotificationHelper.sendInstantNotification(this, "Đã xóa lịch học", tvNotifUpdatedBody.getText().toString());
                        refreshAllUI();
                        Toast.makeText(this, "Đã xóa lịch học và hủy nhắc lịch!", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Đảm bảo luôn có dữ liệu mẫu ngày 05/11/2026 ("Cơ sở dữ liệu", "08:00 - 09:30", "Thư viện A2")
     * để khi chạy độc lập nhánh của bạn lên là có sẵn đúng giao diện như hình mẫu.
     */
    private void ensureSampleScheduleExists() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_SCHEDULE
                + " WHERE " + DBHelper.COL_SCH_DATE + " = '2026-11-05'", null)) {
            if (c != null && c.moveToFirst() && c.getInt(0) == 0) {
                db.execSQL("INSERT OR IGNORE INTO " + DBHelper.TABLE_SUBJECT
                        + " (id, subjectCode, subjectName, description, credits) VALUES (1, 'INT1306', 'Cơ sở dữ liệu', 'CSDL SQL', 3)");
                db.execSQL("INSERT OR IGNORE INTO " + DBHelper.TABLE_SUBJECT
                        + " (id, subjectCode, subjectName, description, credits) VALUES (2, 'INT1310', 'Lập trình Java', 'Java OOP', 3)");
                db.execSQL("INSERT INTO " + DBHelper.TABLE_SCHEDULE
                        + " (studentId, subjectId, studyDate, startTime, endTime, location, status) "
                        + "VALUES (1, 1, '2026-11-05', '08:00', '09:30', 'Thư viện A2', '" + Constants.STATUS_CONFIRMED + "')");
            }
        } catch (Exception ignored) {
        }
    }

    private int getCurrentStudentId() {
        int fromIntent = getIntent().getIntExtra(Constants.EXTRA_STUDENT_ID, -1);
        if (fromIntent > 0) return fromIntent;
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

    private Calendar parseCalendar(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        Calendar cal = Calendar.getInstance();
        try {
            Date d = sdf.parse(dateStr);
            if (d != null) cal.setTime(d);
        } catch (ParseException ignored) {
        }
        return cal;
    }

    private String[] getWeekBounds(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        Calendar cal = parseCalendar(dateStr);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int diffToMonday = (dow == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dow);
        cal.add(Calendar.DAY_OF_MONTH, diffToMonday);
        String startOfWeek = sdf.format(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, 6);
        String endOfWeek = sdf.format(cal.getTime());
        return new String[]{startOfWeek, endOfWeek};
    }

    private String[] getMonthBounds(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        Calendar cal = parseCalendar(dateStr);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startOfMonth = sdf.format(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String endOfMonth = sdf.format(cal.getTime());
        return new String[]{startOfMonth, endOfMonth};
    }

    private String formatWeekRangeLabel(String startIso, String endIso) {
        Calendar c1 = parseCalendar(startIso);
        Calendar c2 = parseCalendar(endIso);
        return String.format(Locale.US, "%02d – %02d/%02d/%04d",
                c1.get(Calendar.DAY_OF_MONTH),
                c2.get(Calendar.DAY_OF_MONTH),
                c2.get(Calendar.MONTH) + 1,
                c2.get(Calendar.YEAR));
    }

    private String formatDisplayDateShort(String isoDate) {
        Calendar c = parseCalendar(isoDate);
        return String.format(Locale.US, "%02d/%02d/%04d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));
    }

    private String formatVietnameseFullDate(String isoDate) {
        Calendar c = parseCalendar(isoDate);
        String[] dowNames = {"Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        String dowName = dowNames[c.get(Calendar.DAY_OF_WEEK) - 1];
        return String.format(Locale.US, "%s, %02d/%02d/%04d",
                dowName,
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));
    }

    private String toVietnameseStatus(String status) {
        if (Constants.STATUS_CONFIRMED.equalsIgnoreCase(status)) return "Đã xác nhận";
        if (Constants.STATUS_CANCELLED.equalsIgnoreCase(status)) return "Đã hủy";
        return "Chờ xác nhận";
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /**
     * Adapter dựng thẻ Timeline giống hệt thiết kế "05 · Lịch & thông báo" (08:00 / 09:30 | Cơ sở dữ liệu | Thư viện A2 | Đã xác nhận)
     * mà không cần chỉnh sửa file item_schedule.xml của Người 4.
     */
    private class TimelineScheduleAdapter extends RecyclerView.Adapter<TimelineScheduleAdapter.TimelineViewHolder> {

        private List<StudySchedule> schedules = new ArrayList<>();

        void setSchedules(List<StudySchedule> list) {
            this.schedules = list != null ? list : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView card = new CardView(parent.getContext());
            RecyclerView.LayoutParams cardParams = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dpToPx(10));
            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(16));
            card.setCardElevation(0f);
            card.setCardBackgroundColor(Color.parseColor("#EEF4FF"));

            LinearLayout root = new LinearLayout(parent.getContext());
            root.setOrientation(LinearLayout.HORIZONTAL);
            root.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            root.setGravity(Gravity.CENTER_VERTICAL);

            // Cột trái: 08:00 / 09:30
            LinearLayout timeCol = new LinearLayout(parent.getContext());
            timeCol.setOrientation(LinearLayout.VERTICAL);
            timeCol.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(52), ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView tvStart = new TextView(parent.getContext());
            tvStart.setTextSize(15f);
            tvStart.setTypeface(null, Typeface.BOLD);
            tvStart.setTextColor(Color.parseColor("#1A2138"));

            TextView tvEnd = new TextView(parent.getContext());
            tvEnd.setTextSize(13f);
            tvEnd.setTextColor(Color.parseColor("#667085"));
            LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            endParams.topMargin = dpToPx(6);
            tvEnd.setLayoutParams(endParams);

            timeCol.addView(tvStart);
            timeCol.addView(tvEnd);

            // Cột giữa: Dấu chấm tròn + Đường kẻ dọc màu xanh (#3B82F6)
            LinearLayout lineCol = new LinearLayout(parent.getContext());
            lineCol.setOrientation(LinearLayout.VERTICAL);
            lineCol.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams lineColParams = new LinearLayout.LayoutParams(dpToPx(20), ViewGroup.LayoutParams.MATCH_PARENT);
            lineColParams.setMargins(dpToPx(4), 0, dpToPx(10), 0);
            lineCol.setLayoutParams(lineColParams);

            View dot = new View(parent.getContext());
            GradientDrawable dotDrawable = new GradientDrawable();
            dotDrawable.setShape(GradientDrawable.OVAL);
            dotDrawable.setColor(Color.parseColor("#2563EB"));
            dot.setBackground(dotDrawable);
            dot.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(9), dpToPx(9)));

            View verticalBar = new View(parent.getContext());
            verticalBar.setBackgroundColor(Color.parseColor("#60A5FA"));
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(2), dpToPx(42));
            barParams.topMargin = dpToPx(2);
            verticalBar.setLayoutParams(barParams);

            lineCol.addView(dot);
            lineCol.addView(verticalBar);

            // Cột phải: Tên môn học + Địa điểm + Badge trạng thái
            LinearLayout infoCol = new LinearLayout(parent.getContext());
            infoCol.setOrientation(LinearLayout.VERTICAL);
            infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvSubject = new TextView(parent.getContext());
            tvSubject.setTextSize(16f);
            tvSubject.setTypeface(null, Typeface.BOLD);
            tvSubject.setTextColor(Color.parseColor("#1A2138"));

            LinearLayout bottomRow = new LinearLayout(parent.getContext());
            bottomRow.setOrientation(LinearLayout.HORIZONTAL);
            bottomRow.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams bottomRowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bottomRowParams.topMargin = dpToPx(6);
            bottomRow.setLayoutParams(bottomRowParams);

            TextView tvLocation = new TextView(parent.getContext());
            tvLocation.setTextSize(13f);
            tvLocation.setTextColor(Color.parseColor("#475467"));
            tvLocation.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvStatusBadge = new TextView(parent.getContext());
            tvStatusBadge.setTextSize(11f);
            tvStatusBadge.setTypeface(null, Typeface.BOLD);
            tvStatusBadge.setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4));

            bottomRow.addView(tvLocation);
            bottomRow.addView(tvStatusBadge);

            infoCol.addView(tvSubject);
            infoCol.addView(bottomRow);

            root.addView(timeCol);
            root.addView(lineCol);
            root.addView(infoCol);
            card.addView(root);

            return new TimelineViewHolder(card, tvStart, tvEnd, tvSubject, tvLocation, tvStatusBadge);
        }

        @Override
        public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
            StudySchedule item = schedules.get(position);
            String subjectName = item.getSubjectName() != null
                    ? item.getSubjectName()
                    : "Môn học #" + item.getSubjectId();

            holder.tvStart.setText(item.getStartTime());
            holder.tvEnd.setText(item.getEndTime());
            holder.tvSubject.setText(subjectName);
            holder.tvLocation.setText("📍 " + item.getLocation());

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(dpToPx(12));

            if (Constants.STATUS_CONFIRMED.equalsIgnoreCase(item.getStatus())) {
                holder.tvStatusBadge.setText("Đã xác nhận");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#027A48"));
                badgeBg.setColor(Color.parseColor("#D1FADF"));
            } else if (Constants.STATUS_CANCELLED.equalsIgnoreCase(item.getStatus())) {
                holder.tvStatusBadge.setText("Đã hủy");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#B42318"));
                badgeBg.setColor(Color.parseColor("#FEE4E2"));
            } else {
                holder.tvStatusBadge.setText("Chờ xác nhận");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#B54708"));
                badgeBg.setColor(Color.parseColor("#FEF0C7"));
            }
            holder.tvStatusBadge.setBackground(badgeBg);

            holder.itemView.setOnClickListener(v -> showScheduleDetailsDialog(item));
        }

        @Override
        public int getItemCount() {
            return schedules.size();
        }

        class TimelineViewHolder extends RecyclerView.ViewHolder {
            TextView tvStart, tvEnd, tvSubject, tvLocation, tvStatusBadge;

            TimelineViewHolder(@NonNull View itemView, TextView tvStart, TextView tvEnd,
                               TextView tvSubject, TextView tvLocation, TextView tvStatusBadge) {
                super(itemView);
                this.tvStart = tvStart;
                this.tvEnd = tvEnd;
                this.tvSubject = tvSubject;
                this.tvLocation = tvLocation;
                this.tvStatusBadge = tvStatusBadge;
            }
        }
    }
}

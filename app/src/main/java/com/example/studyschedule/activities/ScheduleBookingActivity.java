package com.example.studyschedule.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.MainActivity;
import com.example.studyschedule.R;
import com.example.studyschedule.adapters.ScheduleAdapter;
import com.example.studyschedule.dal.StudyScheduleDAO;
import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.models.Subject;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.NotificationHelper;
import com.example.studyschedule.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Member 4: booking form, availability and editing of a student's own schedules. */
public class ScheduleBookingActivity extends AppCompatActivity
        implements ScheduleAdapter.OnScheduleActionListener {
    private StudyScheduleDAO dao;
    private ScheduleAdapter adapter;
    private Spinner subjectSpinner;
    private TextView dateView, startView, endView, availability;
    private AutoCompleteTextView locationView;
    private Button saveButton;
    private List<Subject> subjects = new ArrayList<>();
    private final Calendar selectedDate = Calendar.getInstance();
    private String startTime = "08:00", endTime = "09:30";
    private int studentId = -1, editingId = -1;
    private String editingStatus = Constants.STATUS_PENDING;

    @Override
    protected void onCreate(Bundle state) {
        useLightTheme(this);
        super.onCreate(state);
        setContentView(R.layout.activity_schedule_booking);
        studentId = resolveStudentId(this);
        if (studentId <= 0) return;
        dao = new StudyScheduleDAO(this);
        subjectSpinner = findViewById(R.id.spnSubject);
        dateView = findViewById(R.id.edtStudyDate);
        startView = findViewById(R.id.edtStartTime);
        endView = findViewById(R.id.edtEndTime);
        locationView = findViewById(R.id.edtLocation);
        availability = findViewById(R.id.tvAvailability);
        saveButton = findViewById(R.id.btnSaveSchedule);
        applyFormAppearance();

        RecyclerView list = findViewById(R.id.rvBookedSchedules);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setNestedScrollingEnabled(false);
        adapter = new ScheduleAdapter(this);
        list.setAdapter(adapter);
        ArrayAdapter<String> locations = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                new String[]{"Thư viện A2", "Phòng học A101", "Phòng học B201", "Học trực tuyến"});
        locationView.setAdapter(locations);
        locationView.setText("Thư viện A2", false);
        locationView.setOnClickListener(v -> locationView.showDropDown());
        dateView.setOnClickListener(v -> pickDate());
        startView.setOnClickListener(v -> pickTime(true));
        endView.setOnClickListener(v -> pickTime(false));
        saveButton.setOnClickListener(v -> saveSchedule());
        findViewById(R.id.btnCancelEditing).setOnClickListener(v -> stopEditing());
        findViewById(R.id.btnViewAllSchedules).setOnClickListener(v -> openSchedules());
        findViewById(R.id.navBookingSchedules).setOnClickListener(v -> openSchedules());
        findViewById(R.id.btnBookingMenu).setOnClickListener(this::showMenu);
        findViewById(R.id.btnBookingProfile).setOnClickListener(v -> showProfile());
        findViewById(R.id.navBookingProfile).setOnClickListener(v -> showProfile());
        // Member 5 owns the destination and notification behavior.
        findViewById(R.id.navBookingNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, CalendarScheduleActivity.class)
                        .putExtra(Constants.EXTRA_STUDENT_ID, studentId)));

        Calendar morning = Calendar.getInstance();
        morning.set(Calendar.HOUR_OF_DAY, 8);
        morning.set(Calendar.MINUTE, 0);
        if (!morning.after(Calendar.getInstance())) selectedDate.add(Calendar.DAY_OF_MONTH, 1);
        try {
            loadSubjects(-1);
            if (state != null) {
                selectedDate.setTimeInMillis(state.getLong("booking_date"));
                startTime = state.getString("booking_start", "08:00");
                endTime = state.getString("booking_end", "09:30");
                editingId = state.getInt("booking_edit", -1);
                editingStatus = state.getString("booking_status", Constants.STATUS_PENDING);
                locationView.setText(state.getString("booking_location", ""), false);
                selectSubject(state.getInt("booking_subject", -1));
            } else if (getIntent().hasExtra(Constants.EXTRA_SCHEDULE_ID)) {
                loadEditing(getIntent().getIntExtra(Constants.EXTRA_SCHEDULE_ID, -1));
            } else {
                String date = getIntent().getStringExtra(Constants.EXTRA_SELECTED_DATE);
                if (StudyScheduleDAO.isValidDate(date)) setDate(date);
                selectSubject(getIntent().getIntExtra(Constants.EXTRA_SUBJECT_ID, -1));
            }
        } catch (RuntimeException e) {
            reportError(e, "Không thể tải dữ liệu lịch học.");
            saveButton.setEnabled(false);
        }
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshAvailability();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        renderForm();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dao == null || adapter == null) return;
        try {
            int selectedId = selectedSubjectId();
            loadSubjects(selectedId);
            refreshPreview();
            refreshAvailability();
        } catch (RuntimeException e) {
            reportError(e, "Không thể tải dữ liệu lịch học.");
            saveButton.setEnabled(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (dao == null) return;
        try {
            if (intent.hasExtra(Constants.EXTRA_SCHEDULE_ID))
                loadEditing(intent.getIntExtra(Constants.EXTRA_SCHEDULE_ID, -1));
            else stopEditing();
        } catch (RuntimeException e) {
            reportError(e, "Không thể mở lịch học.");
        }
    }

    private void loadSubjects(int selectedId) {
        subjects = dao.getBookingSubjects();
        List<String> names = new ArrayList<>();
        for (Subject subject : subjects) names.add(subject.getSubjectName());
        if (names.isEmpty()) names.add("Chưa có môn học");
        ArrayAdapter<String> choices = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names) {
            @NonNull @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.rgb(25, 38, 83));
                view.setTextSize(14);
                return view;
            }
        };
        choices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(choices);
        subjectSpinner.setEnabled(!subjects.isEmpty());
        selectSubject(selectedId);
    }

    private void selectSubject(int id) {
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).getId() == id) {
                subjectSpinner.setSelection(i);
                return;
            }
        }
    }

    private int selectedSubjectId() {
        int position = subjectSpinner.getSelectedItemPosition();
        return position >= 0 && position < subjects.size() ? subjects.get(position).getId() : -1;
    }

    private void pickDate() {
        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, day) -> {
            selectedDate.set(year, month, day);
            renderForm();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        dialog.getDatePicker().setMinDate(today.getTimeInMillis());
        dialog.show();
    }

    private void pickTime(boolean start) {
        String[] parts = (start ? startTime : endTime).split(":");
        new TimePickerDialog(this, (picker, hour, minute) -> {
            String value = String.format(Locale.US, "%02d:%02d", hour, minute);
            if (start) startTime = value; else endTime = value;
            renderForm();
        }, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), true).show();
    }

    private String storageDate() {
        return new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US).format(selectedDate.getTime());
    }

    private void setDate(String date) {
        String[] parts = date.split("-");
        selectedDate.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
    }

    private boolean startsInPast() {
        Calendar start = (Calendar) selectedDate.clone();
        String[] parts = startTime.split(":");
        start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        start.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTimeInMillis() <= System.currentTimeMillis();
    }

    private void renderForm() {
        dateView.setText(ScheduleAdapter.displayDate(storageDate(), true));
        startView.setText(startTime);
        endView.setText(endTime);
        ((TextView) findViewById(R.id.tvBookingTitle)).setText(editingId > 0 ? "Chỉnh sửa lịch học" : "Đặt lịch học");
        saveButton.setText(editingId > 0 ? "Lưu thay đổi" : "Đặt lịch");
        findViewById(R.id.btnCancelEditing).setVisibility(editingId > 0 ? View.VISIBLE : View.GONE);
        refreshAvailability();
    }

    private void refreshAvailability() {
        if (dao == null) return;
        String message = "✓  Khung giờ còn trống";
        boolean valid = false;
        try {
            if (subjects.isEmpty()) message = "Chưa có môn học để đặt lịch.";
            else if (!StudyScheduleDAO.isValidTimeRange(startTime, endTime))
                message = "Giờ kết thúc phải sau giờ bắt đầu.";
            else if (startsInPast()) message = "Vui lòng chọn thời gian trong tương lai.";
            else if (!Constants.STATUS_CANCELLED.equals(editingStatus)
                    && dao.hasScheduleConflict(studentId, storageDate(), startTime, endTime, editingId))
                message = "Khung giờ trùng với một lịch học khác.";
            else {
                valid = true;
                if (Constants.STATUS_CANCELLED.equals(editingStatus)) message = "Lịch đã hủy, không giữ khung giờ.";
            }
        } catch (RuntimeException e) {
            Log.e("ScheduleBooking", "Unable to check availability", e);
            message = "Không thể kiểm tra khung giờ. Vui lòng thử lại.";
        }
        availability.setText(message);
        availability.setTextColor(valid ? Color.rgb(36, 155, 69) : Color.rgb(220, 58, 85));
        saveButton.setEnabled(valid);
        saveButton.setAlpha(valid ? 1f : 0.55f);
    }

    private void saveSchedule() {
        if (selectedSubjectId() <= 0) { toast("Vui lòng chọn môn học."); return; }
        String location = locationView.getText().toString().trim();
        if (location.isEmpty() || location.length() > 120) {
            locationView.setError("Vui lòng nhập địa điểm (tối đa 120 ký tự).");
            locationView.requestFocus();
            return;
        }
        refreshAvailability();
        if (!saveButton.isEnabled()) return;
        try {
            StudySchedule schedule = new StudySchedule(studentId, selectedSubjectId(), storageDate(),
                    startTime, endTime, location, editingStatus);
            schedule.setId(editingId);
            if (editingId > 0 && !ownsSchedule(dao, studentId, editingId)) {
                toast("Lịch học không còn tồn tại.");
                stopEditing();
                return;
            }
            boolean isEditing = editingId > 0;
            long savedId = isEditing ? (dao.updateSchedule(schedule) == 1 ? editingId : -1) : dao.insertSchedule(schedule);
            if (savedId <= 0) {
                toast("Không thể lưu lịch. Vui lòng kiểm tra lại khung giờ.");
                refreshAvailability();
                return;
            }
            schedule.setId((int) savedId);
            int pos = subjectSpinner.getSelectedItemPosition();
            if (pos >= 0 && pos < subjects.size()) {
                schedule.setSubjectName(subjects.get(pos).getSubjectName());
                schedule.setSubjectCode(subjects.get(pos).getSubjectCode());
            }
            NotificationHelper.scheduleOrUpdateReminder(this, schedule);
            NotificationHelper.sendInstantNotification(
                    this,
                    isEditing ? "Lịch học đã cập nhật" : "Đặt lịch học thành công",
                    (schedule.getSubjectName() != null ? schedule.getSubjectName() : "Buổi học")
                            + " lúc " + startTime + " (" + ScheduleAdapter.displayDate(storageDate(), false) + ") tại " + location
            );
            toast(isEditing ? "Đã cập nhật lịch học." : "Đặt lịch học thành công.");
            stopEditing();
            refreshPreview();
        } catch (RuntimeException e) {
            reportError(e, "Không thể lưu lịch học. Vui lòng thử lại.");
        }
    }

    private void loadEditing(int id) {
        StudySchedule item = dao.getScheduleById(id);
        if (item == null || item.getStudentId() != studentId) {
            toast("Lịch học không còn tồn tại.");
            return;
        }
        editingId = item.getId();
        editingStatus = item.getStatus();
        setDate(item.getStudyDate());
        startTime = item.getStartTime();
        endTime = item.getEndTime();
        selectSubject(item.getSubjectId());
        locationView.setText(item.getLocation(), false);
        renderForm();
    }

    private void stopEditing() {
        editingId = -1;
        editingStatus = Constants.STATUS_PENDING;
        getIntent().removeExtra(Constants.EXTRA_SCHEDULE_ID);
        renderForm();
    }

    private void refreshPreview() {
        List<StudySchedule> schedules = dao.getSchedulesByStudent(studentId);
        // Show the next active bookings first, then the full list via "Xem tất cả".
        List<StudySchedule> preview = new ArrayList<>();
        String today = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
        for (StudySchedule item : schedules) {
            if (!Constants.STATUS_CANCELLED.equals(item.getStatus()) && item.getStudyDate().compareTo(today) >= 0)
                preview.add(item);
        }
        if (preview.isEmpty()) preview.addAll(schedules);
        adapter.setScheduleList(preview.subList(0, Math.min(3, preview.size())));
        findViewById(R.id.tvEmptySchedules).setVisibility(schedules.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openSchedules() {
        startActivity(new Intent(this, ScheduleListActivity.class)
                .putExtra(Constants.EXTRA_STUDENT_ID, studentId));
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, "Lịch đã đặt");
        menu.getMenu().add(0, 2, 1, "Đặt lịch mới");
        menu.getMenu().add(0, 3, 2, "Lịch & Thông báo (Calendar)");
        menu.getMenu().add(0, 4, 3, "Màn hình chính (Home)");
        menu.getMenu().add(0, 5, 4, "Cá nhân / Đăng xuất");
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) openSchedules();
            else if (item.getItemId() == 2) stopEditing();
            else if (item.getItemId() == 3) {
                startActivity(new Intent(this, CalendarScheduleActivity.class)
                        .putExtra(Constants.EXTRA_STUDENT_ID, studentId));
            } else if (item.getItemId() == 4) {
                startActivity(new Intent(this, MainActivity.class));
            } else showProfile();
            return true;
        });
        menu.show();
    }

    private void showProfile() {
        SessionManager sm = new SessionManager(this);
        String username = sm.getUsername().isEmpty() ? "sv01" : sm.getUsername();
        String role = sm.getRole().isEmpty() ? Constants.ROLE_STUDENT : sm.getRole();
        new AlertDialog.Builder(this).setTitle("Thông tin tài khoản")
                .setMessage("Tài khoản: " + username
                        + "\nVai trò: " + role
                        + "\nMã sinh viên (ID): " + studentId)
                .setPositiveButton("Đóng", null)
                .setNeutralButton("Về Trang chủ", (d, w) -> startActivity(new Intent(this, MainActivity.class)))
                .setNegativeButton("Đăng xuất", (d, w) -> {
                    sm.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    @Override public void onView(StudySchedule item) {
        ScheduleListActivity.showDetails(this, item, () -> onEdit(item));
    }
    @Override public void onEdit(StudySchedule item) {
        try {
            loadEditing(item.getId());
            ((NestedScrollView) findViewById(R.id.bookingScroll)).smoothScrollTo(0, 0);
        } catch (RuntimeException e) { reportError(e, "Không thể mở lịch học."); }
    }
    @Override public void onDelete(StudySchedule item) {
        ScheduleListActivity.showDeleteDialog(this, dao, studentId, item, this::afterScheduleChanged);
    }
    @Override public void onChangeStatus(StudySchedule item) {
        ScheduleListActivity.showStatusDialog(this, dao, studentId, item, this::afterScheduleChanged);
    }
    private void afterScheduleChanged() {
        try {
            if (editingId > 0) {
                StudySchedule current = dao.getScheduleById(editingId);
                if (current == null) stopEditing();
                else editingStatus = current.getStatus();
            }
            refreshPreview();
            renderForm();
        } catch (RuntimeException e) { reportError(e, "Không thể tải lại lịch học."); }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        if (dao != null) {
            state.putLong("booking_date", selectedDate.getTimeInMillis());
            state.putString("booking_start", startTime);
            state.putString("booking_end", endTime);
            state.putString("booking_location", locationView.getText().toString());
            state.putInt("booking_edit", editingId);
            state.putString("booking_status", editingStatus);
            state.putInt("booking_subject", selectedSubjectId());
        }
        super.onSaveInstanceState(state);
    }

    private void applyFormAppearance() {
        for (int id : new int[]{R.id.fieldSubject, R.id.edtStudyDate, R.id.edtStartTime,
                R.id.edtEndTime, R.id.edtLocation}) styleField(findViewById(id));
        saveButton.setBackground(ScheduleAdapter.rounded(Color.rgb(25, 38, 83), ScheduleAdapter.dp(this, 13)));
        findViewById(R.id.bookingBanner).setBackground(ScheduleAdapter.rounded(Color.rgb(255, 230, 239),
                ScheduleAdapter.dp(this, 16)));
        findViewById(R.id.bookingBanner).setClipToOutline(true);
        findViewById(R.id.navBookingCurrent).setBackground(ScheduleAdapter.rounded(Color.rgb(255, 240, 246),
                ScheduleAdapter.dp(this, 13)));
        ((ImageView) findViewById(R.id.imgBookingBanner)).setImageDrawable(new BookingIllustration());
    }

    static void styleField(View view) {
        GradientDrawable background = ScheduleAdapter.rounded(Color.WHITE, ScheduleAdapter.dp(view.getContext(), 11));
        background.setStroke(ScheduleAdapter.dp(view.getContext(), 1), Color.rgb(225, 230, 240));
        view.setBackgroundTintList(null);
        view.setBackground(background);
        if (view instanceof TextView) {
            // Bound platform icons consistently instead of adding drawable resource files.
            TextView text = (TextView) view;
            Drawable[] icons = text.getCompoundDrawablesRelative();
            for (Drawable icon : icons) {
                if (icon != null) icon.setBounds(0, 0, ScheduleAdapter.dp(view.getContext(), 20),
                        ScheduleAdapter.dp(view.getContext(), 20));
            }
            text.setCompoundDrawablesRelative(icons[0], icons[1], icons[2], icons[3]);
        }
    }

    static void useLightTheme(AppCompatActivity activity) {
        activity.setTheme(com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar);
        activity.getWindow().setStatusBarColor(Color.rgb(252, 252, 255));
        activity.getWindow().setNavigationBarColor(Color.WHITE);
        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                | (android.os.Build.VERSION.SDK_INT >= 26 ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0));
    }

    static int resolveStudentId(AppCompatActivity activity) {
        int fromIntent = activity.getIntent().getIntExtra(Constants.EXTRA_STUDENT_ID, -1);
        if (fromIntent > 0) return fromIntent;
        SessionManager sm = new SessionManager(activity);
        int fromSession = sm.getStudentId();
        return fromSession > 0 ? fromSession : 1;
    }

    static boolean ownsSchedule(StudyScheduleDAO dao, int studentId, int scheduleId) {
        StudySchedule item = dao.getScheduleById(scheduleId);
        return item != null && item.getStudentId() == studentId;
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private void reportError(RuntimeException error, String message) {
        Log.e("ScheduleBooking", message, error);
        toast(message);
    }

    /** Banner artwork stays local to this screen; the screen structure is defined in XML. */
    private static class BookingIllustration extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override public void draw(@NonNull Canvas canvas) {
            canvas.save();
            canvas.translate(getBounds().left, getBounds().top);
            canvas.scale(getBounds().width() / 194f, getBounds().height() / 104f);
            // Calendar.
            box(canvas, 130, 12, 49, 46, 5, "#FFFFFF");
            box(canvas, 130, 12, 49, 12, 4, "#FF9BA7");
            for (int x = 139; x < 176; x += 15) box(canvas, x, 8, 3, 12, 1, "#17394B");
            for (int x = 137; x < 175; x += 11) {
                for (int y = 29; y < 54; y += 10)
                    box(canvas, x, y, 7, 6, 1, x == 159 && y == 39 ? "#F5A4C2" : "#E8E9F4");
            }
            // Leaves.
            leaf(canvas, 178, 99, 181, 47, 187, 34, 197, 73, "#54BA93");
            leaf(canvas, 181, 104, 168, 76, 162, 69, 170, 99, "#72CDA2");
            leaf(canvas, 182, 104, 198, 78, 194, 70, 180, 93, "#54BA93");
            box(canvas, 181, 65, 1.3f, 39, 0, "#2B926F");
            leaf(canvas, 31, 104, 19, 86, 9, 83, 19, 105, "#69C49C");
            leaf(canvas, 33, 104, 30, 76, 20, 69, 22, 101, "#59B78B");
            leaf(canvas, 32, 104, 47, 86, 51, 81, 42, 107, "#A4D8AC");
            // Hair and face.
            oval(canvas, 65, 10, 104, 48, "#123B4B");
            oval(canvas, 55, 29, 84, 67, "#123B4B");
            oval(canvas, 48, 51, 102, 86, "#123B4B");
            box(canvas, 78, 43, 15, 20, 4, "#F2B49A");
            oval(canvas, 73, 23, 100, 51, "#FFD0B5");
            leaf(canvas, 70, 28, 88, 13, 100, 18, 99, 34, "#123B4B");
            box(canvas, 81, 34, 2, 2, 1, "#17394B");
            box(canvas, 92, 33, 2, 2, 1, "#17394B");
            box(canvas, 85, 42, 7, 1.4f, 1, "#C67977");
            // Pink top and arm.
            leaf(canvas, 49, 104, 61, 56, 99, 51, 117, 104, "#F17DA4");
            leaf(canvas, 53, 78, 62, 61, 75, 73, 68, 92, "#FF94B6");
            box(canvas, 53, 89, 48, 13, 6, "#FF94B6");
            box(canvas, 87, 91, 24, 10, 5, "#FFD0B5");
            // Laptop.
            polygon(canvas, 102, 103, 114, 73, 160, 73, 150, 103, "#163C51");
            polygon(canvas, 107, 100, 117, 77, 156, 77, 147, 100, "#285970");
            oval(canvas, 131, 87, 136, 92, "#89BACA");
            box(canvas, 96, 101, 59, 3, 1, "#163C51");
            canvas.restore();
        }
        private void box(Canvas c, float x, float y, float w, float h, float r, String color) {
            paint.setColor(Color.parseColor(color));
            c.drawRoundRect(x, y, x + w, y + h, r, r, paint);
        }
        private void oval(Canvas c, float left, float top, float right, float bottom, String color) {
            paint.setColor(Color.parseColor(color));
            c.drawOval(left, top, right, bottom, paint);
        }
        private void leaf(Canvas c, float x1, float y1, float x2, float y2,
                          float x3, float y3, float x4, float y4, String color) {
            paint.setColor(Color.parseColor(color));
            Path path = new Path();
            path.moveTo(x1, y1);
            path.cubicTo(x2, y2, x3, y3, x4, y4);
            path.close();
            c.drawPath(path, paint);
        }
        private void polygon(Canvas c, float x1, float y1, float x2, float y2,
                             float x3, float y3, float x4, float y4, String color) {
            paint.setColor(Color.parseColor(color));
            Path path = new Path();
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
            path.lineTo(x3, y3);
            path.lineTo(x4, y4);
            path.close();
            c.drawPath(path, paint);
        }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}

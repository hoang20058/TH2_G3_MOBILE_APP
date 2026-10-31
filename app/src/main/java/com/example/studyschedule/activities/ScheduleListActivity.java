package com.example.studyschedule.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.adapters.ScheduleAdapter;
import com.example.studyschedule.dal.StudyScheduleDAO;
import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.NotificationHelper;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/** Member 4: a student's personal schedule list, search, details and CRUD actions. */
public class ScheduleListActivity extends AppCompatActivity
        implements ScheduleAdapter.OnScheduleActionListener {
    private StudyScheduleDAO dao;
    private ScheduleAdapter adapter;
    private EditText searchView;
    private ChipGroup filters;
    private int studentId = -1;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchTask = this::refreshSchedules;

    @Override
    protected void onCreate(Bundle state) {
        ScheduleBookingActivity.useLightTheme(this);
        super.onCreate(state);
        setContentView(R.layout.activity_schedule_list);
        studentId = ScheduleBookingActivity.resolveStudentId(this);
        if (studentId <= 0) return;
        dao = new StudyScheduleDAO(this);
        searchView = findViewById(R.id.edtSearchSchedule);
        filters = findViewById(R.id.chipScheduleStatuses);
        ScheduleBookingActivity.styleField(searchView);
        findViewById(R.id.btnGoToBookSchedule).setBackground(ScheduleAdapter.rounded(
                Color.rgb(25, 38, 83), ScheduleAdapter.dp(this, 13)));
        RecyclerView list = findViewById(R.id.rvSchedules);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter(this);
        list.setAdapter(adapter);
        findViewById(R.id.btnBackSchedules).setOnClickListener(v -> finish());
        findViewById(R.id.btnGoToBookSchedule).setOnClickListener(v -> openBooking(-1));
        findViewById(R.id.btnSearchSchedule).setOnClickListener(v -> searchNow());
        if (state != null) {
            searchView.setText(state.getString("schedule_search", ""));
            filters.check(state.getInt("schedule_filter", R.id.chipScheduleAll));
        }
        filters.setOnCheckedStateChangeListener((group, checkedIds) -> refreshSchedules());
        searchView.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchTask);
                handler.postDelayed(searchTask, 200);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        searchView.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) return false;
            searchNow();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dao != null) refreshSchedules();
    }

    private void searchNow() {
        handler.removeCallbacks(searchTask);
        refreshSchedules();
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    private void refreshSchedules() {
        if (dao == null || adapter == null) return;
        try {
            String keyword = searchView.getText().toString().trim();
            List<StudySchedule> schedules = dao.searchSchedules(studentId, keyword);
            int checked = filters.getCheckedChipId();
            String status = checked == R.id.chipSchedulePending ? Constants.STATUS_PENDING
                    : checked == R.id.chipScheduleConfirmed ? Constants.STATUS_CONFIRMED
                    : checked == R.id.chipScheduleCancelled ? Constants.STATUS_CANCELLED : null;
            List<StudySchedule> visible = new ArrayList<>();
            for (StudySchedule item : schedules) {
                if (status == null || status.equals(item.getStatus())) visible.add(item);
            }
            adapter.setScheduleList(visible);
            ((TextView) findViewById(R.id.tvScheduleCount)).setText(visible.size() + " lịch học");
            TextView empty = findViewById(R.id.tvEmptySchedules);
            empty.setText(keyword.isEmpty() && status == null
                    ? "Chưa có lịch học. Hãy đặt lịch đầu tiên của bạn!"
                    : "Không tìm thấy lịch học phù hợp.");
            empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (RuntimeException e) {
            showError(this, e, "Không thể tải danh sách lịch học.");
        }
    }

    private void openBooking(int scheduleId) {
        Intent intent = new Intent(this, ScheduleBookingActivity.class)
                .putExtra(Constants.EXTRA_STUDENT_ID, studentId);
        if (scheduleId > 0) intent.putExtra(Constants.EXTRA_SCHEDULE_ID, scheduleId);
        else intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (scheduleId <= 0) finish();
    }

    @Override public void onView(StudySchedule item) { showDetails(this, item, () -> onEdit(item)); }
    @Override public void onEdit(StudySchedule item) { openBooking(item.getId()); }
    @Override public void onDelete(StudySchedule item) {
        showDeleteDialog(this, dao, studentId, item, this::refreshSchedules);
    }
    @Override public void onChangeStatus(StudySchedule item) {
        showStatusDialog(this, dao, studentId, item, this::refreshSchedules);
    }

    // Shared dialogs stay in the two assigned activities, without adding helper/resource files.
    static void showDetails(Activity activity, StudySchedule item, Runnable edit) {
        String description = (item.getSubjectCode() == null ? "" : "Mã môn: " + item.getSubjectCode() + "\n")
                + "Ngày học: " + ScheduleAdapter.displayDate(item.getStudyDate(), true)
                + "\nThời gian: " + item.getStartTime() + " – " + item.getEndTime()
                + "\nĐịa điểm: " + item.getLocation()
                + "\nTrạng thái: " + ScheduleAdapter.statusLabel(item.getStatus());
        new AlertDialog.Builder(activity).setTitle(ScheduleAdapter.subjectTitle(item)).setMessage(description)
                .setPositiveButton("Chỉnh sửa", (dialog, which) -> edit.run())
                .setNegativeButton("Đóng", null).show();
    }

    static void showStatusDialog(Activity activity, StudyScheduleDAO dao, int studentId,
                                 StudySchedule item, Runnable changed) {
        String[] values = {Constants.STATUS_PENDING, Constants.STATUS_CONFIRMED, Constants.STATUS_CANCELLED};
        String[] labels = {"Chờ xác nhận", "Đã xác nhận", "Đã hủy"};
        int current = Constants.STATUS_CONFIRMED.equals(item.getStatus()) ? 1
                : Constants.STATUS_CANCELLED.equals(item.getStatus()) ? 2 : 0;
        new AlertDialog.Builder(activity).setTitle("Trạng thái lịch học")
                .setSingleChoiceItems(labels, current, (dialog, which) -> {
                    try {
                        if (!ScheduleBookingActivity.ownsSchedule(dao, studentId, item.getId())) {
                            toast(activity, "Lịch học không còn tồn tại.");
                        } else if (dao.updateScheduleStatus(item.getId(), values[which]) == 1) {
                            item.setStatus(values[which]);
                            if (Constants.STATUS_CANCELLED.equals(values[which])) {
                                NotificationHelper.cancelReminder(activity, item.getId());
                            } else {
                                NotificationHelper.scheduleOrUpdateReminder(activity, item);
                            }
                            toast(activity, "Đã cập nhật trạng thái.");
                        } else {
                            toast(activity, "Không thể đổi trạng thái vì khung giờ bị trùng.");
                        }
                        dialog.dismiss();
                        changed.run();
                    } catch (RuntimeException e) {
                        showError(activity, e, "Không thể cập nhật trạng thái.");
                    }
                }).setNegativeButton("Đóng", null).show();
    }

    static void showDeleteDialog(Activity activity, StudyScheduleDAO dao, int studentId,
                                 StudySchedule item, Runnable changed) {
        boolean cancelled = Constants.STATUS_CANCELLED.equals(item.getStatus());
        String[] choices = cancelled ? new String[]{"Xóa lịch học"}
                : new String[]{"Hủy lịch học", "Xóa lịch học"};
        new AlertDialog.Builder(activity).setTitle(ScheduleAdapter.subjectTitle(item))
                .setItems(choices, (dialog, which) -> {
                    boolean delete = cancelled || which == 1;
                    String action = delete ? "Xóa lịch học" : "Hủy lịch học";
                    new AlertDialog.Builder(activity).setTitle(action)
                            .setMessage((delete ? "Xóa" : "Hủy") + " lịch “"
                                    + ScheduleAdapter.subjectTitle(item) + "”?")
                            .setNegativeButton("Giữ lại", null)
                            .setPositiveButton(action, (confirm, button) -> {
                                try {
                                    if (!ScheduleBookingActivity.ownsSchedule(dao, studentId, item.getId())) {
                                        toast(activity, "Lịch học không còn tồn tại.");
                                    } else {
                                        int count = delete ? dao.deleteSchedule(item.getId())
                                                : dao.updateScheduleStatus(item.getId(), Constants.STATUS_CANCELLED);
                                        if (count == 1) {
                                            NotificationHelper.cancelReminder(activity, item.getId());
                                        }
                                        toast(activity, count == 1 ? (delete ? "Đã xóa lịch học." : "Đã hủy lịch học.")
                                                : "Không thể cập nhật lịch học.");
                                    }
                                    changed.run();
                                } catch (RuntimeException e) {
                                    showError(activity, e, "Không thể cập nhật lịch học.");
                                }
                            }).show();
                }).setNegativeButton("Đóng", null).show();
    }

    private static void toast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
    private static void showError(Activity activity, RuntimeException error, String message) {
        Log.e("ScheduleList", message, error);
        toast(activity, message);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle state) {
        if (searchView != null) {
            state.putString("schedule_search", searchView.getText().toString());
            state.putInt("schedule_filter", filters.getCheckedChipId());
        }
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(searchTask);
        super.onDestroy();
    }
}

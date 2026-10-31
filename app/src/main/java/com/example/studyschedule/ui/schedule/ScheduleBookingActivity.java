package com.example.studyschedule.ui.schedule;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studyschedule.R;
import com.example.studyschedule.dao.StudyScheduleDao;
import com.example.studyschedule.dao.SubjectDao;
import com.example.studyschedule.model.StudySchedule;
import com.example.studyschedule.model.Subject;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.NotificationHelper;
import com.example.studyschedule.utils.SessionManager;

import java.util.List;

/**
 * Màn hình Đặt lịch học / Cập nhật lịch học & Kiểm tra trùng lịch (Phụ trách chính: Thành viên 4).
 * Chức năng yêu cầu:
 * - Chọn môn học từ Spinner (Subject)
 * - Chọn ngày học (yyyy-MM-dd), giờ bắt đầu (HH:mm), giờ kết thúc (HH:mm), địa điểm
 * - Kiểm tra trùng lịch bằng `scheduleDao.hasScheduleConflict(...)`
 * - Lưu lịch học và bắn Notification qua `NotificationHelper.sendInstantNotification(...)`
 */
public class ScheduleBookingActivity extends AppCompatActivity {

    private Spinner spnSubject, spnStatus;
    private EditText edtStudyDate, edtStartTime, edtEndTime, edtLocation;
    private Button btnSaveSchedule;

    private SubjectDao subjectDao;
    private StudyScheduleDao scheduleDao;
    private SessionManager sessionManager;
    private List<Subject> subjectList;
    private int editingScheduleId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_booking);

        subjectDao = new SubjectDao(this);
        scheduleDao = new StudyScheduleDao(this);
        sessionManager = new SessionManager(this);

        spnSubject = findViewById(R.id.spnSubject);
        spnStatus = findViewById(R.id.spnStatus);
        edtStudyDate = findViewById(R.id.edtStudyDate);
        edtStartTime = findViewById(R.id.edtStartTime);
        edtEndTime = findViewById(R.id.edtEndTime);
        edtLocation = findViewById(R.id.edtLocation);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);

        setupSpinners();

        // Hỗ trợ nhận ngày được chọn từ CalendarView (Thành viên 5) hoặc ID lịch cần sửa
        editingScheduleId = getIntent().getIntExtra(Constants.EXTRA_SCHEDULE_ID, -1);
        String preselectedDate = getIntent().getStringExtra(Constants.EXTRA_SELECTED_DATE);
        if (preselectedDate != null) {
            edtStudyDate.setText(preselectedDate);
        }

        // TODO (Thành viên 4): Gắn DatePickerDialog cho edtStudyDate và TimePickerDialog cho edtStartTime, edtEndTime

        btnSaveSchedule.setOnClickListener(v -> saveSchedule());
    }

    private void setupSpinners() {
        subjectList = subjectDao.getAllSubjects();
        ArrayAdapter<Subject> subjectAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subjectList);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSubject.setAdapter(subjectAdapter);

        String[] statuses = {Constants.STATUS_PENDING, Constants.STATUS_CONFIRMED, Constants.STATUS_CANCELLED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(statusAdapter);
    }

    private void saveSchedule() {
        if (subjectList == null || subjectList.isEmpty()) {
            Toast.makeText(this, "No subjects available!", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject selectedSubject = (Subject) spnSubject.getSelectedItem();
        String studyDate = edtStudyDate.getText().toString().trim();
        String startTime = edtStartTime.getText().toString().trim();
        String endTime = edtEndTime.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String status = (String) spnStatus.getSelectedItem();
        int studentId = sessionManager.getStudentId();

        if (studyDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime.compareTo(endTime) >= 0) {
            Toast.makeText(this, "End time must be after start time!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra trùng lịch (Schedule Conflict Validation)
        boolean isConflict = scheduleDao.hasScheduleConflict(
                studentId, studyDate, startTime, endTime, editingScheduleId);
        if (isConflict) {
            Toast.makeText(this, "Schedule Conflict! You already have a session during this time.", Toast.LENGTH_LONG).show();
            return;
        }

        StudySchedule schedule = new StudySchedule(
                editingScheduleId, studentId, selectedSubject.getId(),
                studyDate, startTime, endTime, location, status
        );

        if (editingScheduleId == -1) {
            long id = scheduleDao.insertSchedule(schedule);
            if (id != -1) {
                NotificationHelper.sendInstantNotification(this,
                        "Study Session Booked",
                        selectedSubject.getSubjectName() + " on " + studyDate + " (" + startTime + " - " + endTime + ")");
                Toast.makeText(this, "Booked successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            scheduleDao.updateSchedule(schedule);
            NotificationHelper.sendInstantNotification(this,
                    "Study Session Updated",
                    selectedSubject.getSubjectName() + " updated to " + studyDate + " (" + startTime + " - " + endTime + ")");
            Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

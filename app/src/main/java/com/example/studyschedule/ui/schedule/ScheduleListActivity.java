package com.example.studyschedule.ui.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.dao.StudyScheduleDao;
import com.example.studyschedule.model.StudySchedule;
import com.example.studyschedule.utils.SessionManager;

import java.util.List;

/**
 * Màn hình Quản lý Lịch học cá nhân & Trạng thái (Phụ trách chính: Thành viên 4).
 * Chức năng yêu cầu:
 * - Xem danh sách lịch học của sinh viên đang đăng nhập (View)
 * - Tìm kiếm lịch học (Search)
 * - Đổi trạng thái lịch học: Pending, Confirmed, Cancelled (Schedule Status Management)
 * - Chuyển sang màn hình Sửa hoặc Xóa lịch học (Update / Delete)
 */
public class ScheduleListActivity extends AppCompatActivity {

    private EditText edtSearchSchedule;
    private Button btnSearchSchedule, btnGoToBookSchedule;
    private RecyclerView rvSchedules;
    private StudyScheduleDao scheduleDao;
    private SessionManager sessionManager;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        scheduleDao = new StudyScheduleDao(this);
        sessionManager = new SessionManager(this);

        edtSearchSchedule = findViewById(R.id.edtSearchSchedule);
        btnSearchSchedule = findViewById(R.id.btnSearchSchedule);
        btnGoToBookSchedule = findViewById(R.id.btnGoToBookSchedule);
        rvSchedules = findViewById(R.id.rvSchedules);

        rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter();
        rvSchedules.setAdapter(adapter);

        btnSearchSchedule.setOnClickListener(v -> {
            String keyword = edtSearchSchedule.getText().toString().trim();
            loadSchedules(keyword);
        });

        btnGoToBookSchedule.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleBookingActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSchedules(edtSearchSchedule.getText().toString().trim());
    }

    private void loadSchedules(String keyword) {
        int studentId = sessionManager.getStudentId();
        List<StudySchedule> list = keyword.isEmpty()
                ? scheduleDao.getSchedulesByStudent(studentId)
                : scheduleDao.searchSchedules(studentId, keyword);
        adapter.setScheduleList(list);
    }
}

package com.example.studyschedule.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studyschedule.R;

/**
 * ScheduleBookingActivity (Người 4 - nhánh feature/study-schedule).
 * TODO (Người 4):
 * - Chọn môn học từ Spinner (lấy từ SubjectDAO)
 * - Chọn ngày học, giờ bắt đầu, giờ kết thúc, địa điểm
 * - Kiểm tra trùng lịch (Schedule Conflict Validation)
 * - Lưu lịch học mới hoặc cập nhật lịch học
 */
public class ScheduleBookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_booking);
        // TODO (Người 4): Code chức năng đặt lịch học và kiểm tra trùng lịch tại đây
    }
}

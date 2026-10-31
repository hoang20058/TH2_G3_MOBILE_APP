package com.example.studyschedule.dal;

import android.content.Context;
import com.example.studyschedule.models.StudySchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng StudySchedule.
 * Phụ trách: Người 4 (nhánh feature/study-schedule) & Người 5 (nhánh feature/calendar-notifications).
 */
public class StudyScheduleDAO {

    private final DBHelper dbHelper;

    public StudyScheduleDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public List<StudySchedule> getSchedulesByStudent(int studentId) {
        // TODO (Người 4): Lấy danh sách lịch học cá nhân của sinh viên
        return new ArrayList<>();
    }

    public List<StudySchedule> getSchedulesByDate(int studentId, String studyDate) {
        // TODO (Người 5): Lấy lịch học theo ngày (yyyy-MM-dd)
        return new ArrayList<>();
    }

    public List<StudySchedule> getSchedulesBetweenDates(int studentId, String fromDate, String toDate) {
        // TODO (Người 5): Lấy lịch học trong khoảng ngày (phục vụ xem theo Tuần / Tháng)
        return new ArrayList<>();
    }

    public List<StudySchedule> searchSchedules(int studentId, String keyword) {
        // TODO (Người 4): Tìm kiếm lịch học cá nhân
        return new ArrayList<>();
    }

    public StudySchedule getScheduleById(int scheduleId) {
        // TODO (Người 4): Lấy chi tiết 1 lịch học theo ID
        return null;
    }

    public boolean hasScheduleConflict(int studentId, String studyDate, String newStartTime, String newEndTime, int excludeScheduleId) {
        // TODO (Người 4): Kiểm tra trùng lịch học trong cùng ngày và khung giờ
        return false;
    }

    public long insertSchedule(StudySchedule schedule) {
        // TODO (Người 4): Đặt lịch học mới
        return -1;
    }

    public int updateSchedule(StudySchedule schedule) {
        // TODO (Người 4): Cập nhật lịch học
        return 0;
    }

    public int updateScheduleStatus(int scheduleId, String newStatus) {
        // TODO (Người 4): Cập nhật trạng thái Pending / Confirmed / Cancelled
        return 0;
    }

    public int deleteSchedule(int scheduleId) {
        // TODO (Người 4): Xóa lịch học
        return 0;
    }
}

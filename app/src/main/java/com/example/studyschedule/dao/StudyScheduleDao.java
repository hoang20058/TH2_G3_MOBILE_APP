package com.example.studyschedule.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.database.DatabaseHelper;
import com.example.studyschedule.model.StudySchedule;
import com.example.studyschedule.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng StudySchedule (Phụ trách chính: Thành viên 4 & Thành viên 5).
 * Tích hợp sẵn thuật toán kiểm tra trùng lịch (Schedule Conflict Validation).
 */
public class StudyScheduleDao {

    private final DatabaseHelper dbHelper;

    public StudyScheduleDao(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Lấy toàn bộ lịch học của 1 sinh viên (JOIN với bảng Subject để lấy tên môn học).
     */
    public List<StudySchedule> getSchedulesByStudent(int studentId) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DatabaseHelper.COL_SUB_CODE + ", sub." + DatabaseHelper.COL_SUB_NAME
                + " FROM " + DatabaseHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DatabaseHelper.TABLE_SUBJECT + " sub ON s." + DatabaseHelper.COL_SCH_SUBJECT_ID + " = sub." + DatabaseHelper.COL_SUB_ID
                + " WHERE s." + DatabaseHelper.COL_SCH_STUDENT_ID + " = ? "
                + " ORDER BY s." + DatabaseHelper.COL_SCH_DATE + " ASC, s." + DatabaseHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Lấy lịch học của sinh viên theo ngày cụ thể (Dùng cho Calendar View - Thành viên 5).
     * @param studyDate Định dạng yyyy-MM-dd
     */
    public List<StudySchedule> getSchedulesByDate(int studentId, String studyDate) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DatabaseHelper.COL_SUB_CODE + ", sub." + DatabaseHelper.COL_SUB_NAME
                + " FROM " + DatabaseHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DatabaseHelper.TABLE_SUBJECT + " sub ON s." + DatabaseHelper.COL_SCH_SUBJECT_ID + " = sub." + DatabaseHelper.COL_SUB_ID
                + " WHERE s." + DatabaseHelper.COL_SCH_STUDENT_ID + " = ? AND s." + DatabaseHelper.COL_SCH_DATE + " = ? "
                + " ORDER BY s." + DatabaseHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId), studyDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Lấy lịch học của sinh viên trong khoảng từ ngày -> đến ngày (Hỗ trợ xem theo Tuần / Tháng ở Calendar View).
     */
    public List<StudySchedule> getSchedulesBetweenDates(int studentId, String fromDate, String toDate) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DatabaseHelper.COL_SUB_CODE + ", sub." + DatabaseHelper.COL_SUB_NAME
                + " FROM " + DatabaseHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DatabaseHelper.TABLE_SUBJECT + " sub ON s." + DatabaseHelper.COL_SCH_SUBJECT_ID + " = sub." + DatabaseHelper.COL_SUB_ID
                + " WHERE s." + DatabaseHelper.COL_SCH_STUDENT_ID + " = ? "
                + " AND s." + DatabaseHelper.COL_SCH_DATE + " >= ? AND s." + DatabaseHelper.COL_SCH_DATE + " <= ? "
                + " ORDER BY s." + DatabaseHelper.COL_SCH_DATE + " ASC, s." + DatabaseHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId), fromDate, toDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Tìm kiếm lịch học cá nhân theo tên môn, mã môn, địa điểm hoặc ngày.
     */
    public List<StudySchedule> searchSchedules(int studentId, String keyword) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT s.*, sub." + DatabaseHelper.COL_SUB_CODE + ", sub." + DatabaseHelper.COL_SUB_NAME
                + " FROM " + DatabaseHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DatabaseHelper.TABLE_SUBJECT + " sub ON s." + DatabaseHelper.COL_SCH_SUBJECT_ID + " = sub." + DatabaseHelper.COL_SUB_ID
                + " WHERE s." + DatabaseHelper.COL_SCH_STUDENT_ID + " = ? "
                + " AND (sub." + DatabaseHelper.COL_SUB_NAME + " LIKE ? "
                + " OR sub." + DatabaseHelper.COL_SUB_CODE + " LIKE ? "
                + " OR s." + DatabaseHelper.COL_SCH_LOCATION + " LIKE ? "
                + " OR s." + DatabaseHelper.COL_SCH_DATE + " LIKE ?) "
                + " ORDER BY s." + DatabaseHelper.COL_SCH_DATE + " ASC, s." + DatabaseHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId), pattern, pattern, pattern, pattern})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Lấy chi tiết 1 lịch học theo ID (phục vụ màn hình Edit).
     */
    public StudySchedule getScheduleById(int scheduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DatabaseHelper.COL_SUB_CODE + ", sub." + DatabaseHelper.COL_SUB_NAME
                + " FROM " + DatabaseHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DatabaseHelper.TABLE_SUBJECT + " sub ON s." + DatabaseHelper.COL_SCH_SUBJECT_ID + " = sub." + DatabaseHelper.COL_SUB_ID
                + " WHERE s." + DatabaseHelper.COL_SCH_ID + " = ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(scheduleId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToSchedule(cursor);
            }
        }
        return null;
    }

    /**
     * KIỂM TRA TRÙNG LỊCH HỌC (Schedule Conflict Validation):
     * Hai khoảng thời gian [existingStart, existingEnd] và [newStart, newEnd] trong cùng 1 ngày bị trùng nhau khi:
     * existingStart < newEnd VÀ existingEnd > newStart (không tính các lịch đã Cancelled).
     *
     * @param studentId         ID của sinh viên
     * @param studyDate         Ngày học (yyyy-MM-dd)
     * @param newStartTime      Giờ bắt đầu mới (HH:mm)
     * @param newEndTime        Giờ kết thúc mới (HH:mm)
     * @param excludeScheduleId Truyền -1 nếu thêm mới, hoặc truyền ID của lịch đang sửa để không tự đụng chính nó
     * @return true nếu bị trùng lịch, false nếu hợp lệ
     */
    public boolean hasScheduleConflict(int studentId, String studyDate, String newStartTime, String newEndTime, int excludeScheduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCHEDULE
                + " WHERE " + DatabaseHelper.COL_SCH_STUDENT_ID + " = ? "
                + " AND " + DatabaseHelper.COL_SCH_DATE + " = ? "
                + " AND " + DatabaseHelper.COL_SCH_STATUS + " != ? "
                + " AND " + DatabaseHelper.COL_SCH_ID + " != ? "
                + " AND " + DatabaseHelper.COL_SCH_START_TIME + " < ? "
                + " AND " + DatabaseHelper.COL_SCH_END_TIME + " > ?";

        String[] args = {
                String.valueOf(studentId),
                studyDate,
                Constants.STATUS_CANCELLED,
                String.valueOf(excludeScheduleId),
                newEndTime,
                newStartTime
        };

        try (Cursor cursor = db.rawQuery(sql, args)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    public long insertSchedule(StudySchedule schedule) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SCH_STUDENT_ID, schedule.getStudentId());
        values.put(DatabaseHelper.COL_SCH_SUBJECT_ID, schedule.getSubjectId());
        values.put(DatabaseHelper.COL_SCH_DATE, schedule.getStudyDate());
        values.put(DatabaseHelper.COL_SCH_START_TIME, schedule.getStartTime());
        values.put(DatabaseHelper.COL_SCH_END_TIME, schedule.getEndTime());
        values.put(DatabaseHelper.COL_SCH_LOCATION, schedule.getLocation());
        values.put(DatabaseHelper.COL_SCH_STATUS, schedule.getStatus());
        return db.insert(DatabaseHelper.TABLE_SCHEDULE, null, values);
    }

    public int updateSchedule(StudySchedule schedule) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SCH_SUBJECT_ID, schedule.getSubjectId());
        values.put(DatabaseHelper.COL_SCH_DATE, schedule.getStudyDate());
        values.put(DatabaseHelper.COL_SCH_START_TIME, schedule.getStartTime());
        values.put(DatabaseHelper.COL_SCH_END_TIME, schedule.getEndTime());
        values.put(DatabaseHelper.COL_SCH_LOCATION, schedule.getLocation());
        values.put(DatabaseHelper.COL_SCH_STATUS, schedule.getStatus());

        return db.update(DatabaseHelper.TABLE_SCHEDULE, values,
                DatabaseHelper.COL_SCH_ID + " = ?",
                new String[]{String.valueOf(schedule.getId())});
    }

    public int updateScheduleStatus(int scheduleId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SCH_STATUS, newStatus);
        return db.update(DatabaseHelper.TABLE_SCHEDULE, values,
                DatabaseHelper.COL_SCH_ID + " = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    public int deleteSchedule(int scheduleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SCHEDULE,
                DatabaseHelper.COL_SCH_ID + " = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    private StudySchedule mapCursorToSchedule(Cursor cursor) {
        StudySchedule schedule = new StudySchedule(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_STUDENT_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_SUBJECT_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_START_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_END_TIME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_LOCATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SCH_STATUS))
        );
        int codeIndex = cursor.getColumnIndex(DatabaseHelper.COL_SUB_CODE);
        if (codeIndex != -1) {
            schedule.setSubjectCode(cursor.getString(codeIndex));
        }
        int nameIndex = cursor.getColumnIndex(DatabaseHelper.COL_SUB_NAME);
        if (nameIndex != -1) {
            schedule.setSubjectName(cursor.getString(nameIndex));
        }
        return schedule;
    }
}

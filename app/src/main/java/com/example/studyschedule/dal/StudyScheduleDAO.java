package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng StudySchedule (Người 4 & Người 5).
 */
public class StudyScheduleDAO {

    private final DBHelper dbHelper;

    public StudyScheduleDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public List<StudySchedule> getSchedulesByStudent(int studentId) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DBHelper.COL_SUB_CODE + ", sub." + DBHelper.COL_SUB_NAME
                + " FROM " + DBHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DBHelper.TABLE_SUBJECT + " sub ON s." + DBHelper.COL_SCH_SUBJECT_ID + " = sub." + DBHelper.COL_SUB_ID
                + " WHERE s." + DBHelper.COL_SCH_STUDENT_ID + " = ? "
                + " ORDER BY s." + DBHelper.COL_SCH_DATE + " ASC, s." + DBHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<StudySchedule> getSchedulesByDate(int studentId, String studyDate) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DBHelper.COL_SUB_CODE + ", sub." + DBHelper.COL_SUB_NAME
                + " FROM " + DBHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DBHelper.TABLE_SUBJECT + " sub ON s." + DBHelper.COL_SCH_SUBJECT_ID + " = sub." + DBHelper.COL_SUB_ID
                + " WHERE s." + DBHelper.COL_SCH_STUDENT_ID + " = ? AND s." + DBHelper.COL_SCH_DATE + " = ? "
                + " ORDER BY s." + DBHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId), studyDate})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<StudySchedule> getSchedulesBetweenDates(int studentId, String fromDate, String toDate) {
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
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public List<StudySchedule> searchSchedules(int studentId, String keyword) {
        List<StudySchedule> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT s.*, sub." + DBHelper.COL_SUB_CODE + ", sub." + DBHelper.COL_SUB_NAME
                + " FROM " + DBHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DBHelper.TABLE_SUBJECT + " sub ON s." + DBHelper.COL_SCH_SUBJECT_ID + " = sub." + DBHelper.COL_SUB_ID
                + " WHERE s." + DBHelper.COL_SCH_STUDENT_ID + " = ? "
                + " AND (sub." + DBHelper.COL_SUB_NAME + " LIKE ? "
                + " OR sub." + DBHelper.COL_SUB_CODE + " LIKE ? "
                + " OR s." + DBHelper.COL_SCH_LOCATION + " LIKE ? "
                + " OR s." + DBHelper.COL_SCH_DATE + " LIKE ?) "
                + " ORDER BY s." + DBHelper.COL_SCH_DATE + " ASC, s." + DBHelper.COL_SCH_START_TIME + " ASC";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(studentId), pattern, pattern, pattern, pattern})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToSchedule(cursor));
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    public StudySchedule getScheduleById(int scheduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT s.*, sub." + DBHelper.COL_SUB_CODE + ", sub." + DBHelper.COL_SUB_NAME
                + " FROM " + DBHelper.TABLE_SCHEDULE + " s "
                + " JOIN " + DBHelper.TABLE_SUBJECT + " sub ON s." + DBHelper.COL_SCH_SUBJECT_ID + " = sub." + DBHelper.COL_SUB_ID
                + " WHERE s." + DBHelper.COL_SCH_ID + " = ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(scheduleId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                return mapCursorToSchedule(cursor);
            }
        }
        return null;
    }

    public boolean hasScheduleConflict(int studentId, String studyDate, String newStartTime, String newEndTime, int excludeScheduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + DBHelper.TABLE_SCHEDULE
                + " WHERE " + DBHelper.COL_SCH_STUDENT_ID + " = ? "
                + " AND " + DBHelper.COL_SCH_DATE + " = ? "
                + " AND " + DBHelper.COL_SCH_STATUS + " != ? "
                + " AND " + DBHelper.COL_SCH_ID + " != ? "
                + " AND " + DBHelper.COL_SCH_START_TIME + " < ? "
                + " AND " + DBHelper.COL_SCH_END_TIME + " > ?";

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
        values.put(DBHelper.COL_SCH_STUDENT_ID, schedule.getStudentId());
        values.put(DBHelper.COL_SCH_SUBJECT_ID, schedule.getSubjectId());
        values.put(DBHelper.COL_SCH_DATE, schedule.getStudyDate());
        values.put(DBHelper.COL_SCH_START_TIME, schedule.getStartTime());
        values.put(DBHelper.COL_SCH_END_TIME, schedule.getEndTime());
        values.put(DBHelper.COL_SCH_LOCATION, schedule.getLocation());
        values.put(DBHelper.COL_SCH_STATUS, schedule.getStatus());
        return db.insert(DBHelper.TABLE_SCHEDULE, null, values);
    }

    public int updateSchedule(StudySchedule schedule) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SCH_SUBJECT_ID, schedule.getSubjectId());
        values.put(DBHelper.COL_SCH_DATE, schedule.getStudyDate());
        values.put(DBHelper.COL_SCH_START_TIME, schedule.getStartTime());
        values.put(DBHelper.COL_SCH_END_TIME, schedule.getEndTime());
        values.put(DBHelper.COL_SCH_LOCATION, schedule.getLocation());
        values.put(DBHelper.COL_SCH_STATUS, schedule.getStatus());

        return db.update(DBHelper.TABLE_SCHEDULE, values,
                DBHelper.COL_SCH_ID + " = ?",
                new String[]{String.valueOf(schedule.getId())});
    }

    public int updateScheduleStatus(int scheduleId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SCH_STATUS, newStatus);
        return db.update(DBHelper.TABLE_SCHEDULE, values,
                DBHelper.COL_SCH_ID + " = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    public int deleteSchedule(int scheduleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DBHelper.TABLE_SCHEDULE,
                DBHelper.COL_SCH_ID + " = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    private StudySchedule mapCursorToSchedule(Cursor cursor) {
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
        int codeIndex = cursor.getColumnIndex(DBHelper.COL_SUB_CODE);
        if (codeIndex != -1) {
            schedule.setSubjectCode(cursor.getString(codeIndex));
        }
        int nameIndex = cursor.getColumnIndex(DBHelper.COL_SUB_NAME);
        if (nameIndex != -1) {
            schedule.setSubjectName(cursor.getString(nameIndex));
        }
        return schedule;
    }
}

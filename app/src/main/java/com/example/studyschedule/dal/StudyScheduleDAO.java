package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.models.Subject;
import com.example.studyschedule.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** SQLite operations for member 4's booking and personal schedule screens. */
public class StudyScheduleDAO {
    private final DBHelper dbHelper;
    private static final String SELECT = "SELECT s.*, m.subjectCode, m.subjectName FROM "
            + DBHelper.TABLE_SCHEDULE + " s LEFT JOIN " + DBHelper.TABLE_SUBJECT
            + " m ON s.subjectId = m.id ";
    private static final String ORDER = " ORDER BY s.studyDate ASC, s.startTime ASC, s.id ASC";

    public StudyScheduleDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    /** Read-only bridge while member 3's SubjectDAO.getAllSubjects() is still a stub. */
    public List<Subject> getBookingSubjects() {
        List<Subject> subjects = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().query(DBHelper.TABLE_SUBJECT,
                null, null, null, null, null, "id ASC")) {
            while (c.moveToNext()) {
                subjects.add(new Subject(c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("subjectCode")),
                        c.getString(c.getColumnIndexOrThrow("subjectName")),
                        c.getString(c.getColumnIndexOrThrow("description")),
                        c.getInt(c.getColumnIndexOrThrow("credits"))));
            }
        }
        return subjects;
    }

    public List<StudySchedule> getSchedulesByStudent(int studentId) {
        return query("s.studentId = ?", new String[]{String.valueOf(studentId)});
    }

    public List<StudySchedule> getSchedulesByDate(int studentId, String studyDate) {
        return query("s.studentId = ? AND s.studyDate = ?",
                new String[]{String.valueOf(studentId), studyDate});
    }

    public List<StudySchedule> getSchedulesBetweenDates(int studentId, String fromDate, String toDate) {
        return query("s.studentId = ? AND s.studyDate BETWEEN ? AND ?",
                new String[]{String.valueOf(studentId), fromDate, toDate});
    }

    public List<StudySchedule> searchSchedules(int studentId, String keyword) {
        // Escape LIKE wildcards: a typed '%' or '_' is treated as literal text.
        String term = keyword == null ? "" : keyword.trim();
        String like = "%" + term.replace("\\", "\\\\").replace("%", "\\%")
                .replace("_", "\\_") + "%";
        return query("s.studentId = ? AND (m.subjectCode LIKE ? ESCAPE '\\'"
                        + " OR m.subjectName LIKE ? ESCAPE '\\'"
                        + " OR s.studyDate LIKE ? ESCAPE '\\'"
                        + " OR strftime('%d/%m/%Y', s.studyDate) LIKE ? ESCAPE '\\'"
                        + " OR s.location LIKE ? ESCAPE '\\')",
                new String[]{String.valueOf(studentId), like, like, like, like, like});
    }

    public StudySchedule getScheduleById(int scheduleId) {
        List<StudySchedule> schedules = query("s.id = ?", new String[]{String.valueOf(scheduleId)});
        return schedules.isEmpty() ? null : schedules.get(0);
    }

    private List<StudySchedule> query(String where, String[] args) {
        List<StudySchedule> result = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(SELECT + " WHERE " + where + ORDER, args)) {
            while (c.moveToNext()) {
                StudySchedule item = new StudySchedule(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getInt(c.getColumnIndexOrThrow("studentId")),
                        c.getInt(c.getColumnIndexOrThrow("subjectId")),
                        c.getString(c.getColumnIndexOrThrow("studyDate")),
                        c.getString(c.getColumnIndexOrThrow("startTime")),
                        c.getString(c.getColumnIndexOrThrow("endTime")),
                        c.getString(c.getColumnIndexOrThrow("location")),
                        c.getString(c.getColumnIndexOrThrow("status")));
                item.setSubjectCode(c.getString(c.getColumnIndexOrThrow("subjectCode")));
                item.setSubjectName(c.getString(c.getColumnIndexOrThrow("subjectName")));
                result.add(item);
            }
        }
        return result;
    }

    public boolean hasScheduleConflict(int studentId, String studyDate, String newStartTime,
                                       String newEndTime, int excludeScheduleId) {
        // Half-open intervals: 08:00–09:30 and 09:30–10:00 do not overlap.
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + DBHelper.TABLE_SCHEDULE
                        + " WHERE studentId = ? AND studyDate = ? AND status <> ?"
                        + " AND startTime < ? AND endTime > ? AND id <> ? LIMIT 1",
                new String[]{String.valueOf(studentId), studyDate, Constants.STATUS_CANCELLED,
                        newEndTime, newStartTime, String.valueOf(excludeScheduleId)})) {
            return c.moveToFirst();
        }
    }

    public long insertSchedule(StudySchedule schedule) {
        if (!isValidSchedule(schedule)) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (isActiveConflict(schedule, -1)) return -1;
            long id = db.insertOrThrow(DBHelper.TABLE_SCHEDULE, null, values(schedule));
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public int updateSchedule(StudySchedule schedule) {
        if (!isValidSchedule(schedule) || schedule.getId() <= 0) return 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (isActiveConflict(schedule, schedule.getId())) return 0;
            int count = db.update(DBHelper.TABLE_SCHEDULE, values(schedule), "id = ? AND studentId = ?",
                    new String[]{String.valueOf(schedule.getId()), String.valueOf(schedule.getStudentId())});
            db.setTransactionSuccessful();
            return count;
        } finally {
            db.endTransaction();
        }
    }

    public int updateScheduleStatus(int scheduleId, String newStatus) {
        if (!isValidStatus(newStatus)) return 0;
        // The same transaction protects reactivation of a cancelled schedule from conflicts.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            StudySchedule item = getScheduleById(scheduleId);
            if (item == null) return 0;
            item.setStatus(newStatus);
            if (isActiveConflict(item, scheduleId)) return 0;
            ContentValues values = new ContentValues();
            values.put(DBHelper.COL_SCH_STATUS, newStatus);
            int count = db.update(DBHelper.TABLE_SCHEDULE, values, "id = ?",
                    new String[]{String.valueOf(scheduleId)});
            db.setTransactionSuccessful();
            return count;
        } finally {
            db.endTransaction();
        }
    }

    public int deleteSchedule(int scheduleId) {
        return dbHelper.getWritableDatabase().delete(DBHelper.TABLE_SCHEDULE, "id = ?",
                new String[]{String.valueOf(scheduleId)});
    }

    private boolean isActiveConflict(StudySchedule schedule, int excludeId) {
        return !Constants.STATUS_CANCELLED.equals(schedule.getStatus())
                && hasScheduleConflict(schedule.getStudentId(), schedule.getStudyDate(),
                schedule.getStartTime(), schedule.getEndTime(), excludeId);
    }

    private ContentValues values(StudySchedule schedule) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_SCH_STUDENT_ID, schedule.getStudentId());
        values.put(DBHelper.COL_SCH_SUBJECT_ID, schedule.getSubjectId());
        values.put(DBHelper.COL_SCH_DATE, schedule.getStudyDate());
        values.put(DBHelper.COL_SCH_START_TIME, schedule.getStartTime());
        values.put(DBHelper.COL_SCH_END_TIME, schedule.getEndTime());
        values.put(DBHelper.COL_SCH_LOCATION, schedule.getLocation().trim());
        values.put(DBHelper.COL_SCH_STATUS, schedule.getStatus());
        return values;
    }

    public static boolean isValidStatus(String status) {
        return Constants.STATUS_PENDING.equals(status) || Constants.STATUS_CONFIRMED.equals(status)
                || Constants.STATUS_CANCELLED.equals(status);
    }

    public static boolean isValidTimeRange(String start, String end) {
        return start != null && end != null
                && start.matches("([01][0-9]|2[0-3]):[0-5][0-9]")
                && end.matches("([01][0-9]|2[0-3]):[0-5][0-9]")
                && start.compareTo(end) < 0;
    }

    public static boolean isValidDate(String date) {
        if (date == null || !date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return false;
        SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        format.setLenient(false);
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidSchedule(StudySchedule schedule) {
        return schedule != null && schedule.getStudentId() > 0 && schedule.getSubjectId() > 0
                && isValidDate(schedule.getStudyDate())
                && isValidTimeRange(schedule.getStartTime(), schedule.getEndTime())
                && schedule.getLocation() != null && !schedule.getLocation().trim().isEmpty()
                && schedule.getLocation().trim().length() <= 120 && isValidStatus(schedule.getStatus());
    }
}

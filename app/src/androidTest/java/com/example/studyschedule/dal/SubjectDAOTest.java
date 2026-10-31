package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.studyschedule.models.Subject;
import com.example.studyschedule.utils.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import static org.junit.Assert.*;
import static com.example.studyschedule.dal.DBHelper.*;

/** Chạy trên emulator/test device; mọi dữ liệu test được rollback. */
@RunWith(AndroidJUnit4.class)
public class SubjectDAOTest {
    private SQLiteDatabase db;
    private SubjectDAO dao;
    private String prefix;

    @Before public void setUp() {
        android.content.Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DBHelper.getInstance(context).getWritableDatabase();
        dao = new SubjectDAO(context);
        prefix = "TEST_" + System.nanoTime();
        db.beginTransaction();
    }
    @After public void tearDown() { db.endTransaction(); }

    private Subject insert(String suffix) {
        Subject subject = new Subject(prefix + suffix, "Tên môn kiểm thử", "Mô tả đầy đủ", 3);
        subject.setId((int) dao.insertSubject(subject));
        assertTrue(subject.getId() > 0);
        return subject;
    }

    @Test public void seedAndSearchAndRoundTrip() {
        assertFalse(dao.getAllSubjects().isEmpty());
        Subject subject = insert("A");
        List<Subject> loaded = dao.searchSubjects(subject.getSubjectCode().toLowerCase(java.util.Locale.ROOT));
        assertEquals(1, loaded.size());
        assertEquals(subject.getId(), loaded.get(0).getId());
        assertEquals("Mô tả đầy đủ", loaded.get(0).getDescription());
        assertTrue(dao.searchSubjects("Tên môn kiểm thử").size() >= 1);
        assertEquals(dao.getAllSubjects().size(), dao.searchSubjects("   ").size());
        assertTrue(dao.searchSubjects(prefix + "%").isEmpty());
    }

    @Test public void validateAndNormalizeWrites() {
        assertThrows(IllegalArgumentException.class, () -> dao.insertSubject(new Subject(" ", "Tên", "", 1)));
        assertThrows(IllegalArgumentException.class, () -> dao.insertSubject(new Subject("X", " ", "", 1)));
        assertThrows(IllegalArgumentException.class, () -> dao.insertSubject(new Subject("X", "Tên", "", 0)));
        assertThrows(IllegalArgumentException.class, () -> dao.insertSubject(new Subject("X", "Tên", "", -1)));
        Subject subject = new Subject("  " + prefix.toLowerCase(java.util.Locale.ROOT) + "  ", " Tên ", null, Integer.MAX_VALUE);
        dao.insertSubject(subject);
        Subject loaded = dao.searchSubjects(prefix).get(0);
        assertEquals(prefix, loaded.getSubjectCode());
        assertEquals("Tên", loaded.getSubjectName());
        assertEquals("", loaded.getDescription());
        assertEquals(Integer.MAX_VALUE, loaded.getCredits());
    }

    @Test public void updateKeepsIdAndRejectsDuplicate() {
        Subject a = insert("A");
        Subject b = insert("B");
        a.setSubjectName("Tên mới");
        assertEquals(1, dao.updateSubject(a));
        assertEquals(a.getId(), dao.searchSubjects(a.getSubjectCode()).get(0).getId());
        assertEquals("Tên mới", dao.searchSubjects(a.getSubjectCode()).get(0).getSubjectName());
        assertThrows(SubjectDAO.DuplicateCodeException.class, () -> dao.insertSubject(
                new Subject(a.getSubjectCode().toLowerCase(java.util.Locale.ROOT), "Trùng", "", 2)));
        b.setSubjectCode(a.getSubjectCode());
        assertThrows(SubjectDAO.DuplicateCodeException.class, () -> dao.updateSubject(b));
        assertEquals(1, dao.searchSubjects(prefix + "B").size());
    }

    @Test public void deleteUnusedAndMissing() {
        Subject subject = insert("DELETE");
        assertEquals(1, dao.deleteSubject(subject.getId()));
        assertEquals(0, dao.deleteSubject(subject.getId()));
        assertTrue(dao.searchSubjects(subject.getSubjectCode()).isEmpty());
    }

    @Test public void everyScheduleStatusBlocksCascade() {
        Subject subject = insert("USED");
        ContentValues student = new ContentValues();
        student.put(COL_STU_CODE, prefix);
        student.put(COL_STU_FULLNAME, "Test student");
        long studentId = db.insertOrThrow(TABLE_STUDENT, null, student);
        ContentValues schedule = new ContentValues();
        schedule.put(COL_SCH_STUDENT_ID, studentId);
        schedule.put(COL_SCH_SUBJECT_ID, subject.getId());
        schedule.put(COL_SCH_DATE, "2026-11-01");
        schedule.put(COL_SCH_START_TIME, "08:00");
        schedule.put(COL_SCH_END_TIME, "10:00");
        schedule.put(COL_SCH_LOCATION, "Test room");
        schedule.put(COL_SCH_STATUS, Constants.STATUS_PENDING);
        long scheduleId = db.insertOrThrow(TABLE_SCHEDULE, null, schedule);
        for (String status : new String[]{Constants.STATUS_PENDING, Constants.STATUS_CONFIRMED, Constants.STATUS_CANCELLED}) {
            ContentValues change = new ContentValues();
            change.put(COL_SCH_STATUS, status);
            db.update(TABLE_SCHEDULE, change, COL_SCH_ID + " = ?", new String[]{String.valueOf(scheduleId)});
            assertEquals(SubjectDAO.DELETE_IN_USE, dao.deleteSubject(subject.getId()));
            assertEquals(1, dao.searchSubjects(subject.getSubjectCode()).size());
            try (Cursor c = db.query(TABLE_SCHEDULE, new String[]{COL_SCH_STATUS}, COL_SCH_ID + " = ?",
                    new String[]{String.valueOf(scheduleId)}, null, null, null)) {
                assertTrue(c.moveToFirst());
                assertEquals(status, c.getString(0));
            }
        }
    }
}

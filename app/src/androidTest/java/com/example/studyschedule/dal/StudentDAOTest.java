package com.example.studyschedule.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.studyschedule.models.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StudentDAOTest {
    private SQLiteDatabase db;
    private StudentDAO students;
    private String prefix;
    private DBHelper helper;
    private Context context;

    @Before public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        prefix = "test_" + System.nanoTime();
        db = SQLiteDatabase.create(null);
        db.setForeignKeyConstraintsEnabled(true);
        helper = new DBHelper(context) {
            @Override public SQLiteDatabase getWritableDatabase() { return db; }
            @Override public SQLiteDatabase getReadableDatabase() { return db; }
        };
        helper.onCreate(db);
        students = new StudentDAO(context, helper);
    }
    @After public void cleanup() {
        helper.close();
        db.close();
    }
    private Student create(String suffix) {
        String key = prefix + suffix;
        students.insertStudentWithAccount(new Student(key, "Nguyễn An", "an@example.com", "0901234567", 0), key, "secret");
        try (Cursor c = db.rawQuery("SELECT id FROM Account WHERE username = ?", new String[]{key})) {
            assertTrue(c.moveToFirst());
            return students.getStudentByAccountId(c.getInt(0));
        }
    }
    private int count(String table) {
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
            c.moveToFirst(); return c.getInt(0);
        }
    }
    @Test public void createdStudentHasLinkedStudentAccount() {
        Student student = create("a");
        assertNotNull(student);
        try (Cursor c = db.rawQuery("SELECT username, role, password FROM Account WHERE id = ?",
                new String[]{String.valueOf(student.getAccountId())})) {
            assertTrue(c.moveToFirst());
            assertEquals(prefix + "a", c.getString(0));
            assertEquals("STUDENT", c.getString(1));
            assertEquals("secret", c.getString(2));
        }
        assertEquals(prefix + "a", students.getUsername(student.getId()));
    }
    @Test public void duplicateProfileRollsBackNewAccount() {
        Student original = create("a");
        int before = count("Account");
        try {
            students.insertStudentWithAccount(original, prefix + "other", "secret");
            fail("Duplicate code should fail");
        } catch (SQLiteConstraintException expected) { /* The inner transaction rolls back. */ }
        assertEquals(before, count("Account"));
        assertFalse(students.isUsernameTaken(prefix + "other"));
    }
    @Test public void duplicateUsernameDoesNotCreateProfile() {
        create("a");
        int before = count("Student");
        try {
            students.insertStudentWithAccount(new Student(prefix + "b", "Name", "", "", 0), prefix + "a", "secret");
            fail("Duplicate username should fail");
        } catch (SQLiteConstraintException expected) { }
        assertEquals(before, count("Student"));
    }
    @Test public void updatePreservesAccountAndAllowsUnchangedCode() {
        Student student = create("a");
        int accountId = student.getAccountId();
        assertFalse(students.isStudentCodeTaken(student.getStudentCode(), student.getId()));
        student.setFullname("Tên mới");
        student.setAccountId(-100);
        assertEquals(1, students.updateStudent(student));
        assertEquals("Tên mới", students.getStudentByAccountId(accountId).getFullname());
    }
    @Test public void searchTreatsWildcardsLiterallyAndHandlesQuotes() {
        Student student = create("a");
        student.setFullname("O'Neil 100%_unique");
        students.updateStudent(student);
        assertEquals(1, students.searchStudents("100%_unique").size());
        assertEquals(student.getId(), students.searchStudents(prefix + "a").get(0).getId());
        assertTrue(students.searchStudents("' OR 1=1 --").isEmpty());
    }
    @Test public void vietnameseNameRoundTripsAndIsSearchable() {
        Student student = create("unicode");
        String name = "Nguyễn Thị Hồng Ánh";
        student.setFullname(name);
        assertEquals(1, students.updateStudent(student));
        assertEquals(name, students.getStudentByAccountId(student.getAccountId()).getFullname());
        assertEquals(student.getId(), students.searchStudents("Hồng Ánh").get(0).getId());
    }
    @Test public void deleteUsesPersistedAccountAndRemovesSchedules() {
        Student student = create("a");
        Student other = create("b");
        int originalAccount = student.getAccountId();
        ContentValues subject = new ContentValues();
        subject.put("subjectCode", prefix); subject.put("subjectName", "Test"); subject.put("credits", 3);
        long subjectId = db.insertOrThrow("Subject", null, subject);
        ContentValues schedule = new ContentValues();
        schedule.put("studentId", student.getId()); schedule.put("subjectId", subjectId);
        schedule.put("studyDate", "2030-01-01"); schedule.put("startTime", "08:00");
        schedule.put("endTime", "09:00"); schedule.put("location", "Test"); schedule.put("status", "Pending");
        long scheduleId = db.insertOrThrow("StudySchedule", null, schedule);
        student.setAccountId(other.getAccountId());
        assertEquals(1, students.deleteStudent(student));
        assertNull(students.getStudentByAccountId(originalAccount));
        assertFalse(students.isUsernameTaken(prefix + "a"));
        assertTrue(students.isUsernameTaken(prefix + "b"));
        assertNotNull(students.getStudentByAccountId(other.getAccountId()));
        try (Cursor c = db.rawQuery("SELECT id FROM StudySchedule WHERE id = ?", new String[]{String.valueOf(scheduleId)})) {
            assertFalse(c.moveToFirst());
        }
        assertEquals(0, students.deleteStudent(student));
    }
}

package com.example.studyschedule.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studyschedule.utils.Constants;

/**
 * DBHelper theo đúng cấu trúc chuẩn của đề thi:
 * com.example.studyschedule
 * ├── activities
 * ├── dal
 * │   └── DBHelper.java
 * ├── models
 * └── MainActivity.java
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StudyScheduleManagement.db";
    public static final int DATABASE_VERSION = 1;

    // 1. Table Account(id, username, password, role)
    public static final String TABLE_ACCOUNT = "Account";
    public static final String COL_ACC_ID = "id";
    public static final String COL_ACC_USERNAME = "username";
    public static final String COL_ACC_PASSWORD = "password";
    public static final String COL_ACC_ROLE = "role";

    // 2. Table Student(id, studentCode, fullname, email, phone, accountId)
    public static final String TABLE_STUDENT = "Student";
    public static final String COL_STU_ID = "id";
    public static final String COL_STU_CODE = "studentCode";
    public static final String COL_STU_FULLNAME = "fullname";
    public static final String COL_STU_EMAIL = "email";
    public static final String COL_STU_PHONE = "phone";
    public static final String COL_STU_ACCOUNT_ID = "accountId";

    // 3. Table Subject(id, subjectCode, subjectName, description, credits)
    public static final String TABLE_SUBJECT = "Subject";
    public static final String COL_SUB_ID = "id";
    public static final String COL_SUB_CODE = "subjectCode";
    public static final String COL_SUB_NAME = "subjectName";
    public static final String COL_SUB_DESC = "description";
    public static final String COL_SUB_CREDITS = "credits";

    // 4. Table StudySchedule(id, studentId, subjectId, studyDate, startTime, endTime, location, status)
    public static final String TABLE_SCHEDULE = "StudySchedule";
    public static final String COL_SCH_ID = "id";
    public static final String COL_SCH_STUDENT_ID = "studentId";
    public static final String COL_SCH_SUBJECT_ID = "subjectId";
    public static final String COL_SCH_DATE = "studyDate";
    public static final String COL_SCH_START_TIME = "startTime";
    public static final String COL_SCH_END_TIME = "endTime";
    public static final String COL_SCH_LOCATION = "location";
    public static final String COL_SCH_STATUS = "status";

    private static DBHelper instance;

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAccountTable = "CREATE TABLE " + TABLE_ACCOUNT + " ("
                + COL_ACC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACC_USERNAME + " TEXT NOT NULL UNIQUE, "
                + COL_ACC_PASSWORD + " TEXT NOT NULL, "
                + COL_ACC_ROLE + " TEXT NOT NULL)";

        String createStudentTable = "CREATE TABLE " + TABLE_STUDENT + " ("
                + COL_STU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_STU_CODE + " TEXT NOT NULL UNIQUE, "
                + COL_STU_FULLNAME + " TEXT NOT NULL, "
                + COL_STU_EMAIL + " TEXT, "
                + COL_STU_PHONE + " TEXT, "
                + COL_STU_ACCOUNT_ID + " INTEGER, "
                + "FOREIGN KEY(" + COL_STU_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COL_ACC_ID + ") ON DELETE CASCADE)";

        String createSubjectTable = "CREATE TABLE " + TABLE_SUBJECT + " ("
                + COL_SUB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SUB_CODE + " TEXT NOT NULL UNIQUE, "
                + COL_SUB_NAME + " TEXT NOT NULL, "
                + COL_SUB_DESC + " TEXT, "
                + COL_SUB_CREDITS + " INTEGER NOT NULL)";

        String createScheduleTable = "CREATE TABLE " + TABLE_SCHEDULE + " ("
                + COL_SCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SCH_STUDENT_ID + " INTEGER NOT NULL, "
                + COL_SCH_SUBJECT_ID + " INTEGER NOT NULL, "
                + COL_SCH_DATE + " TEXT NOT NULL, "
                + COL_SCH_START_TIME + " TEXT NOT NULL, "
                + COL_SCH_END_TIME + " TEXT NOT NULL, "
                + COL_SCH_LOCATION + " TEXT NOT NULL, "
                + COL_SCH_STATUS + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_SCH_STUDENT_ID + ") REFERENCES " + TABLE_STUDENT + "(" + COL_STU_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + COL_SCH_SUBJECT_ID + ") REFERENCES " + TABLE_SUBJECT + "(" + COL_SUB_ID + ") ON DELETE CASCADE)";

        db.execSQL(createAccountTable);
        db.execSQL(createStudentTable);
        db.execSQL(createSubjectTable);
        db.execSQL(createScheduleTable);

        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // 1. Seed Accounts (admin/123, sv01/123, sv02/123)
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('admin', '123', '" + Constants.ROLE_ADMIN + "')");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('sv01', '123', '" + Constants.ROLE_STUDENT + "')");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('sv02', '123', '" + Constants.ROLE_STUDENT + "')");

        // 2. Seed Students
        db.execSQL("INSERT INTO " + TABLE_STUDENT + " (studentCode, fullname, email, phone, accountId) "
                + "VALUES ('SE17001', 'Nguyen Van An', 'annv@fpt.edu.vn', '0901234567', 2)");
        db.execSQL("INSERT INTO " + TABLE_STUDENT + " (studentCode, fullname, email, phone, accountId) "
                + "VALUES ('SE17002', 'Tran Thi Binh', 'binhtt@fpt.edu.vn', '0912345678', 3)");

        // 3. Seed Subjects
        db.execSQL("INSERT INTO " + TABLE_SUBJECT + " (subjectCode, subjectName, description, credits) "
                + "VALUES ('PRM392', 'Mobile Programming', 'Android Application Development with Java', 3)");
        db.execSQL("INSERT INTO " + TABLE_SUBJECT + " (subjectCode, subjectName, description, credits) "
                + "VALUES ('SWD392', 'Software Architecture & Design', 'Architecture patterns and system design', 3)");
        db.execSQL("INSERT INTO " + TABLE_SUBJECT + " (subjectCode, subjectName, description, credits) "
                + "VALUES ('PRN211', 'Cross-Platform .NET', 'C# and .NET Application Development', 3)");

        // 4. Seed Sample StudySchedules
        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (studentId, subjectId, studyDate, startTime, endTime, location, status) "
                + "VALUES (1, 1, '2026-10-31', '08:00', '10:00', 'Room BE-301', '" + Constants.STATUS_CONFIRMED + "')");
        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (studentId, subjectId, studyDate, startTime, endTime, location, status) "
                + "VALUES (1, 2, '2026-11-01', '13:30', '15:30', 'Library Study Pod 2', '" + Constants.STATUS_PENDING + "')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        onCreate(db);
    }
}

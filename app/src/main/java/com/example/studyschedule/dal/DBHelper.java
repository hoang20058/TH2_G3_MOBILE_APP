package com.example.studyschedule.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studyschedule.utils.Constants;

/**
 * DBHelper chuẩn theo cấu trúc đề thi Test 2 (SQLite) & nạp sẵn dữ liệu mẫu khớp với thiết kế UI của nhóm.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StudyScheduleManagement.db";
    public static final int DATABASE_VERSION = 2;

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

    /**
     * Dữ liệu mẫu khớp 100% với bản thiết kế giao diện STUDY PLANNER của nhóm:
     * - Sinh viên: SV001 (Nguyễn Minh Anh), SV002 (Trần Hoàng Nam), SV003 (Lê Thu Hà)
     * - Môn học: INT1306 (Cơ sở dữ liệu), INT1310 (Lập trình Java), INT1313 (Mạng máy tính)
     * - Lịch học: 05/11/2026 (08:00 - 09:30, Cơ sở dữ liệu, Thư viện A2, Confirmed)
     *             06/11/2026 (14:00 - 15:30, Lập trình Java, Thư viện A2, Pending)
     */
    private void seedInitialData(SQLiteDatabase db) {
        // 1. Seed Accounts
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('admin', '123', '" + Constants.ROLE_ADMIN + "')");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('sv01', '123', '" + Constants.ROLE_STUDENT + "')");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('sv02', '123', '" + Constants.ROLE_STUDENT + "')");
        db.execSQL("INSERT INTO " + TABLE_ACCOUNT + " (username, password, role) VALUES ('sv03', '123', '" + Constants.ROLE_STUDENT + "')");

        // 2. Seed Students
        db.execSQL("INSERT INTO " + TABLE_STUDENT + " (studentCode, fullname, email, phone, accountId) "
                + "VALUES ('SV001', 'Nguyễn Minh Anh', 'minhanh@example.com', '0901234567', 2)");
        db.execSQL("INSERT INTO " + TABLE_STUDENT + " (studentCode, fullname, email, phone, accountId) "
                + "VALUES ('SV002', 'Trần Hoàng Nam', 'hoangnam@example.com', '0912345678', 3)");
        db.execSQL("INSERT INTO " + TABLE_STUDENT + " (studentCode, fullname, email, phone, accountId) "
                + "VALUES ('SV003', 'Lê Thu Hà', 'thuha@example.com', '0923456789', 4)");

        // 3. Seed Subjects
        db.execSQL("INSERT INTO " + TABLE_SUBJECT + " (subjectCode, subjectName, description, credits) "
                + "VALUES ('INT1306', 'Cơ sở dữ liệu', 'Hệ quản trị cơ sở dữ liệu SQL', 3)");
        db.execSQL("INSERT INTO " + TABLE_SUBJECT + " (subjectCode, subjectName, description, credits) "
                + "VALUES ('INT1310', 'Lập trình Java', 'Lập trình hướng đối tượng với Java', 3)");
        db.execSQL("INSERT INTO " + TABLE_SUBJECT + " (subjectCode, subjectName, description, credits) "
                + "VALUES ('INT1313', 'Mạng máy tính', 'Kiến trúc và giao thức mạng máy tính', 3)");

        // 4. Seed StudySchedules (cho SV001 - studentId = 1)
        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (studentId, subjectId, studyDate, startTime, endTime, location, status) "
                + "VALUES (1, 1, '2026-11-05', '08:00', '09:30', 'Thư viện A2', '" + Constants.STATUS_CONFIRMED + "')");
        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (studentId, subjectId, studyDate, startTime, endTime, location, status) "
                + "VALUES (1, 2, '2026-11-06', '14:00', '15:30', 'Thư viện A2', '" + Constants.STATUS_PENDING + "')");
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

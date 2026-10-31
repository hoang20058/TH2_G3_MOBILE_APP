package com.example.studyschedule.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý thông tin đăng nhập bằng SharedPreferences (Yêu cầu: Login Account Storage).
 * Các thành viên lấy studentId hoặc role hiện tại thông qua class này.
 */
public class SessionManager {

    private static final String PREF_NAME = "StudyScheduleSessionPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_STUDENT_ID = "studentId"; // -1 nếu là ADMIN
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Lưu phiên đăng nhập sau khi xác thực thành công.
     */
    public void saveLoginSession(int accountId, int studentId, String username, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_ACCOUNT_ID, accountId);
        editor.putInt(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getAccountId() {
        return prefs.getInt(KEY_ACCOUNT_ID, -1);
    }

    public int getStudentId() {
        return prefs.getInt(KEY_STUDENT_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public boolean isStudent() {
        return Constants.ROLE_STUDENT.equalsIgnoreCase(getRole());
    }

    /**
     * Xóa thông tin đăng nhập khi Logout.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}

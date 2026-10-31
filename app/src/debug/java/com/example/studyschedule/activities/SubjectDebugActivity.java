package com.example.studyschedule.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;
import com.example.studyschedule.R;
import com.example.studyschedule.dal.DBHelper;
import com.example.studyschedule.utils.Constants;
import com.example.studyschedule.utils.SessionManager;

/** Cổng chạy thử module 3, được loại hoàn toàn khỏi bản release. */
public class SubjectDebugActivity extends SubjectManagementActivity {
    @Override protected SessionManager createSessionManager() {
        Context isolatedContext = new ContextWrapper(getApplicationContext()) {
            @Override public Context getApplicationContext() { return this; }

            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences(name + "_subject_debug", mode);
            }
        };
        SessionManager debugSession = new SessionManager(isolatedContext);
        debugSession.logout();
        // Đọc Admin có sẵn; không sửa Account, không ghi phiên của Login/Main.
        try (Cursor cursor = DBHelper.getInstance(this).getReadableDatabase().query(
                DBHelper.TABLE_ACCOUNT,
                new String[]{DBHelper.COL_ACC_ID, DBHelper.COL_ACC_USERNAME, DBHelper.COL_ACC_ROLE},
                DBHelper.COL_ACC_ROLE + " = ? COLLATE NOCASE",
                new String[]{Constants.ROLE_ADMIN}, null, null, DBHelper.COL_ACC_ID, "1")) {
            if (cursor.moveToFirst()) {
                debugSession.saveLoginSession(cursor.getInt(0), -1, cursor.getString(1), cursor.getString(2));
                Toast.makeText(this, R.string.subject_debug_notice, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.subject_debug_no_admin, Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            Log.e("SubjectDebug", "Cannot create debug session", e);
            Toast.makeText(this, R.string.subject_database_error, Toast.LENGTH_LONG).show();
        }
        return debugSession;
    }
}

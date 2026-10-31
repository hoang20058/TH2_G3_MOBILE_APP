package com.example.studyschedule.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Separate persistent database for running module 2 before the login module is merged. */
public final class StudentModuleDemoData {
    private static DBHelper bridge;
    private StudentModuleDemoData() {}

    public static synchronized StudentDAO createDao(Context context) {
        Context app = context.getApplicationContext();
        if (bridge == null) {
            SQLiteOpenHelper demo = new SQLiteOpenHelper(app, "student_module_demo.db", null, 1) {
                @Override public void onConfigure(SQLiteDatabase db) {
                    db.setForeignKeyConstraintsEnabled(true);
                }
                @Override public void onCreate(SQLiteDatabase db) {
                    new DBHelper(app).onCreate(db);
                }
                @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                    throw new IllegalStateException("Define a demo database migration before changing its version.");
                }
            };
            bridge = new DBHelper(app) {
                @Override public SQLiteDatabase getWritableDatabase() { return demo.getWritableDatabase(); }
                @Override public SQLiteDatabase getReadableDatabase() { return demo.getReadableDatabase(); }
                @Override public synchronized void close() { demo.close(); }
            };
        }
        return new StudentDAO(app, bridge);
    }
}

package com.example.studyschedule.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ScheduleAlarmReceiver cơ bản trong khung chung (Người 5 phát triển trên nhánh feature/calendar-notifications).
 */
public class ScheduleAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        NotificationHelper.sendInstantNotification(
                context,
                title != null ? title : "Study Reminder",
                message != null ? message : "You have an upcoming study session!"
        );
    }
}

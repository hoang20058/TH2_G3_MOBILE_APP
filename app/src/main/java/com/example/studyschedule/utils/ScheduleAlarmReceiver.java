package com.example.studyschedule.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Phụ trách: Người 5 (Minh - nhánh feature/calendar-notifications).
 * Nhận sự kiện từ AlarmManager khi đến giờ nhắc lịch học sắp diễn ra.
 */
public class ScheduleAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        NotificationHelper.sendInstantNotification(
                context,
                title != null ? title : "Upcoming Study Session Reminder",
                message != null ? message : "Your study session is starting in 15 minutes!"
        );
    }
}

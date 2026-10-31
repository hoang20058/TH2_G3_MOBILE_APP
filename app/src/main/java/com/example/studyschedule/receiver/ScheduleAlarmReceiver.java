package com.example.studyschedule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.studyschedule.utils.NotificationHelper;

/**
 * BroadcastReceiver nhận sự kiện nhắc lịch học sắp tới (Phụ trách chính: Thành viên 5).
 */
public class ScheduleAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        if (title == null) title = "Upcoming Study Session";
        if (message == null) message = "You have a scheduled study session starting soon!";

        NotificationHelper.sendInstantNotification(context, title, message);
    }
}

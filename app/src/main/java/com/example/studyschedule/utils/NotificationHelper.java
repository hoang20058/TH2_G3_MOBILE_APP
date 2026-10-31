package com.example.studyschedule.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.studyschedule.receiver.ScheduleAlarmReceiver;

/**
 * Helper gửi thông báo (Notifications) khi đặt lịch/cập nhật lịch và hẹn giờ nhắc lịch học sắp tới.
 * Phụ trách chính: Thành viên 5 (Thành viên 4 có thể gọi hàm sendInstantNotification khi lưu lịch).
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "study_schedule_channel";
    public static final String CHANNEL_NAME = "Study Schedule Notifications";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for upcoming study sessions and schedule changes");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Gửi thông báo ngay lập tức khi sinh viên tạo mới, cập nhật hoặc đổi trạng thái lịch học.
     */
    public static void sendInstantNotification(Context context, String title, String message) {
        createNotificationChannel(context);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) (System.currentTimeMillis() % 100000), builder.build());
    }

    /**
     * Đặt báo thức nhắc nhở trước buổi học (Dành cho Thành viên 5 mở rộng với AlarmManager).
     */
    public static void scheduleSessionReminder(Context context, int scheduleId, long triggerAtMillis, String title, String message) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ScheduleAlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (triggerAtMillis > System.currentTimeMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}

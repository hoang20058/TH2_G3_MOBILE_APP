package com.example.studyschedule.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.studyschedule.activities.CalendarScheduleActivity;
import com.example.studyschedule.models.StudySchedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Phụ trách: Người 5 (Minh - nhánh feature/calendar-notifications).
 * Xử lý:
 * - Nhắc lịch học sắp đến bằng AlarmManager & NotificationCompat.
 * - Cập nhật hoặc hủy nhắc lịch khi lịch học thay đổi trạng thái (Cancelled) hoặc bị xóa.
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "study_schedule_channel";
    public static final String CHANNEL_NAME = "Study Schedule Notifications";
    // Nhắc trước giờ bắt đầu học 15 phút
    private static final long REMINDER_BEFORE_MILLIS = 15 * 60 * 1000L;

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
     * Hiển thị thông báo ngay lập tức trên thanh trạng thái.
     */
    public static void sendInstantNotification(Context context, String title, String message) {
        createNotificationChannel(context);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent openIntent = new Intent(context, CalendarScheduleActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        manager.notify((int) (System.currentTimeMillis() % 100000), builder.build());
    }

    /**
     * Đặt mới hoặc cập nhật hẹn giờ nhắc cho 1 lịch học.
     * Nếu lịch chuyển sang Cancelled thì tự động hủy hẹn giờ nhắc.
     */
    public static void scheduleOrUpdateReminder(Context context, StudySchedule schedule) {
        if (schedule == null || schedule.getId() <= 0) return;

        if (Constants.STATUS_CANCELLED.equalsIgnoreCase(schedule.getStatus())) {
            cancelReminder(context, schedule.getId());
            sendInstantNotification(
                    context,
                    "Schedule Cancelled - Reminder Removed",
                    formatSubjectTitle(schedule) + " on " + schedule.getStudyDate()
                            + " (" + schedule.getStartTime() + ") has been cancelled."
            );
            return;
        }

        long startMillis = parseDateTimeMillis(schedule.getStudyDate(), schedule.getStartTime());
        if (startMillis <= 0) return;

        long triggerAtMillis = startMillis - REMINDER_BEFORE_MILLIS;
        long now = System.currentTimeMillis();
        if (triggerAtMillis <= now && startMillis > now) {
            triggerAtMillis = now + 5000L;
        }

        String title = "Upcoming Study Session: " + formatSubjectTitle(schedule);
        String message = "Date: " + schedule.getStudyDate()
                + " | Time: " + schedule.getStartTime() + " - " + schedule.getEndTime()
                + " | Location: " + schedule.getLocation();

        scheduleAlarm(context, schedule.getId(), triggerAtMillis, title, message);
    }

    /**
     * Đặt hẹn giờ bằng AlarmManager.
     */
    public static void scheduleAlarm(Context context, int scheduleId, long triggerAtMillis, String title, String message) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ScheduleAlarmReceiver.class);
        intent.putExtra("scheduleId", scheduleId);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (triggerAtMillis > System.currentTimeMillis()) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } catch (SecurityException e) {
                sendInstantNotification(context, title, message);
            }
        }
    }

    /**
     * Hủy nhắc lịch khi lịch bị hủy (Cancelled) hoặc bị xóa (Deleted).
     */
    public static void cancelReminder(Context context, int scheduleId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ScheduleAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Đồng bộ nhắc lịch cho toàn bộ các buổi học sắp đến của sinh viên.
     */
    public static int syncAllReminders(Context context, List<StudySchedule> allSchedules, boolean notifySummary) {
        if (allSchedules == null || allSchedules.isEmpty()) return 0;

        int activeUpcomingCount = 0;
        StudySchedule nearestUpcoming = null;
        String today = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US).format(new Date());

        for (StudySchedule item : allSchedules) {
            if (Constants.STATUS_CANCELLED.equalsIgnoreCase(item.getStatus())) {
                cancelReminder(context, item.getId());
                continue;
            }
            scheduleOrUpdateReminder(context, item);
            if (item.getStudyDate() != null && item.getStudyDate().compareTo(today) >= 0) {
                activeUpcomingCount++;
                if (nearestUpcoming == null) {
                    nearestUpcoming = item;
                }
            }
        }

        if (notifySummary && nearestUpcoming != null) {
            sendInstantNotification(
                    context,
                    "Upcoming Study Reminder (" + activeUpcomingCount + " session(s))",
                    "Next session: " + formatSubjectTitle(nearestUpcoming)
                            + " on " + nearestUpcoming.getStudyDate()
                            + " (" + nearestUpcoming.getStartTime() + " - " + nearestUpcoming.getEndTime()
                            + ") at " + nearestUpcoming.getLocation()
            );
        }
        return activeUpcomingCount;
    }

    private static String formatSubjectTitle(StudySchedule s) {
        if (s.getSubjectCode() != null && s.getSubjectName() != null) {
            return s.getSubjectCode() + " - " + s.getSubjectName();
        }
        if (s.getSubjectName() != null) {
            return s.getSubjectName();
        }
        return "Subject #" + s.getSubjectId();
    }

    private static long parseDateTimeMillis(String studyDate, String startTime) {
        if (studyDate == null || startTime == null) return -1;
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT + " " + Constants.TIME_FORMAT, Locale.US);
        try {
            Date parsed = sdf.parse(studyDate.trim() + " " + startTime.trim());
            return parsed != null ? parsed.getTime() : -1;
        } catch (ParseException e) {
            return -1;
        }
    }
}

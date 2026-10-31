package com.example.studyschedule.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.models.StudySchedule;
import com.example.studyschedule.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Shared schedule cards. The no-argument constructor remains available to member 5. */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
    public interface OnScheduleActionListener {
        void onView(StudySchedule schedule);
        void onEdit(StudySchedule schedule);
        void onDelete(StudySchedule schedule);
        void onChangeStatus(StudySchedule schedule);
    }

    private List<StudySchedule> scheduleList = new ArrayList<>();
    private OnScheduleActionListener listener;

    public ScheduleAdapter() { }

    public ScheduleAdapter(OnScheduleActionListener listener) {
        this.listener = listener;
    }

    public void setOnScheduleActionListener(OnScheduleActionListener listener) {
        this.listener = listener;
    }

    public void setScheduleList(List<StudySchedule> list) {
        scheduleList = list == null ? new ArrayList<>() : new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        StudySchedule item = scheduleList.get(position);
        holder.subject.setText(subjectTitle(item));
        holder.dateTime.setText(String.format(Locale.getDefault(), "%s · %s – %s",
                displayDate(item.getStudyDate(), false), item.getStartTime(), item.getEndTime()));
        holder.location.setText(item.getLocation());
        holder.status.setText(statusLabel(item.getStatus()));
        holder.status.setContentDescription("Đổi trạng thái: " + statusLabel(item.getStatus()));
        int foreground = Color.rgb(237, 118, 45);
        int background = Color.rgb(255, 240, 229);
        if (Constants.STATUS_CONFIRMED.equals(item.getStatus())) {
            foreground = Color.rgb(36, 155, 69);
            background = Color.rgb(234, 247, 239);
        } else if (Constants.STATUS_CANCELLED.equals(item.getStatus())) {
            foreground = Color.rgb(240, 68, 100);
            background = Color.rgb(255, 234, 240);
        }
        holder.status.setTextColor(foreground);
        GradientDrawable badge = rounded(background, dp(holder.status.getContext(), 8));
        holder.status.setBackground(new InsetDrawable(badge, 0, dp(holder.status.getContext(), 11),
                0, dp(holder.status.getContext(), 11)));
        holder.icon.setBackground(rounded(Color.rgb(241, 236, 255), dp(holder.icon.getContext(), 12)));
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onView(item); });
        holder.edit.setOnClickListener(v -> { if (listener != null) listener.onEdit(item); });
        holder.delete.setOnClickListener(v -> { if (listener != null) listener.onDelete(item); });
        holder.status.setOnClickListener(v -> { if (listener != null) listener.onChangeStatus(item); });
        holder.edit.setVisibility(listener == null ? View.GONE : View.VISIBLE);
        holder.delete.setVisibility(listener == null ? View.GONE : View.VISIBLE);
        holder.status.setClickable(listener != null);
        holder.status.setFocusable(listener != null);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static String subjectTitle(StudySchedule item) {
        return item.getSubjectName() == null ? "Môn học #" + item.getSubjectId() : item.getSubjectName();
    }

    public static String statusLabel(String status) {
        if (Constants.STATUS_CONFIRMED.equals(status)) return "Đã xác nhận";
        if (Constants.STATUS_CANCELLED.equals(status)) return "Đã hủy";
        return "Chờ xác nhận";
    }

    public static String displayDate(String date, boolean includeYear) {
        if (date == null) return "";
        SimpleDateFormat input = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
        input.setLenient(false);
        try {
            return new SimpleDateFormat(includeYear ? "dd/MM/yyyy" : "dd/MM", Locale.getDefault())
                    .format(input.parse(date));
        } catch (ParseException e) {
            return date;
        }
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable rounded(int color, float radius) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(radius);
        return background;
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        final TextView subject, dateTime, location, status, icon;
        final View edit, delete;

        ScheduleViewHolder(@NonNull View view) {
            super(view);
            subject = view.findViewById(R.id.tvScheduleSubject);
            dateTime = view.findViewById(R.id.tvScheduleDateTime);
            location = view.findViewById(R.id.tvScheduleLocationStatus);
            status = view.findViewById(R.id.btnChangeStatus);
            icon = view.findViewById(R.id.tvScheduleIcon);
            edit = view.findViewById(R.id.btnEditSchedule);
            delete = view.findViewById(R.id.btnDeleteSchedule);
        }
    }
}

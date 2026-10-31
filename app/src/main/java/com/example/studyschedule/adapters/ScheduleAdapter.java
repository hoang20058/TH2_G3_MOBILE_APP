package com.example.studyschedule.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.models.StudySchedule;

import java.util.ArrayList;
import java.util.List;

/**
 * ScheduleAdapter (Dùng chung cho Người 4 & Người 5).
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<StudySchedule> scheduleList = new ArrayList<>();

    public void setScheduleList(List<StudySchedule> list) {
        this.scheduleList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        StudySchedule item = scheduleList.get(position);
        String subjectTitle = (item.getSubjectCode() != null ? item.getSubjectCode() + " - " : "")
                + (item.getSubjectName() != null ? item.getSubjectName() : "Subject #" + item.getSubjectId());

        holder.tvScheduleSubject.setText(subjectTitle);
        holder.tvScheduleDateTime.setText("Date: " + item.getStudyDate()
                + " | Time: " + item.getStartTime() + " - " + item.getEndTime());
        holder.tvScheduleLocationStatus.setText("Location: " + item.getLocation()
                + " | Status: " + item.getStatus());
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvScheduleSubject, tvScheduleDateTime, tvScheduleLocationStatus;
        Button btnChangeStatus, btnEditSchedule, btnDeleteSchedule;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScheduleSubject = itemView.findViewById(R.id.tvScheduleSubject);
            tvScheduleDateTime = itemView.findViewById(R.id.tvScheduleDateTime);
            tvScheduleLocationStatus = itemView.findViewById(R.id.tvScheduleLocationStatus);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
            btnEditSchedule = itemView.findViewById(R.id.btnEditSchedule);
            btnDeleteSchedule = itemView.findViewById(R.id.btnDeleteSchedule);
        }
    }
}

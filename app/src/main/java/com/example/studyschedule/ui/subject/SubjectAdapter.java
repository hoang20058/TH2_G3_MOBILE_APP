package com.example.studyschedule.ui.subject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.model.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách Subject (Phụ trách chính: Thành viên 3).
 */
public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList = new ArrayList<>();

    public void setSubjectList(List<Subject> list) {
        this.subjectList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.tvSubjectCodeAndName.setText(subject.getSubjectCode() + " - " + subject.getSubjectName());
        holder.tvSubjectDetails.setText("Credits: " + subject.getCredits() + " | " + subject.getDescription());

        // TODO (Thành viên 3): Gắn listener cho holder.btnEditSubject và holder.btnDeleteSubject
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectCodeAndName, tvSubjectDetails;
        Button btnEditSubject, btnDeleteSubject;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectCodeAndName = itemView.findViewById(R.id.tvSubjectCodeAndName);
            tvSubjectDetails = itemView.findViewById(R.id.tvSubjectDetails);
            btnEditSubject = itemView.findViewById(R.id.btnEditSubject);
            btnDeleteSubject = itemView.findViewById(R.id.btnDeleteSubject);
        }
    }
}

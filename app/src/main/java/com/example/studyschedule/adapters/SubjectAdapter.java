package com.example.studyschedule.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyschedule.R;
import com.example.studyschedule.models.Subject;
import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    public interface Listener {
        void onView(Subject subject);
        void onEdit(Subject subject);
        void onDelete(Subject subject);
    }
    private List<Subject> subjectList = new ArrayList<>();
    private Listener listener;
    public void setListener(Listener listener) { this.listener = listener; }
    public void setSubjectList(List<Subject> list) {
        subjectList = list == null ? new ArrayList<>() : new ArrayList<>(list);
        notifyDataSetChanged();
    }
    @NonNull @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubjectViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.title.setText(subject.getSubjectName());
        holder.code.setText(subject.getSubjectCode());
        holder.details.setText(holder.itemView.getContext().getString(
                R.string.subject_credit_count, subject.getCredits()));
        // Màu/icon trang trí ổn định theo ID, không đổi khi lọc danh sách.
        int variant = Math.floorMod(subject.getId() - 1, 3);
        int[] icons = {R.drawable.subject_database, R.drawable.subject_code, R.drawable.subject_network};
        int[] backgrounds = {R.drawable.subject_icon_purple_background,
                R.drawable.subject_icon_orange_background, R.drawable.subject_icon_blue_background};
        holder.icon.setImageResource(icons[variant]);
        holder.icon.setBackgroundResource(backgrounds[variant]);
        holder.itemView.findViewById(R.id.btnEditSubject).setContentDescription(
                holder.itemView.getContext().getString(R.string.subject_edit_named, subject.getSubjectName()));
        holder.itemView.findViewById(R.id.btnDeleteSubject).setContentDescription(
                holder.itemView.getContext().getString(R.string.subject_delete_named, subject.getSubjectName()));
    }
    @Override public int getItemCount() { return subjectList.size(); }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        final TextView title, code, details;
        final ImageView icon;
        SubjectViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvSubjectCodeAndName);
            code = view.findViewById(R.id.tvSubjectCode);
            icon = view.findViewById(R.id.ivSubjectIcon);
            details = view.findViewById(R.id.tvSubjectDetails);
            view.setOnClickListener(v -> dispatch(0));
            view.findViewById(R.id.btnEditSubject).setOnClickListener(v -> dispatch(1));
            view.findViewById(R.id.btnDeleteSubject).setOnClickListener(v -> dispatch(2));
        }
        private void dispatch(int action) {
            // Lấy vị trí hiện tại, không giữ position cũ sau khi tìm kiếm/làm mới.
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION || listener == null) return;
            Subject subject = subjectList.get(position);
            if (action == 0) listener.onView(subject);
            else if (action == 1) listener.onEdit(subject);
            else listener.onDelete(subject);
        }
    }
}

package com.example.studyschedule.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.content.res.ColorStateList;
import java.util.Locale;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.models.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * StudentAdapter (Người 2 - feature/student-management).
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList = new ArrayList<>();
    public interface Listener {
        void onView(Student student);
        void onEdit(Student student);
        void onDelete(Student student);
    }
    private Listener listener;
    public void setListener(Listener listener) { this.listener = listener; }

    public void setStudentList(List<Student> list) {
        this.studentList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvStudentCodeAndName.setText(student.getFullname());
        holder.tvStudentCode.setText(student.getStudentCode());
        String[] words = student.getFullname().trim().split("\\s+");
        String initials = "";
        if (words.length > 1) initials += words[words.length - 2].substring(0, 1);
        if (words.length > 0 && !words[words.length - 1].isEmpty()) initials += words[words.length - 1].substring(0, 1);
        holder.tvStudentAvatar.setText(initials.toUpperCase(Locale.ROOT));
        int[] backgrounds = {0xFFEFE9FF, 0xFFFFEBDD, 0xFFE3EFFF};
        int[] colors = {0xFF8058E8, 0xFFE47835, 0xFF2778EF};
        int palette = Math.floorMod(student.getId() - 1, backgrounds.length);
        holder.tvStudentAvatar.setBackgroundTintList(ColorStateList.valueOf(backgrounds[palette]));
        holder.tvStudentAvatar.setTextColor(colors[palette]);
        holder.btnEditStudent.setContentDescription(holder.itemView.getContext().getString(R.string.student_edit_action, student.getFullname()));
        holder.btnDeleteStudent.setContentDescription(holder.itemView.getContext().getString(R.string.student_delete_action, student.getFullname()));
        holder.tvStudentContact.setText(student.getEmail() != null && !student.getEmail().isEmpty()
                ? student.getEmail() : student.getPhone());
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onView(student); });
        holder.btnEditStudent.setOnClickListener(v -> { if (listener != null) listener.onEdit(student); });
        holder.btnDeleteStudent.setOnClickListener(v -> { if (listener != null) listener.onDelete(student); });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentCodeAndName, tvStudentContact, tvStudentCode, tvStudentAvatar;
        ImageButton btnEditStudent, btnDeleteStudent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentCodeAndName = itemView.findViewById(R.id.tvStudentCodeAndName);
            tvStudentContact = itemView.findViewById(R.id.tvStudentContact);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvStudentAvatar = itemView.findViewById(R.id.tvStudentAvatar);
            btnEditStudent = itemView.findViewById(R.id.btnEditStudent);
            btnDeleteStudent = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}

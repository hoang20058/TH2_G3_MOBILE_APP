package com.example.studyschedule.ui.student;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.model.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách Student (Phụ trách chính: Thành viên 2).
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList = new ArrayList<>();

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
        holder.tvStudentCodeAndName.setText(student.getStudentCode() + " - " + student.getFullname());
        holder.tvStudentContact.setText("Email: " + student.getEmail() + " | Phone: " + student.getPhone());

        // TODO (Thành viên 2): Gắn listener cho holder.btnEditStudent và holder.btnDeleteStudent
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentCodeAndName, tvStudentContact;
        Button btnEditStudent, btnDeleteStudent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentCodeAndName = itemView.findViewById(R.id.tvStudentCodeAndName);
            tvStudentContact = itemView.findViewById(R.id.tvStudentContact);
            btnEditStudent = itemView.findViewById(R.id.btnEditStudent);
            btnDeleteStudent = itemView.findViewById(R.id.btnDeleteStudent);
        }
    }
}

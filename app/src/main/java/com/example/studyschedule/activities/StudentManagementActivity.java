package com.example.studyschedule.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.adapters.StudentAdapter;
import com.example.studyschedule.dal.StudentDAO;
import com.example.studyschedule.models.Student;

import java.util.List;

/**
 * StudentManagementActivity (Người 2 - feature/student-management).
 */
public class StudentManagementActivity extends AppCompatActivity {

    private EditText edtSearchStudent;
    private Button btnSearchStudent, btnAddStudent;
    private RecyclerView rvStudents;
    private StudentDAO studentDAO;
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);

        studentDAO = new StudentDAO(this);

        edtSearchStudent = findViewById(R.id.edtSearchStudent);
        btnSearchStudent = findViewById(R.id.btnSearchStudent);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        rvStudents = findViewById(R.id.rvStudents);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter();
        rvStudents.setAdapter(adapter);

        loadStudents("");

        btnSearchStudent.setOnClickListener(v -> {
            String keyword = edtSearchStudent.getText().toString().trim();
            loadStudents(keyword);
        });

        btnAddStudent.setOnClickListener(v -> {
            // TODO (Người 2): Mở dialog thêm sinh viên và tạo tài khoản Student
        });
    }

    private void loadStudents(String keyword) {
        List<Student> list = keyword.isEmpty()
                ? studentDAO.getAllStudents()
                : studentDAO.searchStudents(keyword);
        adapter.setStudentList(list);
    }
}

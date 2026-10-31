package com.example.studyschedule.ui.student;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.dao.StudentDao;
import com.example.studyschedule.model.Student;

import java.util.List;

/**
 * Màn hình Quản lý Sinh viên dành cho Admin (Phụ trách chính: Thành viên 2).
 * Chức năng yêu cầu:
 * - Xem danh sách sinh viên (View)
 * - Tìm kiếm sinh viên (Search)
 * - Thêm mới sinh viên kèm tạo Account (Add -> gọi studentDao.insertStudentWithAccount(...))
 * - Cập nhật thông tin sinh viên (Update -> gọi studentDao.updateStudent(...))
 * - Xóa sinh viên (Delete -> gọi studentDao.deleteStudent(...))
 */
public class StudentManagementActivity extends AppCompatActivity {

    private EditText edtSearchStudent;
    private Button btnSearchStudent, btnAddStudent;
    private RecyclerView rvStudents;
    private StudentDao studentDao;
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_management);

        studentDao = new StudentDao(this);

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
            // TODO (Thành viên 2): Mở AlertDialog sử dụng R.layout.dialog_student_form để thêm mới Student
        });
    }

    private void loadStudents(String keyword) {
        List<Student> list = keyword.isEmpty()
                ? studentDao.getAllStudents()
                : studentDao.searchStudents(keyword);
        adapter.setStudentList(list);
    }
}

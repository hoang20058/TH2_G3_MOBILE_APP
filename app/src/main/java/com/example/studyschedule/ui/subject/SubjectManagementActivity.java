package com.example.studyschedule.ui.subject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.dao.SubjectDao;
import com.example.studyschedule.model.Subject;

import java.util.List;

/**
 * Màn hình Quản lý Môn học dành cho Admin (Phụ trách chính: Thành viên 3).
 * Chức năng yêu cầu:
 * - Xem danh sách môn học (View)
 * - Tìm kiếm môn học (Search)
 * - Thêm mới môn học (Add -> gọi subjectDao.insertSubject(...))
 * - Cập nhật môn học (Update -> gọi subjectDao.updateSubject(...))
 * - Xóa môn học (Delete -> gọi subjectDao.deleteSubject(...))
 */
public class SubjectManagementActivity extends AppCompatActivity {

    private EditText edtSearchSubject;
    private Button btnSearchSubject, btnAddSubject;
    private RecyclerView rvSubjects;
    private SubjectDao subjectDao;
    private SubjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_management);

        subjectDao = new SubjectDao(this);

        edtSearchSubject = findViewById(R.id.edtSearchSubject);
        btnSearchSubject = findViewById(R.id.btnSearchSubject);
        btnAddSubject = findViewById(R.id.btnAddSubject);
        rvSubjects = findViewById(R.id.rvSubjects);

        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter();
        rvSubjects.setAdapter(adapter);

        loadSubjects("");

        btnSearchSubject.setOnClickListener(v -> {
            String keyword = edtSearchSubject.getText().toString().trim();
            loadSubjects(keyword);
        });

        btnAddSubject.setOnClickListener(v -> {
            // TODO (Thành viên 3): Mở AlertDialog sử dụng R.layout.dialog_subject_form để thêm mới Subject
        });
    }

    private void loadSubjects(String keyword) {
        List<Subject> list = keyword.isEmpty()
                ? subjectDao.getAllSubjects()
                : subjectDao.searchSubjects(keyword);
        adapter.setSubjectList(list);
    }
}

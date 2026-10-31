package com.example.studyschedule.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyschedule.R;
import com.example.studyschedule.adapters.SubjectAdapter;
import com.example.studyschedule.dal.SubjectDAO;
import com.example.studyschedule.models.Subject;

import java.util.List;

/**
 * SubjectManagementActivity (Người 3 - feature/subject-management).
 */
public class SubjectManagementActivity extends AppCompatActivity {

    private EditText edtSearchSubject;
    private Button btnSearchSubject, btnAddSubject;
    private RecyclerView rvSubjects;
    private SubjectDAO subjectDAO;
    private SubjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_management);

        subjectDAO = new SubjectDAO(this);

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
            // TODO (Người 3): Mở dialog thêm môn học
        });
    }

    private void loadSubjects(String keyword) {
        List<Subject> list = keyword.isEmpty()
                ? subjectDAO.getAllSubjects()
                : subjectDAO.searchSubjects(keyword);
        adapter.setSubjectList(list);
    }
}

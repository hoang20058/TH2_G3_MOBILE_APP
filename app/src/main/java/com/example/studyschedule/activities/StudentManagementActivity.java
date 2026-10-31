package com.example.studyschedule.activities;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyschedule.R;
import com.example.studyschedule.adapters.StudentAdapter;
import com.example.studyschedule.dal.StudentDAO;
import com.example.studyschedule.models.Student;
import com.example.studyschedule.utils.SessionManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentManagementActivity extends AppCompatActivity {
    private StudentDAO dao;
    private StudentAdapter adapter;
    private EditText search;
    private TextView empty;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private int queryVersion;

    @Override protected void onCreate(Bundle state) {
        setTheme(R.style.Theme_StudentModule);
        super.onCreate(state);
        setContentView(R.layout.activity_student_management);
        setTitle("Quản lý sinh viên");
        dao = createStudentDao();
        search = findViewById(R.id.edtSearchStudent);
        empty = findViewById(R.id.tvEmptyStudents);
        adapter = new StudentAdapter();
        adapter.setListener(new StudentAdapter.Listener() {
            public void onView(Student s) { showDetail(s); }
            public void onEdit(Student s) { showForm(s); }
            public void onDelete(Student s) { confirmDelete(s); }
        });
        RecyclerView list = findViewById(R.id.rvStudents);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        findViewById(R.id.btnAddStudent).setOnClickListener(v -> showForm(null));
        findViewById(R.id.btnSearchStudent).setOnClickListener(v -> reload());
        findViewById(R.id.btnStudentQuickAdd).setOnClickListener(v -> showForm(null));
        findViewById(R.id.btnStudentRefresh).setOnClickListener(v -> reload());
        findViewById(R.id.btnStudentList).setOnClickListener(v -> { search.setText(""); reload(); });
        search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                reload();
                ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(search.getWindowToken(), 0);
                return true;
            }
            return false;
        });
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { reload(); }
            public void afterTextChanged(Editable s) {}
        });
    }
    protected StudentDAO createStudentDao() { return new StudentDAO(this); }

    protected boolean hasModuleAccess() {
        SessionManager session = new SessionManager(this);
        return session.isLoggedIn() && session.isAdmin();
    }

    private boolean requireAdmin() {
        if (hasModuleAccess()) return true;
        Toast.makeText(this, "Chỉ Admin được quản lý sinh viên.", Toast.LENGTH_SHORT).show();
        finish();
        return false;
    }
    @Override protected void onResume() {
        super.onResume();
        if (requireAdmin()) reload();
    }
    private void reload() {
        final int version = ++queryVersion;
        final String keyword = search.getText().toString().trim();
        worker.execute(() -> {
            try {
                List<Student> students = dao.searchStudents(keyword);
                int total = dao.getStudentCount();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || version != queryVersion) return;
                    adapter.setStudentList(students);
                    ((TextView) findViewById(R.id.tvStudentCount)).setText(getString(R.string.student_count, total));
                    ((TextView) findViewById(R.id.tvStudentResults)).setText(
                            keyword.isEmpty() ? "" : getString(R.string.student_result_count, students.size()));
                    empty.setText(keyword.isEmpty() ? "Chưa có sinh viên." : "Không tìm thấy sinh viên phù hợp.");
                    empty.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (SQLiteException e) { message("Không thể tải danh sách sinh viên."); }
        });
    }
    private void message(String text) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        });
    }
    private void showDetail(Student s) {
        if (!requireAdmin()) return;
        new MaterialAlertDialogBuilder(this).setTitle(s.getFullname())
                .setMessage("Mã sinh viên: " + s.getStudentCode()
                        + "\nEmail: " + s.getEmail() + "\nSố điện thoại: " + s.getPhone()
                        + "\nTên đăng nhập: " + dao.getUsername(s.getId()))
                .setPositiveButton("Đóng", null).setNeutralButton("Sửa", (d, w) -> showForm(s)).show();
    }
    private void showForm(Student existing) {
        if (!requireAdmin()) return;
        View form = getLayoutInflater().inflate(R.layout.dialog_student_form, null);
        EditText code = form.findViewById(R.id.edtDialogStudentCode);
        EditText name = form.findViewById(R.id.edtDialogFullname);
        EditText email = form.findViewById(R.id.edtDialogEmail);
        EditText phone = form.findViewById(R.id.edtDialogPhone);
        EditText username = form.findViewById(R.id.edtDialogUsername);
        EditText password = form.findViewById(R.id.edtDialogPassword);
        if (existing != null) {
            code.setText(existing.getStudentCode()); name.setText(existing.getFullname());
            email.setText(existing.getEmail()); phone.setText(existing.getPhone());
            form.findViewById(R.id.studentFieldUsername).setVisibility(View.GONE);
            form.findViewById(R.id.studentFieldPassword).setVisibility(View.GONE);
            form.findViewById(R.id.studentAccountNote).setVisibility(View.GONE);
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? "Thêm sinh viên" : "Sửa sinh viên")
                .setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!requireAdmin()) return;
            if (!required(code) || !required(name)) return;
            String mail = email.getText().toString().trim();
            String number = phone.getText().toString().trim();
            if (!mail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                email.setError("Email không hợp lệ."); email.requestFocus(); return;
            }
            if (!number.isEmpty() && !number.matches("\\+?[0-9 ()-]{7,20}")) {
                phone.setError("Số điện thoại không hợp lệ."); phone.requestFocus(); return;
            }
            if (existing == null && (!required(username) || !required(password))) return;
            Student student = new Student(existing == null ? 0 : existing.getId(),
                    code.getText().toString().trim(), name.getText().toString().trim(),
                    mail, number, existing == null ? 0 : existing.getAccountId());
            String user = username.getText().toString().trim();
            String secret = password.getText().toString();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
            dialog.setCancelable(false);
            worker.execute(() -> {
                String error = null;
                try {
                    if (dao.isStudentCodeTaken(student.getStudentCode(), student.getId())) {
                        error = "Mã sinh viên đã tồn tại.";
                    } else if (existing == null && dao.isUsernameTaken(user)) {
                        error = "Tên đăng nhập đã tồn tại.";
                    } else if (existing == null) {
                        dao.insertStudentWithAccount(student, user, secret);
                    } else if (dao.updateStudent(student) == 0) {
                        error = "Sinh viên không còn tồn tại.";
                    }
                } catch (SQLiteConstraintException e) {
                    error = "Mã sinh viên hoặc tên đăng nhập đã tồn tại.";
                } catch (SQLiteException | IllegalArgumentException e) {
                    error = "Không thể lưu sinh viên. Vui lòng kiểm tra dữ liệu.";
                }
                final String result = error;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (result == null) {
                        dialog.dismiss(); reload(); message("Đã lưu sinh viên.");
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                        dialog.setCancelable(true);
                        message(result);
                    }
                });
            });
        }));
        dialog.show();
    }
    private boolean required(EditText field) {
        if (!field.getText().toString().trim().isEmpty()) return true;
        field.setError("Không được để trống."); field.requestFocus(); return false;
    }
    private void confirmDelete(Student student) {
        if (!requireAdmin()) return;
        new MaterialAlertDialogBuilder(this).setTitle("Xóa sinh viên?")
                .setMessage(student.getFullname() + " — " + student.getStudentCode()
                        + "\nTài khoản và lịch học liên quan cũng sẽ bị xóa.")
                .setNegativeButton("Hủy", null).setPositiveButton("Xóa", (d, w) -> {
                    if (!requireAdmin()) return;
                    worker.execute(() -> {
                        try {
                            int count = dao.deleteStudent(student);
                            message(count == 0 ? "Sinh viên không còn tồn tại." : "Đã xóa sinh viên.");
                            runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) reload(); });
                        } catch (SQLiteException e) { message("Không thể xóa sinh viên."); }
                    });
                }).show();
    }
    @Override protected void onDestroy() {
        worker.shutdown();
        super.onDestroy();
    }
}

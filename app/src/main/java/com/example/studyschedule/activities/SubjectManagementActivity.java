package com.example.studyschedule.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyschedule.R;
import com.example.studyschedule.adapters.SubjectAdapter;
import com.example.studyschedule.dal.SubjectDAO;
import com.example.studyschedule.models.Subject;
import com.example.studyschedule.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import java.util.Locale;

public class SubjectManagementActivity extends AppCompatActivity implements SubjectAdapter.Listener {
    private SubjectDAO dao;
    private SubjectAdapter adapter;
    private SessionManager session;
    private EditText search;
    private TextView count, empty;
    private AlertDialog activeDialog;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        session = createSessionManager();
        if (!checkAccess()) return;
        setContentView(R.layout.activity_subject_management);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        dao = new SubjectDAO(this);
        adapter = new SubjectAdapter();
        adapter.setListener(this);
        RecyclerView list = findViewById(R.id.rvSubjects);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        search = findViewById(R.id.edtSearchSubject);
        count = findViewById(R.id.tvSubjectCount);
        empty = findViewById(R.id.tvSubjectEmpty);
        findViewById(R.id.btnAddSubject).setOnClickListener(v -> onEdit(null));
        findViewById(R.id.btnSubjectBack).setOnClickListener(v -> finish());
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            public void afterTextChanged(Editable s) { }
        });
    }

    // Cho phép màn hình test debug dùng phiên riêng, không sửa phiên đăng nhập chung.
    protected SessionManager createSessionManager() {
        return new SessionManager(this);
    }

    @Override protected void onResume() {
        super.onResume();
        if (checkAccess() && dao != null) refresh();
    }

    private boolean checkAccess() {
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return false;
        }
        if (!session.isAdmin()) {
            message(R.string.subject_access_denied);
            finish();
            return false;
        }
        return true;
    }

    private void refresh() {
        try {
            String keyword = search.getText().toString().trim();
            List<Subject> all = dao.getAllSubjects();
            List<Subject> result = keyword.isEmpty() ? all : dao.searchSubjects(keyword);
            adapter.setSubjectList(result);
            count.setText(keyword.isEmpty() ? getString(R.string.subject_total, all.size())
                    : getString(R.string.subject_count, result.size(), all.size()));
            empty.setText(keyword.isEmpty() ? R.string.subject_empty : R.string.subject_no_results);
            empty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (SQLiteException e) { reportDatabaseError(e); }
    }

    @Override public void onView(Subject subject) {
        if (!checkAccess()) return;
        String description = subject.getDescription();
        if (description == null || description.trim().isEmpty()) description = getString(R.string.subject_no_description);
        activeDialog = new AlertDialog.Builder(this).setTitle(R.string.subject_detail)
                .setMessage(getString(R.string.subject_full_details, subject.getId(), subject.getSubjectCode(),
                        subject.getSubjectName(), description, subject.getCredits()))
                .setPositiveButton(R.string.subject_close, null).show();
    }

    @Override public void onEdit(Subject old) {
        if (!checkAccess()) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.SubjectFormDialogTheme);
        View form = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_subject_form, null);
        ((TextView) form.findViewById(R.id.tvSubjectFormTitle)).setText(
                old == null ? R.string.subject_form_add : R.string.subject_edit);
        ((TextView) form.findViewById(R.id.tvSubjectFormSubtitle)).setText(
                old == null ? R.string.subject_form_add_subtitle : R.string.subject_form_edit_subtitle);
        TextInputLayout codeField = form.findViewById(R.id.tilSubjectCode);
        TextInputLayout nameField = form.findViewById(R.id.tilSubjectName);
        TextInputLayout creditsField = form.findViewById(R.id.tilSubjectCredits);
        EditText code = form.findViewById(R.id.edtDialogSubjectCode);
        EditText name = form.findViewById(R.id.edtDialogSubjectName);
        EditText description = form.findViewById(R.id.edtDialogSubjectDesc);
        EditText credits = form.findViewById(R.id.edtDialogCredits);
        clearFieldErrorOnChange(code, codeField);
        clearFieldErrorOnChange(name, nameField);
        clearFieldErrorOnChange(credits, creditsField);
        if (old != null) {
            code.setText(old.getSubjectCode());
            name.setText(old.getSubjectName());
            description.setText(old.getDescription());
            credits.setText(String.valueOf(old.getCredits()));
        }
        AlertDialog dialog = builder
                .setView(form).setNegativeButton(R.string.subject_cancel, null)
                .setPositiveButton(old == null ? R.string.subject_form_save : R.string.subject_form_update, null).create();
        activeDialog = dialog;
        // Gắn listener sau show để lỗi nhập liệu không tự đóng form.
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!checkAccess()) return;
            codeField.setError(null); nameField.setError(null); creditsField.setError(null);
            String codeValue = code.getText().toString().trim().toUpperCase(Locale.ROOT);
            String nameValue = name.getText().toString().trim();
            EditText firstInvalid = null;
            if (codeValue.isEmpty()) {
                codeField.setError(getString(R.string.subject_required));
                firstInvalid = code;
            }
            if (nameValue.isEmpty()) {
                nameField.setError(getString(R.string.subject_required));
                if (firstInvalid == null) firstInvalid = name;
            }
            int creditValue = 0;
            try { creditValue = Integer.parseInt(credits.getText().toString().trim()); }
            catch (NumberFormatException ignoredNumber) { /* Báo lỗi tại trường tín chỉ bên dưới. */ }
            if (creditValue <= 0) {
                creditsField.setError(getString(R.string.subject_invalid_credits));
                if (firstInvalid == null) firstInvalid = credits;
            }
            if (firstInvalid != null) { firstInvalid.requestFocus(); return; }
            // Tạo bản mới: chỉ thay danh sách sau khi SQLite lưu thành công.
            Subject updated = new Subject(old == null ? 0 : old.getId(), codeValue, nameValue,
                    description.getText().toString().trim(), creditValue);
            try {
                boolean saved = old == null ? dao.insertSubject(updated) > 0 : dao.updateSubject(updated) > 0;
                if (!saved) { message(R.string.subject_not_found); return; }
                dialog.dismiss();
                message(R.string.subject_saved);
                refresh();
            } catch (SubjectDAO.DuplicateCodeException e) {
                codeField.setError(getString(R.string.subject_duplicate));
                code.requestFocus();
            } catch (SQLiteException e) { reportDatabaseError(e); }
        }));
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void clearFieldErrorOnChange(EditText input, TextInputLayout field) {
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { field.setError(null); }
            public void afterTextChanged(Editable s) { }
        });
    }

    @Override public void onDelete(Subject subject) {
        if (!checkAccess()) return;
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.subject_delete)
                .setMessage(getString(R.string.subject_confirm_delete, subject.getSubjectCode(), subject.getSubjectName()))
                .setNegativeButton(R.string.subject_cancel, null)
                .setPositiveButton(R.string.subject_delete, null).create();
        activeDialog = dialog;
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!checkAccess()) return;
            try {
                int result = dao.deleteSubject(subject.getId());
                if (result == SubjectDAO.DELETE_IN_USE) { message(R.string.subject_in_use); return; }
                if (result == 0) { message(R.string.subject_not_found); return; }
                dialog.dismiss();
                message(R.string.subject_deleted);
                refresh();
            } catch (SQLiteException e) { reportDatabaseError(e); }
        }));
        dialog.show();
    }

    private void message(int resource) { Toast.makeText(this, resource, Toast.LENGTH_LONG).show(); }
    private void reportDatabaseError(SQLiteException e) {
        Log.e("SubjectManagement", "SQLite operation failed", e);
        message(R.string.subject_database_error);
    }
    @Override protected void onDestroy() {
        if (activeDialog != null) activeDialog.dismiss();
        super.onDestroy();
    }
}

package com.example.studyschedule.dal;

import android.content.Context;
import com.example.studyschedule.models.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO phụ trách bảng Subject.
 * Phụ trách: Người 3 (nhánh feature/subject-management).
 */
public class SubjectDAO {

    private final DBHelper dbHelper;

    public SubjectDAO(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public List<Subject> getAllSubjects() {
        // TODO (Người 3): Lấy toàn bộ danh sách môn học (cung cấp cho Người 4 dùng khi đặt lịch)
        return new ArrayList<>();
    }

    public List<Subject> searchSubjects(String keyword) {
        // TODO (Người 3): Tìm kiếm môn học theo mã môn hoặc tên môn
        return new ArrayList<>();
    }

    public long insertSubject(Subject subject) {
        // TODO (Người 3): Thêm môn học mới
        return -1;
    }

    public int updateSubject(Subject subject) {
        // TODO (Người 3): Sửa thông tin môn học
        return 0;
    }

    public int deleteSubject(int subjectId) {
        // TODO (Người 3): Xóa môn học
        return 0;
    }
}

package com.example.studyschedule.utils;

/**
 * Hằng số dùng chung cho toàn bộ 5 thành viên.
 * TUYỆT ĐỐI KHÔNG tự ý đổi giá trị chuỗi để tránh lệch dữ liệu giữa các nhánh.
 */
public final class Constants {

    private Constants() {}

    // Roles cho bảng Account
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_STUDENT = "STUDENT";

    // Status cho bảng StudySchedule
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CANCELLED = "Cancelled";

    // Định dạng chuẩn lưu vào SQLite (để so sánh chuỗi trực tiếp được trong SQL)
    // Ngày: yyyy-MM-dd (Ví dụ: "2026-11-15")
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    // Giờ: HH:mm định dạng 24h (Ví dụ: "08:30", "14:00") -> Giúp câu lệnh SQL so sánh trùng lịch chính xác!
    public static final String TIME_FORMAT = "HH:mm";

    // Intent Extra Keys dùng khi chuyển màn hình
    public static final String EXTRA_SCHEDULE_ID = "extra_schedule_id";
    public static final String EXTRA_SELECTED_DATE = "extra_selected_date";
    public static final String EXTRA_STUDENT_ID = "extra_student_id";
    public static final String EXTRA_SUBJECT_ID = "extra_subject_id";
}

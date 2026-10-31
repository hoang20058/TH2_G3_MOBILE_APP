# TH2_G3_MOBILE_APP - STUDENT STUDY SCHEDULE MANAGEMENT AND BOOKING APPLICATION

- **GitHub Repository của nhóm**: [https://github.com/hoang20058/TH2_G3_MOBILE_APP](https://github.com/hoang20058/TH2_G3_MOBILE_APP)
- Khung dự án chuẩn (Base Skeleton) bằng **Java + XML Views + SQLiteOpenHelper + SharedPreferences** được thiết kế cho **5 thành viên** làm việc song song trên GitHub mà **KHÔNG BỊ XUNG ĐỘT CODE (Zero Merge Conflict)**.

---

## 1. Tài khoản mẫu có sẵn (Seed Data trong `DatabaseHelper.java`)
Ngay khi chạy app lần đầu, Database tự động tạo 4 bảng và nạp sẵn dữ liệu mẫu để cả 5 anh em có thể test màn hình của mình độc lập mà không cần chờ nhau:
- **Tài khoản Admin**: `username`: `admin` | `password`: `123` (`role`: `ADMIN`)
- **Tài khoản Student 1**: `username`: `sv01` | `password`: `123` (`role`: `STUDENT`, `studentId = 1`, đã có sẵn 2 lịch học mẫu ngày `2026-11-01`)
- **Tài khoản Student 2**: `username`: `sv02` | `password`: `123` (`role`: `STUDENT`, `studentId = 2`)
- **Môn học mẫu (Subject)**: `PRM392`, `SWD392`, `PRN211`

---

## 2. Phân công công việc cho 5 thành viên (Độc lập file 100%)

> **NGUYÊN TẮC VÀNG ĐỂ KHÔNG BỊ CONFLICT KHI GỘP CODE:**
> - **KHÔNG AI** sửa file `AndroidManifest.xml`, `DatabaseHelper.java`, `Constants.java`, `SessionManager.java` (đã được cấu hình và khai báo sẵn 100%).
> - **Mỗi người chỉ sửa đúng các file Java và XML thuộc phạm vi của mình** bên dưới.

| Thành viên | Nhánh Git (`branch`) | Chức năng phụ trách | Các file được phép chỉnh sửa |
| :--- | :--- | :--- | :--- |
| **Thành viên 1 (Lead / Auth)** | `feature/auth-dashboard` | - Login 2 role (`ADMIN`, `STUDENT`)<br>- Lưu phiên đăng nhập bằng `SharedPreferences`<br>- Màn hình `AdminDashboard` & `StudentDashboard`<br>- Quản lý repo GitHub & duyệt Pull Request | - `dao/AccountDao.java`<br>- `ui/auth/LoginActivity.java`<br>- `ui/dashboard/AdminDashboardActivity.java`<br>- `ui/dashboard/StudentDashboardActivity.java`<br>- `res/layout/activity_login.xml`<br>- `res/layout/activity_admin_dashboard.xml`<br>- `res/layout/activity_student_dashboard.xml` |
| **Thành viên 2 (Admin - Student)** | `feature/student-management` | - **Student Management**: Xem danh sách, Tìm kiếm, Thêm mới (tự động tạo luôn `Account` cho sinh viên), Sửa, Xóa sinh viên | - `dao/StudentDao.java`<br>- `ui/student/StudentManagementActivity.java`<br>- `ui/student/StudentAdapter.java`<br>- `res/layout/activity_student_management.xml`<br>- `res/layout/item_student.xml`<br>- `res/layout/dialog_student_form.xml` |
| **Thành viên 3 (Admin - Subject)** | `feature/subject-management` | - **Subject Management**: Xem danh sách, Tìm kiếm, Thêm mới, Sửa, Xóa môn học (`Subject`) | - `dao/SubjectDao.java`<br>- `ui/subject/SubjectManagementActivity.java`<br>- `ui/subject/SubjectAdapter.java`<br>- `res/layout/activity_subject_management.xml`<br>- `res/layout/item_subject.xml`<br>- `res/layout/dialog_subject_form.xml` |
| **Thành viên 4 (Student - Schedule & Booking)** | `feature/schedule-booking` | - **Study Schedule Management**: Xem, Tìm kiếm, Sửa, Xóa lịch học cá nhân<br>- **Study Schedule Booking**: Chọn môn, ngày, giờ bắt đầu/kết thúc, phòng học<br>- **Schedule Conflict Validation**: Chặn đặt trùng khung giờ trong cùng ngày<br>- **Schedule Status Management**: Đổi trạng thái `Pending`, `Confirmed`, `Cancelled` | - `dao/StudyScheduleDao.java`<br>- `ui/schedule/ScheduleListActivity.java`<br>- `ui/schedule/ScheduleBookingActivity.java`<br>- `ui/schedule/ScheduleAdapter.java`<br>- `res/layout/activity_schedule_list.xml`<br>- `res/layout/activity_schedule_booking.xml`<br>- `res/layout/item_schedule.xml` |
| **Thành viên 5 (Calendar & Notifications)** | `feature/calendar-notification` | - **Calendar View**: Xem lịch học theo Ngày (`Daily`), Tuần (`Weekly`), Tháng (`Monthly`)<br>- **Notifications**: Thông báo khi đặt/thay đổi lịch & hẹn giờ nhắc buổi học sắp tới bằng `AlarmManager`<br>- Hỗ trợ Thành viên 1 test tích hợp toàn hệ thống | - `ui/calendar/CalendarScheduleActivity.java`<br>- `utils/NotificationHelper.java`<br>- `receiver/ScheduleAlarmReceiver.java`<br>- `res/layout/activity_calendar_schedule.xml` |

---

## 3. Quy ước chung bắt buộc (Common Conventions)

1. **Quy ước Role (`Constants.java`)**:
   - Admin: `Constants.ROLE_ADMIN` (`"ADMIN"`)
   - Student: `Constants.ROLE_STUDENT` (`"STUDENT"`)
2. **Quy ước Trạng thái lịch học (`Constants.java`)**:
   - `Constants.STATUS_PENDING` (`"Pending"`)
   - `Constants.STATUS_CONFIRMED` (`"Confirmed"`)
   - `Constants.STATUS_CANCELLED` (`"Cancelled"`)
3. **Quy ước Định dạng Ngày & Giờ lưu trong SQLite**:
   - `studyDate`: Luôn lưu chuẩn **`yyyy-MM-dd`** (ví dụ: `"2026-11-05"`, tháng và ngày luôn có 2 chữ số `05`).
   - `startTime` và `endTime`: Luôn lưu chuẩn 24h **`HH:mm`** (ví dụ: `"08:30"`, `"14:00"`). Nhờ định dạng 24h `HH:mm`, câu lệnh SQL kiểm tra trùng lịch (`startTime < newEndTime AND endTime > newStartTime`) hoạt động chính xác tuyệt đối!
4. **Cách lấy ID sinh viên đang đăng nhập**:
   ```java
   SessionManager session = new SessionManager(this);
   int currentStudentId = session.getStudentId();
   ```

---

## 4. Quy trình Git / GitHub cho cả nhóm (Tối ưu thời gian lấy điểm tối đa)

### Bước 1: Clone khung dự án từ GitHub chung của nhóm về máy
```bash
git clone https://github.com/hoang20058/TH2_G3_MOBILE_APP.git
cd TH2_G3_MOBILE_APP
```

### Bước 2: Mỗi thành viên chuyển sang nhánh riêng của mình (Đã được tạo sẵn trên GitHub!)
```bash
# Ví dụ Thành viên 1:
git checkout feature/auth-dashboard

# Ví dụ Thành viên 2:
git checkout feature/student-management

# Ví dụ Thành viên 3:
git checkout feature/subject-management

# Ví dụ Thành viên 4:
git checkout feature/schedule-booking

# Ví dụ Thành viên 5:
git checkout feature/calendar-notification
```

### Bước 3: Code xong tính năng -> Push nhánh lên GitHub -> Tạo Pull Request
```bash
git add .
git commit -m "feat: complete assigned module"
git push origin <ten-nhanh-cua-ban>
```
Sau đó lên [https://github.com/hoang20058/TH2_G3_MOBILE_APP/pulls](https://github.com/hoang20058/TH2_G3_MOBILE_APP/pulls) bấm **New Pull Request** để gộp vào nhánh `main` (hoặc `features`). Vì mỗi thành viên làm trên các file hoàn toàn tách biệt nên GitHub sẽ báo **"Able to merge"** xanh mượt 100%!

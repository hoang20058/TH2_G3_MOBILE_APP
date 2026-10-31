# TH2_G3_MOBILE_APP - STUDENT STUDY SCHEDULE MANAGEMENT AND BOOKING APPLICATION

- **GitHub Repository của nhóm**: [https://github.com/hoang20058/TH2_G3_MOBILE_APP](https://github.com/hoang20058/TH2_G3_MOBILE_APP)
- **Cấu trúc Base Project (SQLite)** chuẩn 100% theo file quy định **Format the tests (Test 2)**:

```text
com.example.studyschedule
├── activities
│   ├── LoginActivity.java
│   ├── StudentManagementActivity.java
│   ├── SubjectManagementActivity.java
│   ├── ScheduleListActivity.java
│   ├── ScheduleBookingActivity.java
│   └── CalendarScheduleActivity.java
├── adapters
│   ├── StudentAdapter.java
│   ├── SubjectAdapter.java
│   └── ScheduleAdapter.java
├── dal
│   ├── DBHelper.java
│   ├── AccountDAO.java
│   ├── StudentDAO.java
│   ├── SubjectDAO.java
│   └── StudyScheduleDAO.java
├── models
│   ├── Account.java
│   ├── Student.java
│   ├── Subject.java
│   └── StudySchedule.java
├── utils
│   ├── Constants.java
│   ├── SessionManager.java
│   ├── NotificationHelper.java
│   └── ScheduleAlarmReceiver.java
└── MainActivity.java
```

---

## 1. Tài khoản mẫu có sẵn (Seed Data trong `dal/DBHelper.java`)
- **Admin**: `username`: `admin` | `password`: `123` (`role`: `ADMIN`)
- **Student 1**: `username`: `sv01` | `password`: `123` (`role`: `STUDENT`, `studentId = 1`)
- **Student 2**: `username`: `sv02` | `password`: `123` (`role`: `STUDENT`, `studentId = 2`)
- **Môn học mẫu (`Subject`)**: `PRM392`, `SWD392`, `PRN211`

---

## 2. Phân chia công việc và Nhánh Git cho 5 thành viên

| Người | Phụ trách | Công việc cụ thể | Nhánh Git (`branch`) | File làm việc riêng |
| :--- | :--- | :--- | :--- | :--- |
| **1 — Trưởng nhóm, nền tảng và đăng nhập** | Khung ứng dụng, tài khoản | Khởi tạo project; cấu hình SQLite (`DBHelper`); khai báo 4 bảng; đăng nhập, phân quyền Admin/Student; lưu đăng nhập bằng `SharedPreferences`; điều hướng tại `MainActivity` | `feature/auth-core` | - `MainActivity.java`<br>- `activities/LoginActivity.java`<br>- `dal/AccountDAO.java`<br>- `res/layout/activity_login.xml`<br>- `res/layout/activity_main.xml` |
| **2 — Quản lý sinh viên** | Chức năng của Admin | Giao diện danh sách, thêm, sửa, xóa, tìm kiếm, xem chi tiết sinh viên; xử lý tạo tài khoản Student khi thêm sinh viên | `feature/student-management` | - `activities/StudentManagementActivity.java`<br>- `adapters/StudentAdapter.java`<br>- `dal/StudentDAO.java`<br>- `res/layout/activity_student_management.xml`<br>- `res/layout/item_student.xml`<br>- `res/layout/dialog_student_form.xml` |
| **3 — Quản lý môn học** | Chức năng của Admin | Giao diện danh sách, thêm, sửa, xóa, tìm kiếm, xem chi tiết môn học; cung cấp danh sách môn học để người 4 dùng khi đặt lịch | `feature/subject-management` | - `activities/SubjectManagementActivity.java`<br>- `adapters/SubjectAdapter.java`<br>- `dal/SubjectDAO.java`<br>- `res/layout/activity_subject_management.xml`<br>- `res/layout/item_subject.xml`<br>- `res/layout/dialog_subject_form.xml` |
| **4 — Quản lý và đặt lịch học** | Luồng chính của Student | Tạo/đặt lịch; sửa, xóa, tìm kiếm, xem lịch cá nhân; chọn môn, ngày, giờ, địa điểm; kiểm tra trùng lịch; quản lý `Pending`/`Confirmed`/`Cancelled` | `feature/study-schedule` | - `activities/ScheduleListActivity.java`<br>- `activities/ScheduleBookingActivity.java`<br>- `adapters/ScheduleAdapter.java`<br>- `dal/StudyScheduleDAO.java`<br>- `res/layout/activity_schedule_list.xml`<br>- `res/layout/activity_schedule_booking.xml`<br>- `res/layout/item_schedule.xml` |
| **5 — Lịch và thông báo** | Hiển thị theo thời gian | Hiển thị lịch ngày/tuần/tháng; chọn lịch để xem chi tiết; nhắc lịch sắp đến; cập nhật hoặc hủy nhắc khi lịch thay đổi/hủy/xóa | `feature/calendar-notifications` | - `activities/CalendarScheduleActivity.java`<br>- `utils/NotificationHelper.java`<br>- `utils/ScheduleAlarmReceiver.java`<br>- `res/layout/activity_calendar_schedule.xml` |

---

## 3. Quy trình Git cho 5 anh em

```bash
# 1. Clone project về máy:
git clone https://github.com/hoang20058/TH2_G3_MOBILE_APP.git
cd TH2_G3_MOBILE_APP

# 2. Chuyển sang nhánh được phân công (đã tạo sẵn trên GitHub):
git checkout feature/auth-core
# hoặc:
git checkout feature/student-management
git checkout feature/subject-management
git checkout feature/study-schedule
git checkout feature/calendar-notifications

# 3. Code xong phần của mình thì commit và push lên đúng nhánh đó:
git add .
git commit -m "feat: hoan thanh module"
git push origin <ten-nhanh-cua-ban>
```

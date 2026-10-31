# Module 2 — Quản lý sinh viên

## Phạm vi

Chỉ triển khai StudentManagementActivity, StudentAdapter, StudentDAO và ba layout sinh viên; kèm kiểm thử StudentDAOTest. Không sửa đăng nhập, điều hướng, AccountDAO, DBHelper hoặc NotificationHelper.

## Chức năng

- Danh sách, tìm theo mã/tên/email/điện thoại, xem chi tiết và tên đăng nhập liên kết.
- Thêm hồ sơ kèm Account có role STUDENT trong một transaction; chặn mã hoặc username trùng.
- Sửa hồ sơ, giữ nguyên accountId. Email và điện thoại có thể trống; nếu nhập phải hợp lệ.
- Xóa có xác nhận; xóa lịch, hồ sơ và tài khoản liên quan trong transaction. Không xóa tài khoản Admin hoặc tài khoản còn được hồ sơ khác tham chiếu.
- Hiển thị danh sách trống, giữ từ khóa tìm kiếm sau cập nhật.
- Kiểm tra quyền Admin bằng SessionManager có sẵn. Truy vấn danh sách và thao tác ghi chạy trên executor riêng.

## Điểm cần phối hợp

- Module 1 cung cấp đăng nhập, lưu phiên Admin và mở StudentManagementActivity. Các file login đã được trả về bộ khung gốc nên hiện chưa thể chạy luồng đăng nhập đầu cuối.
- Mật khẩu tài khoản mới dùng cùng định dạng dạng rõ với dữ liệu seed hiện có. Khi module 1 thống nhất cơ chế băm/xác thực chung, cần cập nhật phần tạo tài khoản trong StudentDAO tương ứng; module 2 không tự thay đổi cơ chế login.
- Chưa tích hợp hủy lời nhắc khi xóa sinh viên vì module 5 chưa cung cấp API. Cần phối hợp lấy ID lịch trước khi xóa và gọi API hủy sau khi transaction thành công.
- Schema, version, ngày giờ và trạng thái database giữ nguyên.

## Kiểm thử

StudentDAOTest có bảy kiểm thử: tạo hồ sơ kèm tài khoản đúng role; rollback tài khoản khi trùng mã; trùng username; cập nhật giữ liên kết tài khoản; tìm ký tự đặc biệt; lưu và tìm tên tiếng Việt có dấu; xóa đúng tài khoản và lịch. Tests dùng SQLite trong bộ nhớ và schema từ DBHelper gốc, không sửa database của ứng dụng. Không kiểm thử login thuộc module 1.

Đã chạy :app:assembleDebug, :app:assembleDebugAndroidTest và :app:connectedDebugAndroidTest trên emulator Pixel_7 (API 17); cả 8 kiểm thử đều đạt (7 DAO, 1 giao diện). Kiểm thử mới xác nhận tên tiếng Việt có dấu nhập được trong form, lưu và đọc lại nguyên vẹn, đồng thời tìm kiếm theo chuỗi có dấu được.

Sau khi module 1 được ghép, kiểm tra thủ công danh sách, thêm hợp lệ/không hợp lệ, sửa giữ nguyên mã, tìm kiếm, chi tiết, hủy/xác nhận xóa và quyền truy cập Student. Kiểm tra lời nhắc sau khi ghép API module 5.

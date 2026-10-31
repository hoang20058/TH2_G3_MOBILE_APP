# Module 3 — Quản lý môn học

## Chạy riêng module 3 ngay, không cần đăng nhập

Đã có cấu hình **Module 3 - Test**, chỉ dành cho build `debug`.

1. **Sync Project with Gradle Files**.
2. Mở **Build Variants**, chọn `debug` cho module `app`.
3. Ở danh sách cấu hình cạnh nút Run, chọn **Module 3 - Test**.
4. Chọn điện thoại/máy ảo rồi bấm **Run ▶**. Màn hình môn học mở trực tiếp, không nhập tài khoản.

Nếu chưa thấy cấu hình: **Run → Edit Configurations → + → Android App**; đặt tên `Module 3 - Test`, Module `app`, Launch **Specified Activity**, Activity `com.example.studyschedule.activities.SubjectDebugActivity`. Apply rồi Run. Chạy trực tiếp `SubjectManagementActivity` vẫn kiểm tra phiên đăng nhập bình thường; để chạy độc lập phải chọn `SubjectDebugActivity`.

Cổng test đọc tài khoản Admin đầu tiên đang có trong SQLite, lưu phiên vào preferences riêng có hậu tố `_subject_debug`, rồi dùng lại toàn bộ màn hình/nghiệp vụ module 3. Không đổi tài khoản, không ghi đè phiên chung của Login/Main. Thông báo “Test module 3” xuất hiện khi mở. Dữ liệu môn học là SQLite thật trên thiết bị: thêm/sửa/xóa được lưu và còn sau khi chạy lại.

`SubjectDebugActivity`, manifest và chuỗi test nằm trong `app/src/debug`, tự bị loại khỏi bản release. Màn hình chính chỉ thêm hàm `createSessionManager()` để bản debug truyền phiên riêng; kiểm tra quyền chính thức vẫn hoạt động. Khi tích hợp nhóm, chọn lại cấu hình `app` để chạy luồng đăng nhập. Nếu muốn gỡ setup tạm: xóa `app/src/debug` và `.run/Module_3_Test.run.xml`, cùng test `SubjectDebugActivityTest`; không cần sửa Login/Main.

## Mở bằng Android Studio

1. File → Open → chọn thư mục gốc `TH2_G3_MOBILE_APP` (có `settings.gradle`), không chọn riêng thư mục `app`.
2. Settings → Build, Execution, Deployment → Build Tools → Gradle → chọn **Gradle JDK 17**. Trên máy hiện tại có `C:\Users\Asus\.jdks\jbr-17.0.14`. Java mặc định của terminal là 25 nên cần chọn đúng JDK.
3. Dùng phiên bản khai báo trong `gradle-wrapper.properties` và `build.gradle` của nhánh đang mở; compileSdk/targetSdk là 34. Các kết quả kiểm chứng bên dưới ghi rõ cấu hình máy đã chạy. Nếu thiếu, cài Android SDK Platform 34 và Build Tools 34.0.0 trong SDK Manager; kết nối mạng để tải dependencies.
4. Sync Project with Gradle Files. Wrapper đã được bổ sung (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`). `local.properties` là cấu hình riêng máy, không commit.
5. Mở `app/src/main/res/layout/activity_subject_management.xml`, chọn **Split / Design**. `tools:listitem` và dữ liệu `tools:text` cho phép xem mẫu thẻ ngay trong Layout Editor; không đưa dữ liệu giả vào runtime. Form ở `dialog_subject_form.xml`, thẻ ở `item_subject.xml`.

## Chạy màn hình và phụ thuộc module 1

LoginActivity và MainActivity hiện vẫn là TODO. Cấu hình `app` đi vào luồng đăng nhập khung; hãy dùng `Module 3 - Test` ở trên để kiểm thử độc lập. Luồng chính thức tiếp tục dùng phiên của module 1.

Sau khi module 1 hoạt động: đăng nhập `admin / 123`, lưu phiên thật bằng SessionManager, rồi nút quản lý môn học tại MainActivity gọi:

```java
startActivity(new Intent(this, SubjectManagementActivity.class));
```

Tài khoản `sv01 / 123`, `sv02 / 123` là Student và phải bị chặn. Màn hình kiểm tra đăng nhập và vai trò ở onCreate/onResume và trước các hành động. Manifest đã khai báo Activity, không cần đổi exported.

Giao diện đã được cập nhật theo khung 03 trong ảnh Study Planner: header ngang có nhãn quản trị viên, ô tìm kiếm viền mảnh, banner hồng chứa nút thêm, thẻ môn có icon pastel và nút sửa/xóa. Chỉ sửa module 3; dùng nút quay lại thật thay menu chưa được tích hợp, không thêm tab Sinh viên/Tài khoản thuộc điều hướng chung. Mô tả đầy đủ vẫn xem được khi nhấn vào thẻ.

## API cho module 4

- `List<Subject> getAllSubjects()`: đầy đủ ID/mã/tên/mô tả/tín chỉ; sắp theo mã NOCASE, sau đó ID.
- `List<Subject> searchSubjects(String keyword)`: tìm chuỗi con theo mã/tên; null/trống trả tất cả; `%`, `_` được coi là ký tự tìm kiếm thật.
- `long insertSubject(Subject subject)`: trả ID mới; chuẩn hóa mã uppercase Locale.ROOT, trim trường, kiểm tra mã/tên và tín chỉ dương.
- `int updateSubject(Subject subject)`: giữ ID; trả 1 khi sửa, 0 nếu không tồn tại.
- `int deleteSubject(int subjectId)`: trả 1 khi xóa, 0 nếu không tồn tại, `SubjectDAO.DELETE_IN_USE` (-2) nếu bất kỳ lịch nào tham chiếu. Kiểm tra và xóa trong cùng transaction.
- `boolean isCodeExists(String code, int excludedId)`: bỏ qua ID đang sửa; dùng -1 khi thêm.
- Ghi mã trùng ném `SubjectDAO.DuplicateCodeException`; dữ liệu không hợp lệ ném `IllegalArgumentException`; lỗi DB ném `SQLiteException`. Bên gọi cần xử lý, không xem exception là thành công.
- Không đóng DBHelper/database singleton. Không sửa version, schema, foreign key hoặc lịch của module khác.

Luồng: XML định nghĩa giao diện → Activity kiểm tra quyền, đọc form và điều phối → DAO kiểm tra dữ liệu/truy vấn → SQLite chung. Adapter chỉ hiển thị Subject và gọi callback xem/sửa/xóa với vị trí hiện tại; không chứa nghiệp vụ database.

## Kiểm thử

Dùng JDK 17:

```powershell
$env:JAVA_HOME='C:\Users\Asus\.jdks\jbr-17.0.14'
.\gradlew.bat assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

`SubjectDAOTest` chạy SQLite thật trên emulator/test device, rollback dữ liệu của từng test. Dùng bản cài test có dữ liệu seed để kiểm tra danh sách không trống. Bao phủ dữ liệu seed, tìm mã/tên/trống/wildcard, lưu đầy đủ mô tả, chuẩn hóa/trim, tín chỉ sai và int tối đa, sửa giữ mã/ID, trùng mã khi thêm/sửa, xóa môn chưa có lịch, chặn xóa và bảo toàn lịch Pending/Confirmed/Cancelled.

Checklist UI: có thể thử CRUD ngay qua `Module 3 - Test`; các kiểm tra phiên thật cần module 1 tích hợp:

- Đọc các môn seed hiện tại INT1306/INT1310/INT1313; số lượng khớp dữ liệu thật. Database cũ có thể vẫn chứa bộ PRM392/SWD392/PRN211 vì version không đổi.
- Thêm môn hợp lệ, đóng/mở lại ứng dụng để xác nhận lưu bền vững.
- Thử mã/tên trống; tín chỉ trống, 0, âm, không phải số và 2147483648: form giữ mở, báo lỗi.
- Thêm mã khác hoa/thường của môn hiện có: bị chặn.
- Sửa tên giữ mã: thành công; đổi sang mã môn khác: bị chặn, dữ liệu cũ giữ nguyên.
- Tìm mã/tên, không có kết quả, xóa từ khóa, kiểm tra lọc được giữ sau CRUD.
- Nhấn thẻ xem đủ ID/mã/tên/mô tả/tín chỉ; mô tả trống hiện “Chưa có mô tả”.
- Xóa môn không có lịch: thành công; có lịch kể cả Cancelled: bị chặn.
- Hủy form hoặc xác nhận xóa: không đổi dữ liệu.
- Kiểm tra chưa đăng nhập/Student và đổi phiên khi Activity ở background.
- Kiểm tra màn hình nhỏ, bàn phím mở, chữ lớn và cuộn form.

## Phạm vi thay đổi

Sáu file module: SubjectManagementActivity, SubjectAdapter, SubjectDAO và ba XML activity/item/dialog. Thêm subject_strings.xml, subject_colors.xml, subject_book.xml, SubjectDAOTest và tài liệu này. File dùng chung chỉ bổ sung Gradle wrapper và ignore cache `.gradle-user`; không đổi DBHelper, model, Main/Login, manifest, theme hay dependencies.

## Kết quả kiểm chứng tại lần bàn giao này

- Nhánh: `feature/subject-management`; không commit/push/merge.
- Đã dùng JDK 17.0.14 + Gradle 8.2 + AGP 8.2.2; đã cài SDK Platform 34 và Build Tools 34.0.0 theo cấu hình gốc.
- `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug`: BUILD SUCCESSFUL. Có APK app và APK kiểm thử trong `app/build/outputs/apk`.
- `testDebugUnitTest`: NO-SOURCE (project không có test JVM); không coi đây là đã chạy test SQLite.
- Lint: 0 lỗi, 92 cảnh báo toàn project. Module môn học có cảnh báo `NotifyDataSetChanged` (thay cả danh sách sau tìm kiếm) và `Overdraw` (nền riêng của màn hình). Không tắt lint hay che cảnh báo.
- XML parse và `git diff --check`: đạt. Chưa kiểm chứng trực quan trên emulator hoặc Android Studio Layout Editor.
- `adb devices`: không có thiết bị kết nối. Các test SQLite/UI chưa được thực thi; cần emulator/test device và module đăng nhập để hoàn tất checklist thủ công.
- Lần tải wrapper dùng cache mặc định bị khóa bởi tiến trình khác; build thành công bằng bản Gradle 8.2 chính thức tải vào `.gradle-user/tooling/gradle-8.2`, với cache riêng `.gradle-user`. Nếu Android Studio còn chờ tải wrapper, chờ tiến trình sync hiện có kết thúc; có thể chọn bản Gradle local này trong Settings → Gradle trên máy hiện tại.
- `connectedDebugAndroidTest` đã được gọi và dừng với `DeviceException: No connected devices!`; không có test runtime nào được xác nhận đạt.

## Kiểm chứng cổng test độc lập

- Theo cấu hình hiện tại của workspace (Gradle 8.13 / AGP 8.13.2), `assembleDebug`, `assembleDebugAndroidTest`, `assembleRelease`, `lintDebug` đều thành công.
- Manifest debug có `SubjectDebugActivity`; manifest release và lớp biên dịch release không có Activity này.
- `SubjectDebugActivityTest` đã chạy đạt trên Infinix X6833B / Android 14: mở trực tiếp được danh sách môn học và không thay đổi preferences đăng nhập chung.
- Các kết quả “không có thiết bị” ở mục kiểm chứng lần bàn giao đầu là kết quả cũ; cổng test mới đã được kiểm tra trên thiết bị thật như trên. Chưa chạy lại toàn bộ bộ test DAO trong lần setup này.

## Giao diện thêm/sửa môn học

Form dùng chung `dialog_subject_form.xml`, với icon sách hồng, tiêu đề theo chế độ, các ô nhập Material có nhãn nổi và bo góc. Mã, tên và tín chỉ đánh dấu bắt buộc; mô tả có thể trống. Nút Hủy viền nhẹ, nút Lưu môn học/Lưu thay đổi màu navy. Style `SubjectFormDialogTheme` chỉ áp dụng cho dialog của module 3.

Chạy `Module 3 - Test`, nhấn **+ Thêm môn học** để xem form mới; nhấn icon bút trên thẻ để sửa với dữ liệu điền sẵn. Nội dung cuộn được khi mở bàn phím. Lỗi xuất hiện dưới trường nhập, tự bỏ khi chỉnh lại; form chỉ đóng khi lưu thành công hoặc hủy. Hủy không thay đổi SQLite.

Phạm vi lần chỉnh form: `SubjectManagementActivity`, `dialog_subject_form.xml`, `subject_strings.xml`, thêm `subject_styles.xml` và `color/subject_field_stroke.xml`; bổ sung kiểm thử giao diện trong `SubjectDebugActivityTest`. Không sửa DAO, DBHelper, model, Login/Main, Gradle hoặc theme chung trong lần chỉnh này.
- Kiểm chứng form: build APK và lint thành công (không có lỗi lint). Cả 3 test `SubjectDebugActivityTest` đã đạt trên Infinix/Android 14, gồm báo lỗi inline với tín chỉ vượt int, dữ liệu sửa điền sẵn, hủy giữ nguyên dữ liệu và giữ phiên đăng nhập chung. Đã xem trực quan dialog trên điện thoại; style riêng giữ nền trắng trong chế độ tối.

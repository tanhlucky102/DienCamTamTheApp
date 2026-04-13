# Dàn ý Thuyết trình Dự án Thuyết minh Phần mềm "Diễn Cầm Tam Thế" (DienCamTamTheApp)

Dưới đây là dàn ý chi tiết dùng để chuẩn bị slide thuyết trình cho dự án. Bạn có thể sử dụng các tiêu đề này để làm các slide tương ứng.

## 1. Giới thiệu tổng quan (Overview)
- **Tên dự án:** Diễn Cầm Tam Thế (DienCamTamThe App).
- **Loại hình:** Ứng dụng Web.
- **Giới thiệu ngắn gọn:** Đây là một ứng dụng bói toán, tra cứu tử vi và phong thủy dựa trên nền tảng cổ học Phương Đông (Diễn Cầm Tam Thế Tử). Ứng dụng giúp người dùng xem các quẻ bói, luận giải vận mệnh, hôn nhân, gia đạo, sự nghiệp dựa trên thông tin năm, tháng, ngày, giờ sinh và giới tính.

## 2. Mục đích và Ý nghĩa của Ứng dụng (Purpose & Value)
- **Giải quyết vấn đề:** Chuyển đổi số cuốn sách cổ "Diễn Cầm Tam Thế", giúp người dùng không cần phải tra cứu thủ công phức tạp qua sách vở mà có thể nhận được kết quả một cách tự động, nhanh chóng và chính xác.
- **Giá trị mang lại:**
  - Bảo tồn và phát huy các giá trị văn hóa, tín ngưỡng truyền thống một cách hiện đại hóa.
  - Cung cấp trải nghiệm thân thiện, trực quan với các thẻ bài (flashcard) hiển thị quẻ bói cho người dùng.
  - Tư vấn, tham khảo để có thêm góc nhìn về các khía cạnh trong cuộc sống (vận khí, kiếp trước, kiếp này,...).

## 3. Công nghệ và Thư viện sử dụng (Tech Stack)
Dự án được phát triển theo mô hình ứng dụng Web hiện đại, kết hợp chặt chẽ giữa Backend và Frontend:
- **Backend (Server-side):**
  - **Java 21:** Ngôn ngữ lập trình chính.
  - **Spring Boot (v4.0.5):** Framework lõi giúp xây dựng ứng dụng nhanh chóng (bao gồm WebMVC).
  - **Spring Security:** Quản lý phân quyền, xác thực người dùng, bảo mật các API (Authentication & Authorization) và mã hóa mật khẩu (`spring-security-crypto`).
  - **Spring Data JPA:** ORM tương tác với database linh hoạt.
- **Database (Cơ sở dữ liệu):**
  - **MySQL:** Hệ quản trị cơ sở dữ liệu quan hệ, lưu trữ thông tin người dùng, nội dung các quẻ bói (Book Section, Book Entry), và lịch sử tra cứu của người dùng.
- **Frontend (Client-side & UI):**
  - **Thymeleaf:** Template Engine của Spring Boot dùng để render giao diện HTML động (`dashboard.html`, `auth.html`, `profile.html`, `history.html`).
  - **HTML/CSS/JavaScript thuần:** Xây dựng giao diện responsive, xử lý hiệu ứng lật thẻ (flashcards), thao tác animation mượt mà trên UI.
- **Tiện ích khác:**
  - Các thuật toán tính toán Lịch Âm - Dương, Can Chi, và các quy tắc quy đổi ngũ hành (`LunarCalendarUtil`).

## 4. Các tính năng chính của Ứng dụng (Key Features)
- **Quản lý tài khoản (Authentication & Profile):**
  - Đăng ký, Đăng nhập an toàn.
  - Cập nhật thông tin cá nhân (Profile update), Đổi mật khẩu, Quên mật khẩu.
- **Tra cứu và Luận giải Tử vi (Divination Engine):**
  - Nhập dữ liệu đầu vào: Năm, tháng, ngày, giờ sinh, giới tính.
  - Hệ thống tự động chuyển đổi sang Âm lịch, Can Chi và cung mệnh.
  - Dựa vào tham số ngày sinh để truy xuất hơn 30+ hạng mục luận giải từ CSDL (Ví dụ: Số phu thê, Giờ sinh đoán kiếp trước / kiếp này, Chữ Vượng/Tướng/Hưu/Tù/Tử...).
- **Giao diện thẻ bài đa dạng (Flashcards UI):**
  - Kết quả được trình bày dưới dạng thẻ bài tương tác trực quan trên trang Dashboard.
  - Thiết kế thích ứng ứng (Responsive), tự động scale kích thước quẻ bói thân thiện cho các màn hình (Mobile & Desktop).
- **Lưu trữ Lịch sử (History Tracking):**
  - Xem lại các lần bói trước đó của người dùng (trang History).

## 5. Kiến trúc hệ thống và Luồng dữ liệu (Architecture & Data Flow)
*(Nên chuẩn bị một số hình ảnh minh họa hoặc sơ đồ hoạt động cho slide này)*
- Người dùng -> Gửi yêu cầu thông qua Giao diện (Thymeleaf/JS) -> Backend Controller (Ví dụ: `DivinationController`) -> Tầng Dịch vụ (Service - Gọi `LunarCalendarUtil` tính lịch âm/can chi, gọi DB lấy quẻ) -> Trả về `DivinationResponse` -> Hiển thị kết quả dạng UI Cards trên Dashboard.

## 6. Hướng phát triển trong tương lai (Future Enhancements)
- Hỗ trợ thêm nhiều ngôn ngữ, thuật toán luận giải mở rộng từ các sách lý số khác.
- Cải thiện cơ chế đề xuất (Recommendation) nâng cao dựa vào AI/chatbot để tư vấn vận mệnh tương tác như một chuyên gia tử vi.
- Phát triển thêm app mobile native cho Android / iOS.

## 7. Demo Cấu trúc Ứng dụng (Q&A và Live Demo)
- Cho người dùng xem thao tác tạo tài khoản.
- Tiến hành nhập thông tin ngày tháng năm sinh, hiển thị kết quả và cách các thẻ flashcards hoạt động.

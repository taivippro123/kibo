# Hướng dẫn tích hợp API cho ứng dụng Kibo

## Tổng quan
Ứng dụng Android Kibo đã được tích hợp với API backend sử dụng Retrofit2. Tất cả các màn hình đăng nhập, đăng ký và xác thực OTP đã được kết nối với API.

## Cấu trúc API đã được thiết lập

### 1. Dependencies đã thêm
- Retrofit2: 2.9.0
- OkHttp: 4.12.0
- Gson: 2.10.1
- Logging Interceptor: 4.12.0

### 2. Permissions đã thêm
- INTERNET
- ACCESS_NETWORK_STATE

### 3. Model Classes
- `LoginRequest`: Dữ liệu đăng nhập
- `RegisterRequest`: Dữ liệu đăng ký
- `OtpRequest`: Dữ liệu xác thực OTP
- `ApiResponse<T>`: Response wrapper từ API
- `User`: Thông tin người dùng

### 4. API Service Interface
- `POST /auth/login`: Đăng nhập
- `POST /auth/register`: Đăng ký
- `POST /auth/verify-otp`: Xác thực OTP
- `POST /auth/resend-otp`: Gửi lại OTP

### 5. API Client
- `ApiClient`: Singleton class để khởi tạo Retrofit
- Base URL: `https://your-api-domain.com/api/` (cần thay đổi)

## Cách sử dụng

### 1. Cập nhật Base URL
Mở file `ApiClient.java` và thay đổi `BASE_URL` thành URL API thực tế của bạn:

```java
private static final String BASE_URL = "https://your-actual-api-domain.com/api/";
```

### 2. Cấu trúc API Response mong đợi

#### Login API Response:
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "phone": "0123456789",
    "token": "jwt_token_here",
    "refreshToken": "refresh_token_here"
  }
}
```

#### Register API Response:
```json
{
  "success": true,
  "message": "Đăng ký thành công, vui lòng xác thực OTP"
}
```

#### OTP Verification API Response:
```json
{
  "success": true,
  "message": "Xác thực thành công",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "phone": "0123456789",
    "token": "jwt_token_here",
    "refreshToken": "refresh_token_here"
  }
}
```

### 3. Xử lý lỗi
Ứng dụng đã được thiết lập để xử lý:
- Lỗi mạng (network errors)
- Lỗi HTTP (HTTP errors)
- Lỗi API (API response errors)
- Validation errors

### 4. Lưu trữ dữ liệu người dùng
Sau khi đăng nhập/xác thực OTP thành công, thông tin người dùng được lưu vào SharedPreferences:
- `user_token`: JWT token
- `refresh_token`: Refresh token
- `user_email`: Email người dùng
- `user_name`: Tên người dùng
- `user_id`: ID người dùng
- `is_logged_in`: Trạng thái đăng nhập

## Các tính năng đã được tích hợp

### 1. LoginActivity
- ✅ Validation form
- ✅ Gọi API đăng nhập
- ✅ Lưu thông tin người dùng
- ✅ Xử lý lỗi
- ✅ Chuyển hướng đến MainActivity

### 2. RegisterActivity
- ✅ Validation form
- ✅ Gọi API đăng ký
- ✅ Xử lý lỗi
- ✅ Chuyển hướng đến OtpVerificationActivity

### 3. OtpVerificationActivity
- ✅ Gọi API xác thực OTP
- ✅ Gọi API gửi lại OTP
- ✅ Lưu thông tin người dùng
- ✅ Xử lý lỗi
- ✅ Chuyển hướng đến MainActivity

## Bước tiếp theo

1. **Cập nhật Base URL**: Thay đổi URL trong `ApiClient.java`
2. **Test API**: Kiểm tra kết nối với backend
3. **Xử lý token**: Implement token refresh logic nếu cần
4. **Error handling**: Tùy chỉnh thông báo lỗi theo yêu cầu
5. **Logging**: Kiểm tra logs trong Logcat để debug

## Lưu ý quan trọng

- Đảm bảo backend API trả về đúng format JSON như mong đợi
- Kiểm tra CORS settings nếu cần
- Test trên thiết bị thật để đảm bảo kết nối mạng hoạt động
- Xem logs trong Logcat để debug các vấn đề kết nối

## Debug

Để debug, kiểm tra logs trong Android Studio Logcat với tag "OkHttp" để xem các request/response chi tiết.

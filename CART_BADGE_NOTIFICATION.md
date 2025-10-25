# Cart Badge Notification - Hướng dẫn

## Tổng quan

Tính năng hiển thị số lượng sản phẩm trong giỏ hàng trên badge của app icon khi app đóng, sử dụng:

- **NotificationCompat** để tạo notification với badge count
- **WorkManager** để chạy background task định kỳ (mỗi 15 phút)
- **NotificationChannel** với `setShowBadge(true)` để hỗ trợ badge trên launcher

## Các file đã thêm/sửa

### 1. Thêm mới

- `app/src/main/java/com/example/kibo/notifications/NotificationHelper.java`
  - Tạo NotificationChannel với `setShowBadge(true)`
  - Post notification với `setNumber(count)` để cập nhật badge
  - Clear notification khi cần
- `app/src/main/java/com/example/kibo/workers/CartBadgeWorker.java`
  - Worker chạy background để query cart items từ API
  - Gọi `ApiService.getCartItems(cartId)` synchronously
  - Cập nhật notification với số lượng item

### 2. Cập nhật

- `app/build.gradle`
  - Thêm dependency: `androidx.work:work-runtime:2.8.1`
- `app/src/main/AndroidManifest.xml`
  - Thêm permission: `android.permission.POST_NOTIFICATIONS` (Android 13+)
- `app/src/main/java/com/example/kibo/MainActivity.java`
  - Schedule PeriodicWorkRequest khi app khởi động
  - Clear notification khi user mở app

## Cách hoạt động

### Khi app đang chạy (foreground)

- `MainActivity.refreshCartBadge()` cập nhật badge trên BottomNavigationView
- Notification bị clear để tránh trùng lặp

### Khi app đóng (background)

- WorkManager chạy `CartBadgeWorker` mỗi 15 phút (giới hạn hệ thống)
- Worker kiểm tra `SessionManager`:
  - Nếu user chưa login hoặc không có cart → notification count = 0
  - Nếu user có cart → gọi API `getCartItems(cartId)`
  - Lấy số lượng item và post notification
- Notification với `setNumber(count)` sẽ hiển thị badge trên app icon (tùy launcher)

### API endpoint sử dụng

```java
ApiService.getCartItems(int cartId)
// Response: CartItemsResponse với List<CartItem>
// Badge count = response.body().getData().size()
```

### Authentication

- Sử dụng `ApiClient.getApiServiceWithAuth(context)`
- Token lấy từ `SessionManager.getAccessToken()`
- Auth header tự động thêm bởi `ApiClient`

## Hướng dẫn build & test

### 1. Build project

```powershell
cd d:\PRM392\kibo
.\gradlew.bat assembleDebug
```

### 2. Cài đặt APK

```powershell
# Kết nối thiết bị/emulator qua ADB
adb devices

# Cài APK
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 3. Test flow

#### Bước 1: Mở app và đăng nhập

- Launch app
- Đăng nhập với tài khoản có sẵn cart
- Kiểm tra badge trên BottomNavigationView (cart tab)

#### Bước 2: Thêm sản phẩm vào cart

- Browse sản phẩm
- Thêm vài sản phẩm vào giỏ hàng
- Xác nhận badge cập nhật ngay lập tức

#### Bước 3: Test background notification

- Đóng app hoàn toàn (swipe away từ recent apps)
- Đợi ~15 phút hoặc trigger worker thủ công (xem phần Debug bên dưới)
- Kiểm tra notification tray → thấy notification "Giỏ hàng của bạn"
- Kiểm tra app icon → thấy badge count (tùy launcher)

#### Bước 4: Test clear notification

- Mở lại app
- Notification tự động bị clear
- Badge trên BottomNav vẫn hiển thị đúng

### 4. Test trên Android 13+ (API 33+)

App sẽ tự động yêu cầu permission `POST_NOTIFICATIONS` khi cần.

Nếu muốn test permission flow:

```powershell
# Revoke permission
adb shell pm revoke com.example.kibo android.permission.POST_NOTIFICATIONS

# Grant permission
adb shell pm grant com.example.kibo android.permission.POST_NOTIFICATIONS
```

## Debug & troubleshooting

### Trigger worker ngay lập tức (không đợi 15 phút)

```powershell
adb shell cmd jobscheduler run -f com.example.kibo 1
```

Hoặc tạm thời đổi code để test (CHỈ DÙNG DEBUG):

```java
// Trong MainActivity.onCreate(), đổi interval
OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(
    CartBadgeWorker.class
).setInitialDelay(10, TimeUnit.SECONDS).build();

WorkManager.getInstance(this).enqueue(workRequest);
```

### Kiểm tra WorkManager status

```powershell
# View scheduled work
adb shell dumpsys jobscheduler | findstr "com.example.kibo"
```

### Kiểm tra logs

```powershell
# Filter WorkManager logs
adb logcat -s WM-WorkerWrapper

# Filter notification logs
adb logcat | findstr "NotificationHelper\|CartBadgeWorker"
```

### Badge không hiển thị?

- **Samsung/Xiaomi/Oppo launchers**: Một số OEM launcher yêu cầu permission riêng hoặc không hỗ trợ badge từ notification
- **Giải pháp**: Test trên Google Pixel launcher hoặc Nova Launcher
- **Alternative**: Có thể thêm thư viện `ShortcutBadger` để hỗ trợ nhiều launcher hơn:
  ```gradle
  implementation 'me.leolin:ShortcutBadger:1.1.22@aar'
  ```

### Worker không chạy sau 15 phút?

- Kiểm tra Battery Optimization settings của device
- Một số OEM (Xiaomi, Huawei) aggressive kill background tasks
- Whitelist app trong battery settings hoặc test trên stock Android

## Tùy chỉnh

### Đổi interval (chỉ giảm được khi debug)

```java
// Trong MainActivity, đổi 15 thành giá trị khác (>= 15 phút cho release)
PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
    CartBadgeWorker.class,
    30, TimeUnit.MINUTES  // Ví dụ: 30 phút
).build();
```

### Đổi notification icon

Trong `NotificationHelper.java`:

```java
.setSmallIcon(R.drawable.ic_cart_notification) // Thay bằng icon riêng
```

### Thêm sound/vibration

Trong `NotificationHelper.java`:

```java
builder.setDefaults(NotificationCompat.DEFAULT_ALL)
       .setVibrate(new long[]{0, 250, 250, 250});
```

## Yêu cầu hệ thống

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Badge support**: Android 8.0+ (API 26+) với launcher hỗ trợ
- **Notification permission**: Android 13+ (API 33+)

## Lưu ý quan trọng

1. **Battery optimization**: WorkManager tự động tối ưu battery, nhưng một số device có thể delay/skip execution
2. **Launcher compatibility**: Badge chỉ hiển thị trên launcher hỗ trợ (Google Pixel, Samsung One UI, etc.)
3. **Permission**: User có thể tắt notification permission → badge sẽ không cập nhật khi app đóng
4. **API rate limit**: Worker chạy mỗi 15 phút, cân nhắc rate limit trên backend nếu có nhiều user

## Tính năng có thể mở rộng

- [ ] Push notification với FCM để cập nhật real-time thay vì polling
- [ ] Thêm ShortcutBadger để hỗ trợ nhiều launcher OEM
- [ ] Cache cart count locally để giảm API calls
- [ ] Thêm notification action (Clear cart, View cart)
- [ ] Analytics tracking cho notification engagement

# Debug Badge Issue - Hướng dẫn

## Vấn đề

Cart đã có 2 sản phẩm nhưng badge không hiển thị trên BottomNavigationView

## Đã thêm log debug

Đã thêm log vào các method quan trọng để theo dõi flow:

### MainActivity

- `updateCartBadge(int count)` - Log mỗi lần badge được cập nhật
- `refreshCartBadge()` - Log khi fetch cart từ API

### CartFragment

- `showCartItems()` - Log khi hiển thị cart items và gọi updateCartBadge

## Cách debug

### Bước 1: Clear log và khởi động app

```powershell
# Clear log cũ
adb logcat -c

# Start monitoring log
adb logcat | Select-String "MainActivity.*Badge|CartFragment.*cart|updateCartBadge"
```

### Bước 2: Test flow

1. **Mở app và đăng nhập**
2. **Vào tab Cart** (tab có icon giỏ hàng)
3. **Xem log** để kiểm tra:
   - `CartFragment` có gọi `showCartItems()` không?
   - `showCartItems()` có gọi `updateCartBadge()` với số lượng đúng không?
   - `MainActivity.updateCartBadge()` có được gọi không?
   - `bottomNav` có null không?
   - Badge có được set thành công không?

### Bước 3: Kiểm tra các log quan trọng

#### Log nên thấy (khi có 2 sản phẩm):

```
CartFragment: showCartItems called with totalItems: 2
CartFragment: Calling updateCartBadge with count: 2
MainActivity: updateCartBadge called with count: 2
MainActivity: Badge set to visible with count: 2
MainActivity: ShortcutBadger applied count: 2
```

#### Nếu badge không hiển thị, kiểm tra:

**1. bottomNav là null?**

```
MainActivity: bottomNav is null!
```

→ **Nguyên nhân**: BottomNavigationView chưa được khởi tạo
→ **Giải pháp**: Đã sử dụng `bottomNav.post()` để delay việc gọi `refreshCartBadge()`

**2. refreshCartBadge() ghi đè badge về 0?**

```
MainActivity: refreshCartBadge() called
MainActivity: refreshCartBadge onResponse: count=0
MainActivity: updateCartBadge called with count: 0
```

→ **Nguyên nhân**: API trả về 0 items hoặc chưa login
→ **Giải pháp**: Kiểm tra session và cart ID

**3. CartFragment không gọi updateCartBadge?**

```
CartFragment: showCartItems called with totalItems: 2
CartFragment: getActivity() is not MainActivity!
```

→ **Nguyên nhân**: Activity không phải MainActivity
→ **Giải pháp**: Kiểm tra lại context

**4. Badge được set nhưng không hiển thị?**

```
MainActivity: Badge set to visible with count: 2
```

Nhưng vẫn không thấy trên UI
→ **Nguyên nhân**: Có thể là theme/style issue hoặc badge bị ẩn bởi một view khác
→ **Giải pháp**: Kiểm tra theme và layout

## Kiểm tra thêm

### Test badge trực tiếp

Thêm code test vào `MainActivity.onCreate()` (sau khi init bottomNav):

```java
// Test badge - remove after debugging
bottomNav.post(new Runnable() {
    @Override
    public void run() {
        BadgeDrawable testBadge = bottomNav.getOrCreateBadge(R.id.nav_cart);
        testBadge.setVisible(true);
        testBadge.setNumber(99);
        testBadge.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.primary_color));
        android.util.Log.d("MainActivity", "Test badge set to 99");
    }
});
```

Nếu badge "99" hiển thị → badge system hoạt động OK, vấn đề nằm ở logic cập nhật
Nếu badge "99" KHÔNG hiển thị → vấn đề nằm ở theme/layout

### Kiểm tra theme badge

Trong `app/src/main/res/values/themes.xml`, đảm bảo có:

```xml
<item name="badgeStyle">@style/Widget.MaterialComponents.Badge</item>
```

### Kiểm tra màu badge

Badge có thể bị ẩn nếu màu background trùng với màu text. Trong `MainActivity.updateCartBadge()`:

```java
badge.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
badge.setBadgeTextColor(ContextCompat.getColor(this, android.R.color.white));
```

Thử đổi sang màu dễ thấy hơn để test:

```java
badge.setBackgroundColor(0xFFFF0000); // Đỏ
badge.setBadgeTextColor(0xFFFFFFFF); // Trắng
```

## Log commands hữu ích

### Xem tất cả log của app

```powershell
adb logcat | Select-String "com.example.kibo"
```

### Xem log MainActivity và CartFragment

```powershell
adb logcat -s MainActivity:D CartFragment:D
```

### Xem log badge real-time

```powershell
adb logcat | Select-String "Badge|badge"
```

### Export log ra file

```powershell
adb logcat -d > app_log.txt
```

## Checklist

- [ ] App đã build với code mới (có log debug)
- [ ] App đã được cài đặt lại
- [ ] User đã đăng nhập
- [ ] Cart đã có sản phẩm (kiểm tra trong app)
- [ ] Đã vào tab Cart
- [ ] Đã check log để xem badge có được gọi không
- [ ] Badge có hiển thị không?

## Next steps

Sau khi đã có log, báo lại kết quả:

1. Badge có hiển thị không?
2. Log có xuất hiện không?
3. Số lượng trong log có đúng (2) không?
4. Có lỗi gì trong log không?

Nếu vẫn không được, có thể cần:

- Test với màu badge khác (đỏ, xanh) để đảm bảo badge system hoạt động
- Kiểm tra theme/style
- Kiểm tra layout overlap
- Test trên device/emulator khác

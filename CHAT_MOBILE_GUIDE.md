# Hướng dẫn sử dụng Chat Mobile

## Tổng quan
Màn hình chat cho phép người dùng (customers) chat trực tiếp với shop thông qua ứng dụng mobile. Hệ thống sử dụng API backend đã được tích hợp để xử lý tin nhắn real-time.

## Các tính năng đã hoàn thành

### ✅ 1. Models và API Integration
- **ChatMessage**: Model cho tin nhắn chat
- **Conversation**: Model cho cuộc trò chuyện
- **SendChatMessageRequest**: Request model để gửi tin nhắn
- **PaginationResponse**: Response model với phân trang
- **ApiService**: Đã thêm các endpoints chat:
  - `POST /api/Conversations/start` - Tạo/bắt đầu conversation
  - `GET /api/Conversations` - Lấy conversations của user
  - `GET /api/ChatMessages` - Lấy tin nhắn
  - `GET /api/ChatMessages/conversation/{id}` - Lấy tin nhắn theo conversation
  - `POST /api/ChatMessages` - Gửi tin nhắn
  - `DELETE /api/ChatMessages/{id}` - Xóa tin nhắn

### ✅ 2. UI Components
- **ChatActivity**: Màn hình chat chính với:
  - Toolbar với nút back và tiêu đề "Chat với Shop"
  - RecyclerView hiển thị tin nhắn
  - Input field để nhập tin nhắn
  - Nút gửi tin nhắn
  - Loading indicator
  - Typing indicator (UI ready)
  
- **item_chat_message.xml**: Layout cho từng tin nhắn với:
  - Tin nhắn từ shop (màu xám, bên trái)
  - Tin nhắn từ user (màu đỏ chủ đạo, bên phải)
  - Hiển thị thời gian gửi
  - Hiển thị tên người gửi

- **ChatMessageAdapter**: Adapter để hiển thị danh sách tin nhắn

### ✅ 3. Design System
- **Tone màu chủ đạo**: Tuân thủ design system của app
  - Primary color: `#E30019` (đỏ Kibo)
  - Background: Trắng
  - Text: Đen/xám
  - Message bubbles: Xám nhạt (shop) / Đỏ (user)
  
- **Drawable resources**:
  - `bg_shop_message_bubble.xml`: Bubble cho tin nhắn từ shop
  - `bg_user_message_bubble.xml`: Bubble cho tin nhắn từ user

### ✅ 4. Navigation Integration
- **AccountFragment**: Đã thêm nút "Chat với Shop" trong phần cài đặt
- **AndroidManifest.xml**: Đã đăng ký ChatActivity

### ✅ 5. Real-time Features
- **Auto-refresh**: Tự động tải tin nhắn mới mỗi 5 giây
- **Live updates**: Tin nhắn mới được thêm vào danh sách ngay lập tức
- **Auto-scroll**: Tự động scroll xuống tin nhắn mới nhất

## Cách sử dụng

### Cho User (Customer):
1. **Truy cập chat**: 
   - Vào tab "Tài khoản" trong bottom navigation
   - Chọn "Chat với Shop" trong phần cài đặt

2. **Gửi tin nhắn**:
   - Nhập tin nhắn vào ô input
   - Nhấn nút gửi (màu đỏ)
   - Tin nhắn sẽ xuất hiện ngay lập tức

3. **Nhận tin nhắn**:
   - Tin nhắn từ shop sẽ xuất hiện tự động
   - Hiển thị với tên "Shop" (không hiển thị tên cụ thể admin/staff)

### Tính năng kỹ thuật:
- **Conversation Management**: Mỗi user chỉ có 1 conversation duy nhất
- **Message Persistence**: Tin nhắn được lưu trữ và đồng bộ với backend
- **Error Handling**: Xử lý lỗi kết nối và hiển thị thông báo phù hợp
- **Session Management**: Sử dụng JWT token để xác thực

## Cấu trúc code

### File Structure:
```
app/src/main/java/com/example/kibo/
├── ChatActivity.java                    # Activity chính
├── adapters/
│   └── ChatMessageAdapter.java          # Adapter cho RecyclerView
├── models/
│   ├── ChatMessage.java                 # Model tin nhắn
│   ├── Conversation.java                # Model conversation
│   ├── SendChatMessageRequest.java      # Request model
│   └── PaginationResponse.java          # Response model
├── api/
│   └── ApiService.java                  # API endpoints (đã cập nhật)
└── ui/
    └── AccountFragment.java             # Fragment tài khoản (đã cập nhật)

app/src/main/res/
├── layout/
│   ├── activity_chat.xml                # Layout chính
│   └── item_chat_message.xml            # Layout tin nhắn
├── drawable/
│   ├── bg_shop_message_bubble.xml       # Bubble shop
│   └── bg_user_message_bubble.xml       # Bubble user
└── AndroidManifest.xml                  # Đã đăng ký activity
```

## API Endpoints được sử dụng

### 1. Tạo conversation:
```http
POST /api/Conversations/start
Authorization: Bearer {jwt_token}
```

### 2. Lấy tin nhắn:
```http
GET /api/ChatMessages/conversation/{conversationId}?page=1&pageSize=50
Authorization: Bearer {jwt_token}
```

### 3. Gửi tin nhắn:
```http
POST /api/ChatMessages
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "conversationId": 1,
  "message": "Xin chào shop!",
  "imageUrl": ""
}
```

## Lưu ý quan trọng

1. **Authentication**: Tất cả API calls đều yêu cầu JWT token hợp lệ
2. **Error Handling**: App sẽ hiển thị thông báo lỗi khi không thể kết nối
3. **Auto-refresh**: Tin nhắn được refresh mỗi 5 giây để đảm bảo real-time
4. **UI Consistency**: Thiết kế tuân thủ tone màu và style của app hiện tại
5. **Memory Management**: Handler được cleanup khi activity bị destroy

## Testing

Để test tính năng chat:
1. Đăng nhập với tài khoản customer
2. Vào AccountFragment và chọn "Chat với Shop"
3. Gửi tin nhắn test
4. Kiểm tra tin nhắn có được lưu và hiển thị đúng không

## Tương lai có thể mở rộng

- Push notifications khi có tin nhắn mới
- Hỗ trợ gửi hình ảnh
- Typing indicator thực tế
- Sound notifications
- Message status (sent, delivered, read)
- File sharing
- Voice messages

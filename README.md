# Kibo - Mechanical Keyboard E-Commerce Android App

Kibo is a full-featured Android e-commerce application built with Java, focused on mechanical keyboards and accessories. It provides both customer-facing and admin-facing functionality with a modern Material Design UI.

## Features

### Customer Features
- **User Authentication** — Login, register, and OTP email verification
- **Product Browsing** — Browse mechanical keyboards with filtering by category and price
- **Product Detail** — Detailed product info with image galleries and technical specifications (switch type, layout, keycap, connection, etc.)
- **Shopping Cart** — Add/remove items, update quantities, with real-time badge count
- **Wishlist** — Save favorite products for later
- **Order Management** — View order history, order details, and track shipping status via GHN
- **Real-time Chat** — Chat with shop staff using SignalR real-time messaging
- **Voucher System** — Apply discount codes at checkout
- **Store Locator** — Find store locations on Google Maps and OpenStreetMap
- **QR Code** — QR code generation for orders
- **Profile Management** — Edit personal info and shipping addresses (province/district/ward)
- **Push Notifications** — Background cart badge updates via WorkManager + ShortcutBadger

### Admin Features
- **Dashboard** — Analytics with charts (MPAndroidChart), KPIs, revenue overview
- **Product Management** — CRUD operations with image upload
- **Category Management** — Manage product brands/categories
- **Order Management** — View and manage all orders with GHN sync
- **Chat Management** — Customer conversation management with unread count badges

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java |
| UI | Material Design, RecyclerView, ViewPager2, CoordinatorLayout |
| Networking | Retrofit2, OkHttp, Gson |
| Real-time | SignalR (Microsoft) |
| Image Loading | Glide |
| Maps | Google Maps API & OpenStreetMap (osmdroid) |
| Charts | MPAndroidChart |
| QR Code | ZXing |
| Background Tasks | WorkManager |
| Badge Notifications | ShortcutBadger |
| Shipping | GHN API Integration |

## Architecture

The app follows a standard Android architecture with:

- **Activities** — Entry points for screens (LoginActivity, MainActivity, ProductDetailActivity, etc.)
- **Fragments** — Modular UI sections (HomeFragment, CartFragment, AccountFragment, AdminDashboardFragment, etc.)
- **Adapters** — RecyclerView adapters for lists and grids
- **Models** — POJOs for API request/response data
- **API** — Retrofit service interface and client singleton
- **Workers** — Background WorkManager workers for periodic tasks
- **Utilities** — SessionManager, NotificationHelper, WishlistHelper

## Screenshots

*(Add screenshots here)*

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11+
- Android SDK 36 (target), min SDK 24

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/kibo.git
   ```

2. **Open in Android Studio**
   - File → Open → Select the `kibo` directory

3. **Configure API endpoint**
   - Open `ApiClient.java` and update the `BASE_URL` to your backend server URL

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or run directly from Android Studio.

## API Integration

The app communicates with a RESTful backend via Retrofit2. Authentication uses JWT tokens stored in SharedPreferences. Key API modules:

- `Auth/` — Login, register, OTP verification
- `Products/` — Product CRUD with image upload
- `CartItems/` — Shopping cart operations
- `Orders/` — Order management
- `Shipping/` — GHN shipping fee calculation and order creation
- `ChatMessages/` — Customer support chat
- `Conversations/` — Chat conversation management
- `Wishlist/` — Wishlist CRUD
- `Vouchers/` — Discount voucher management
- `Address/` — Province/district/ward address data
- `StoreLocations/` — Physical store locations
- `Payments/` — Payment records

See [API_INTEGRATION_GUIDE.md](API_INTEGRATION_GUIDE.md) for detailed API documentation.

## Build Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36
- **Version**: 1.0.0

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/kibo/
│   │   │   ├── api/          # Retrofit API service & client
│   │   │   ├── models/       # Data models & DTOs
│   │   │   ├── ui/           # Fragments (Home, Cart, Account, Admin, etc.)
│   │   │   ├── adapters/     # RecyclerView adapters
│   │   │   ├── dialogs/      # Custom dialogs
│   │   │   ├── widgets/      # Custom views
│   │   │   ├── workers/      # WorkManager workers
│   │   │   ├── notifications/# Notification helper
│   │   │   ├── realtime/     # SignalR real-time manager
│   │   │   ├── utils/        # Utilities (SessionManager, WishlistHelper)
│   │   │   ├── *.java        # Activities
│   │   │
│   │   ├── res/              # Layouts, drawables, themes, etc.
│   │   └── AndroidManifest.xml
│   │
│   ├── test/                 # Unit tests
│   └── androidTest/          # Instrumented tests
│
├── build.gradle              # App module build config
├── gradle/                   # Gradle wrapper
└── settings.gradle           # Project settings
```

## License

This project is developed for educational purposes as part of the PRM392 course.

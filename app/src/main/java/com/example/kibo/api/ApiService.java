package com.example.kibo.api;

import com.example.kibo.models.ApiResponse;
import com.example.kibo.models.LoginRequest;
import com.example.kibo.models.LoginResponse;
import com.example.kibo.models.OtpRequest;
import com.example.kibo.models.RegisterRequest;
import com.example.kibo.models.User;
import com.example.kibo.models.Product;
import com.example.kibo.models.ProductResponse;
import com.example.kibo.models.Province;
import com.example.kibo.models.District;
import com.example.kibo.models.Ward;
import com.example.kibo.models.UpdateUserRequest;
import com.example.kibo.models.FullAddressResponse;
import com.example.kibo.models.CategoryResponse;
import com.example.kibo.models.Category;
import com.example.kibo.models.Cart;
import com.example.kibo.models.CartRequest;
import com.example.kibo.models.CartItemRequest;
import com.example.kibo.models.CartItem;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.models.UpdateQuantityRequest;
import com.example.kibo.models.RemoveCartItemRequest;
import com.example.kibo.models.ShippingFeeRequest;
import com.example.kibo.models.ShippingFeeResponse;
import com.example.kibo.models.UserResponse;
import com.example.kibo.models.VoucherResponse;
import com.example.kibo.models.VoucherUseResponse;
import com.example.kibo.models.ShippingOrderRequest;
import com.example.kibo.models.ShippingOrderResponse;
import com.example.kibo.models.ProductImage;
import com.example.kibo.models.Order;
import com.example.kibo.models.OrderDetail;
import com.example.kibo.models.OrderDetailsResponse;
import com.example.kibo.models.CategoryRequest;
import com.example.kibo.models.ChatMessage;
import com.example.kibo.models.Conversation;
import com.example.kibo.models.SendChatMessageRequest;
import com.example.kibo.models.PaginationResponse;
import com.example.kibo.models.ConversationResponse;
import com.example.kibo.models.UnreadCountResponse;
import com.example.kibo.models.StoreLocationsResponse;
import com.example.kibo.models.StoreLocation;
import com.example.kibo.models.WishlistItem;
import com.example.kibo.models.WishlistResponse;
import com.example.kibo.models.AddToWishlistRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.HTTP;

public interface ApiService {

        @POST("Auth/login")
        Call<LoginResponse> login(@Body LoginRequest loginRequest);

        @POST("Auth/register")
        Call<ApiResponse<String>> register(@Body RegisterRequest registerRequest);

        @POST("Auth/verify-otp")
        Call<LoginResponse> verifyOtp(@Body OtpRequest otpRequest);

        @POST("Auth/resend-otp")
        Call<ApiResponse<String>> resendOtp(@Body String email);

        // Alternative method for login that returns raw response
        @POST("Auth/login")
        Call<Object> loginRaw(@Body LoginRequest loginRequest);

        // Get products with pagination
        @GET("Products")
        Call<ProductResponse> getProducts(
                        @Query("pageNumber") int pageNumber,
                        @Query("pageSize") int pageSize);

        // Get products by category with pagination
        @GET("Products")
        Call<ProductResponse> getProductsByCategory(
                        @Query("Categoryid") int categoryId,
                        @Query("pageNumber") int pageNumber,
                        @Query("pageSize") int pageSize);

        // Get all products without pagination
        @GET("Products")
        Call<ProductResponse> getAllProducts();

    // Get product by ID
    @GET("Products")
    Call<ProductResponse> getProductById(@Query("Productid") int productId);
    
    @GET("Products")
    Call<ProductResponse> getProductsFiltered(
            @Query("CategoryId") Integer categoryId,
            @Query("MinPrice") Double minPrice,
            @Query("MaxPrice") Double maxPrice
    );


        // Get product detail by ID (direct endpoint)
        @GET("Products/{id}")
        Call<Product> getProductDetail(@Path("id") int productId);

        // Get product images
        @GET("ProductImages/product/{productId}")
        Call<List<ProductImage>> getProductImages(@Path("productId") int productId);

        // Create product (multipart/form-data) with multiple images
        @Multipart
        @POST("Products")
        Call<ApiResponse<String>> createProduct(
                        @Part("Productname") okhttp3.RequestBody productname,
                        @Part("Briefdescription") okhttp3.RequestBody briefdescription,
                        @Part("Fulldescription") okhttp3.RequestBody fulldescription,
                        @Part("Price") okhttp3.RequestBody price,
                        @Part("Categoryid") okhttp3.RequestBody categoryid,
                        @Part("Connection") okhttp3.RequestBody connection,
                        @Part("Layout") okhttp3.RequestBody layout,
                        @Part("Keycap") okhttp3.RequestBody keycap,
                        @Part("Switch") okhttp3.RequestBody switchType,
                        @Part("Battery") okhttp3.RequestBody battery,
                        @Part("Os") okhttp3.RequestBody os,
                        @Part("Led") okhttp3.RequestBody led,
                        @Part("Screen") okhttp3.RequestBody screen,
                        @Part("Width") okhttp3.RequestBody width,
                        @Part("Length") okhttp3.RequestBody length,
                        @Part("Height") okhttp3.RequestBody height,
                        @Part("Quantity") okhttp3.RequestBody quantity,
                        @Part java.util.List<okhttp3.MultipartBody.Part> ImageFiles);

        // Update product (multipart/form-data) - CHỈNH SỬA ĐỂ NHẬN NHIỀU ẢNH
        @Multipart
        @PUT("Products/{id}")
        Call<ApiResponse<String>> updateProduct(
                        @Path("id") int id,
                        @Part("Productid") okhttp3.RequestBody productid,
                        @Part("Productname") okhttp3.RequestBody productname,
                        @Part("Briefdescription") okhttp3.RequestBody briefdescription,
                        @Part("Fulldescription") okhttp3.RequestBody fulldescription,
                        @Part("Price") okhttp3.RequestBody price,
                        @Part("Categoryid") okhttp3.RequestBody categoryid,
                        @Part("Connection") okhttp3.RequestBody connection,
                        @Part("Layout") okhttp3.RequestBody layout,
                        @Part("Keycap") okhttp3.RequestBody keycap,
                        @Part("Switch") okhttp3.RequestBody switchType,
                        @Part("Battery") okhttp3.RequestBody battery,
                        @Part("Os") okhttp3.RequestBody os,
                        @Part("Led") okhttp3.RequestBody led,
                        @Part("Screen") okhttp3.RequestBody screen,
                        @Part("Width") okhttp3.RequestBody width,
                        @Part("Length") okhttp3.RequestBody length,
                        @Part("Height") okhttp3.RequestBody height,
                        @Part("Quantity") okhttp3.RequestBody quantity,
                        @Part java.util.List<okhttp3.MultipartBody.Part> ImageFiles,
                        @Part("PrimaryImageIndex") okhttp3.RequestBody primaryImageIndex);

        // Delete product
        @HTTP(method = "DELETE", path = "Products/{id}", hasBody = false)
        Call<ApiResponse<String>> deleteProduct(@Path("id") int productId);

        // Address endpoints
        @GET("Address/provinces")
        Call<List<Province>> getProvinces();

        @GET("Address/provinces/{provinceID}/districts")
        Call<List<District>> getDistricts(@Path("provinceID") int provinceID);

        @GET("Address/districts/{districtID}/wards")
        Call<List<Ward>> getWards(@Path("districtID") int districtID);

        @GET("Address/full-address/{provinceId}/{districtId}/{wardCode}")
        Call<FullAddressResponse> getFullAddress(
                        @Path("provinceId") int provinceId,
                        @Path("districtId") int districtId,
                        @Path("wardCode") String wardCode);

        // Update user
        @PUT("Users/{id}")
        Call<User> updateUser(@Path("id") int userId, @Body UpdateUserRequest updateUserRequest);

        // Cart endpoints
        @POST("Carts")
        Call<Cart> createCart(@Body CartRequest cartRequest);

        // Get all carts of a user
        @GET("Carts")
        Call<PaginationResponse<Cart>> getCarts(@Query("Userid") int userId, @Query("PageSize") int pageSize);

        @POST("CartItems/add")
        Call<ApiResponse<String>> addToCart(@Body CartItemRequest cartItemRequest);

        @GET("CartItems")
        Call<CartItemsResponse> getCartItems(@Query("Cartid") int cartId);

        @PUT("CartItems/update-quantity")
        Call<ApiResponse<String>> updateCartItemQuantity(@Body UpdateQuantityRequest request);

        @HTTP(method = "DELETE", path = "CartItems/remove", hasBody = true)
        Call<ApiResponse<String>> removeCartItem(@Body RemoveCartItemRequest request);

        // Logout endpoint
        @POST("Auth/logout")
        Call<ApiResponse<String>> logout();

        // Categories for brand dropdown
        @GET("Categories")
        Call<CategoryResponse> getCategories();

        // Create category
        @POST("Categories")
        Call<ApiResponse<Category>> createCategory(@Body CategoryRequest request);

        // Update category
        @PUT("Categories/{id}")
        Call<Void> updateCategory(@Path("id") int id, @Body CategoryRequest request);

        // Delete category
        @HTTP(method = "DELETE", path = "Categories/{id}", hasBody = false)
        Call<ApiResponse<String>> deleteCategory(@Path("id") int id);

        // Get user by ID
        @GET("Users")
        Call<UserResponse> getUserById(@Query("Userid") int userId);

        // Orders
        @GET("Orders")
        Call<java.util.List<Order>> getOrders(@Query("UserId") int userId);

        // Admin: get all orders (no user filter)
        @GET("Orders")
        Call<java.util.List<Order>> getAllOrders();

        // Sync all orders with GHN (server-side)
        @POST("Shipping/orders/sync-with-ghn")
        Call<Object> syncOrdersWithGHN();

        @GET("OrderDetails")
        Call<OrderDetailsResponse> getOrderDetails(@Query("OrderId") int orderId);

        // Calculate shipping fee
        @POST("Shipping/fee")
        Call<ShippingFeeResponse> calculateShippingFee(@Body ShippingFeeRequest request);

        // Get vouchers
        @GET("Vouchers")
        Call<VoucherResponse> getVouchers(@Query("isActive") boolean isActive);

        // Use voucher
        @POST("Vouchers/use/{code}")
        Call<VoucherUseResponse> useVoucher(@Path("code") String voucherCode, @Body double orderValue);

        // Create shipping order
        @POST("Shipping/order")
        Call<ShippingOrderResponse> createShippingOrder(@Body ShippingOrderRequest request);

        // Check payment status
        @GET("Payments")
        Call<java.util.List<com.example.kibo.models.Payment>> getPaymentStatus(@Query("PaymentId") int paymentId);

        // Get all payments for admin dashboard with pagination and date filtering
        @GET("Payments")
        Call<java.util.List<com.example.kibo.models.Payment>> getAllPayments(
                        @Query("Page") int page,
                        @Query("PageSize") int pageSize,
                        @Query("StartDate") String startDate,
                        @Query("EndDate") String endDate);

        // Chat endpoints
        @POST("Conversations/start")
        Call<Conversation> startConversation();

        @GET("Conversations")
        Call<PaginationResponse<Conversation>> getConversations(
                        @Query("page") int page,
                        @Query("pageSize") int pageSize);

        @GET("ChatMessages")
        Call<PaginationResponse<ChatMessage>> getChatMessages(
                        @Query("page") int page,
                        @Query("pageSize") int pageSize);

        @GET("ChatMessages/conversation/{conversationId}")
        Call<PaginationResponse<ChatMessage>> getMessagesByConversation(
                        @Path("conversationId") int conversationId,
                        @Query("page") int page,
                        @Query("pageSize") int pageSize);

        @POST("ChatMessages")
        Call<ChatMessage> sendChatMessage(@Body SendChatMessageRequest request);

        @HTTP(method = "DELETE", path = "ChatMessages/{messageId}", hasBody = false)
        Call<ApiResponse<String>> deleteChatMessage(@Path("messageId") int messageId);

        // Admin chat endpoints - sử dụng endpoint Conversations với admin role
        @GET("Conversations")
        Call<PaginationResponse<ConversationResponse>> getAllConversations(
                        @Query("page") int page,
                        @Query("pageSize") int pageSize);

        @GET("admin/AdminChat/conversations/{conversationId}/messages")
        Call<PaginationResponse<ChatMessage>> getConversationMessages(
                        @Path("conversationId") int conversationId,
                        @Query("page") int page,
                        @Query("pageSize") int pageSize);

        @POST("admin/AdminChat/conversations/{conversationId}/messages")
        Call<ChatMessage> sendMessageToCustomer(
                        @Path("conversationId") int conversationId,
                        @Body SendChatMessageRequest request);

        @HTTP(method = "DELETE", path = "admin/AdminChat/messages/{messageId}", hasBody = false)
        Call<ApiResponse<String>> deleteMessage(@Path("messageId") int messageId);

        @POST("admin/AdminChat/conversations/{conversationId}/read")
        Call<ApiResponse<String>> markConversationAsRead(@Path("conversationId") int conversationId);

        @GET("admin/AdminChat/conversations/{conversationId}/unread-count")
        Call<ApiResponse<UnreadCountResponse>> getUnreadMessageCount(@Path("conversationId") int conversationId);

        // Store locations
        @GET("StoreLocations")
        Call<StoreLocationsResponse> getStoreLocations(
                        @Query("Page") int page,
                        @Query("PageSize") int pageSize);

        @GET("StoreLocations")
        Call<StoreLocationsResponse> getStoreLocationById(@Query("Locationid") int locationId,
                        @Query("Page") int page,
                        @Query("PageSize") int pageSize);

        // Wishlist endpoints
        @GET("Wishlist")
        Call<List<WishlistResponse>> getWishlist(@Query("userid") int userId);

        @POST("Wishlist/add")
        Call<List<WishlistResponse>> addToWishlist(@Body AddToWishlistRequest request);

        @DELETE("Wishlist/by-user-product")
        Call<ApiResponse<String>> removeFromWishlist(@Query("userId") int userId, @Query("productId") int productId);

        // GHN Tracking endpoint (direct API call to GHN)
        @POST("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/detail")
        Call<Object> getGHNTrackingDetail(@retrofit2.http.Header("Token") String token, @Body Object requestBody);
}

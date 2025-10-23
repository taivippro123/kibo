package com.example.kibo.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.AddToWishlistRequest;
import com.example.kibo.models.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistHelper {
    private static final String TAG = "WishlistHelper";

    public interface WishlistCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    /**
     * Add a product to wishlist
     */
    public static void addToWishlist(Context context, int productId, WishlistCallback callback) {
        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            if (callback != null) {
                callback.onError("Vui lòng đăng nhập để thêm vào wishlist");
            }
            return;
        }

        ApiService apiService = ApiClient.getApiServiceWithAuth(context);
        AddToWishlistRequest request = new AddToWishlistRequest(userId, new int[] { productId });

        Call<ApiResponse<String>> call = apiService.addToWishlist(request);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Product added to wishlist successfully");
                    if (callback != null) {
                        callback.onSuccess("Đã thêm vào danh sách yêu thích");
                    }
                } else {
                    Log.e(TAG, "Failed to add to wishlist: " + response.code());
                    if (callback != null) {
                        callback.onError("Không thể thêm vào wishlist");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error adding to wishlist", t);
                if (callback != null) {
                    callback.onError("Lỗi kết nối: " + t.getMessage());
                }
            }
        });
    }

    /**
     * Remove a product from wishlist
     */
    public static void removeFromWishlist(Context context, int productId, WishlistCallback callback) {
        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            if (callback != null) {
                callback.onError("Vui lòng đăng nhập");
            }
            return;
        }

        ApiService apiService = ApiClient.getApiServiceWithAuth(context);

        Call<ApiResponse<String>> call = apiService.removeFromWishlist(userId, productId);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Product removed from wishlist successfully");
                    if (callback != null) {
                        callback.onSuccess("Đã xóa khỏi danh sách yêu thích");
                    }
                } else {
                    Log.e(TAG, "Failed to remove from wishlist: " + response.code());
                    if (callback != null) {
                        callback.onError("Không thể xóa khỏi wishlist");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error removing from wishlist", t);
                if (callback != null) {
                    callback.onError("Lỗi kết nối: " + t.getMessage());
                }
            }
        });
    }
}

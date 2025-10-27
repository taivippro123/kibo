package com.example.kibo.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.CartItemsResponse;
import com.example.kibo.notifications.NotificationHelper;
import com.example.kibo.utils.SessionManager;

import retrofit2.Response;

public class CartBadgeWorker extends Worker {

    public CartBadgeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        try {
            SessionManager sessionManager = new SessionManager(ctx);
            if (!sessionManager.isLoggedIn() || !sessionManager.hasActiveCart()) {
                // Ensure notification shows zero to clear badge if desired
                NotificationHelper.showCartNotification(ctx, 0);
                return Result.success();
            }

            int cartId = sessionManager.getActiveCartId();
            ApiService apiService = ApiClient.getApiServiceWithAuth(ctx);

            // Synchronous call is ok inside Worker
            Response<CartItemsResponse> response = apiService.getCartItems(cartId).execute();
            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                int count = response.body().getData().size();
                NotificationHelper.showCartNotification(ctx, count);
            } else {
                NotificationHelper.showCartNotification(ctx, 0);
            }

            return Result.success();
        } catch (Exception e) {
            // Retry on transient failures
            return Result.retry();
        }
    }
}

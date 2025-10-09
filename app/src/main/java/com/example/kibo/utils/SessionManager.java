package com.example.kibo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.kibo.models.User;

/**
 * Session Manager to handle user session data
 * Provides easy access to stored login information
 */
public class SessionManager {
    private static final String PREF_NAME = "KiboPrefs";
    
    // Token keys
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRES_AT = "token_expires_at";
    
    // User basic info keys
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_ROLE_NAME = "user_role_name";
    
    // User address info keys
    private static final String KEY_USER_ADDRESS = "user_address";
    private static final String KEY_USER_PROVINCE_ID = "user_province_id";
    private static final String KEY_USER_DISTRICT_ID = "user_district_id";
    private static final String KEY_USER_WARD_ID = "user_ward_id";
    
    // Login flags
    private static final String KEY_IS_FIRST_LOGIN = "is_first_login";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    // Cart info
    private static final String KEY_ACTIVE_CART_ID = "active_cart_id";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    // ============ Token Methods ============
    
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_USER_TOKEN, null);
    }
    
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }
    
    public String getTokenExpiresAt() {
        return sharedPreferences.getString(KEY_TOKEN_EXPIRES_AT, null);
    }
    
    // ============ User Info Methods ============
    
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }
    
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserPhone() {
        return sharedPreferences.getString(KEY_USER_PHONE, "");
    }
    
    public int getUserRole() {
        return sharedPreferences.getInt(KEY_USER_ROLE, 0);
    }
    
    public String getUserRoleName() {
        return sharedPreferences.getString(KEY_USER_ROLE_NAME, "");
    }
    
    public String getUserAddress() {
        return sharedPreferences.getString(KEY_USER_ADDRESS, "");
    }
    
    public int getProvinceId() {
        return sharedPreferences.getInt(KEY_USER_PROVINCE_ID, 0);
    }
    
    public int getDistrictId() {
        return sharedPreferences.getInt(KEY_USER_DISTRICT_ID, 0);
    }
    
    public int getWardId() {
        return sharedPreferences.getInt(KEY_USER_WARD_ID, 0);
    }
    
    // ============ Login Status Methods ============
    
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public boolean isFirstLogin() {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LOGIN, false);
    }
    
    // ============ Get Full User Object ============
    
    /**
     * Get the complete user object from stored data
     * @return User object with all stored information
     */
    public User getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setUserid(getUserId());
        user.setUsername(getUserName());
        user.setEmail(getUserEmail());
        user.setPhonenumber(getUserPhone());
        user.setRole(getUserRole());
        user.setRoleName(getUserRoleName());
        user.setAddress(getUserAddress());
        user.setProvinceid(getProvinceId());
        user.setDistrictid(getDistrictId());
        user.setWardid(getWardId());
        
        return user;
    }
    
    // ============ Update Methods ============
    
    /**
     * Update user phone number
     */
    public void updateUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }
    
    /**
     * Update user address information
     */
    public void updateUserAddress(String address, int provinceId, int districtId, int wardId) {
        editor.putString(KEY_USER_ADDRESS, address);
        editor.putInt(KEY_USER_PROVINCE_ID, provinceId);
        editor.putInt(KEY_USER_DISTRICT_ID, districtId);
        editor.putInt(KEY_USER_WARD_ID, wardId);
        editor.apply();
    }
    
    /**
     * Update user name
     */
    public void updateUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }
    
    /**
     * Update access token and expiration
     */
    public void updateAccessToken(String token, String expiresAt) {
        editor.putString(KEY_USER_TOKEN, token);
        if (expiresAt != null) {
            editor.putString(KEY_TOKEN_EXPIRES_AT, expiresAt);
        }
        editor.apply();
    }
    
    /**
     * Mark that this is no longer the first login
     */
    public void setFirstLoginCompleted() {
        editor.putBoolean(KEY_IS_FIRST_LOGIN, false);
        editor.apply();
    }
    
    // ============ Clear Session ============
    
    /**
     * Clear all user session data (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
    
    // ============ Helper Methods ============
    
    /**
     * Check if user has completed their profile (has address info)
     */
    public boolean hasCompleteProfile() {
        return !getUserAddress().isEmpty() && 
               getProvinceId() > 0 && 
               getDistrictId() > 0 && 
               getWardId() > 0;
    }
    
    /**
     * Check if user is a customer (role = 1)
     */
    public boolean isCustomer() {
        return getUserRole() == 1;
    }
    
    /**
     * Check if user is an admin (role = 2)
     */
    public boolean isAdmin() {
        return getUserRole() == 2;
    }
    
    // ============ Cart Methods ============
    
    /**
     * Get active cart ID
     */
    public int getActiveCartId() {
        return sharedPreferences.getInt(KEY_ACTIVE_CART_ID, -1);
    }
    
    /**
     * Set active cart ID
     */
    public void setActiveCartId(int cartId) {
        editor.putInt(KEY_ACTIVE_CART_ID, cartId);
        editor.apply();
    }
    
    /**
     * Clear active cart ID (when order is completed)
     */
    public void clearActiveCartId() {
        editor.remove(KEY_ACTIVE_CART_ID);
        editor.apply();
    }
    
    /**
     * Check if user has an active cart
     */
    public boolean hasActiveCart() {
        return getActiveCartId() != -1;
    }
}


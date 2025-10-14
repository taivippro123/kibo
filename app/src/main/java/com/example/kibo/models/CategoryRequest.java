package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class CategoryRequest {
    @SerializedName("categoryname")
    private String categoryName;
    
    @SerializedName("categoryid")
    private int categoryId;

    public CategoryRequest(String categoryName) {
        this.categoryName = categoryName;
        this.categoryId = 0; // Default value
    }
    
    public CategoryRequest(String categoryName, int categoryId) {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}

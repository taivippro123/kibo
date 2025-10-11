package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class CategoryRequest {
    @SerializedName("categoryname")
    private String categoryName;

    public CategoryRequest(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}

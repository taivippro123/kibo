package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {
    // API có thể trả về "data" hoặc "products"
    @SerializedName(value = "data", alternate = {"products"})
    private List<Product> data;
    
    @SerializedName("totalPages")
    private int totalPages;
    
    @SerializedName("totalItems")
    private int totalItems;
    
    @SerializedName("currentPage")
    private int currentPage;
    
    @SerializedName("pageSize")
    private int pageSize;
    
    @SerializedName("hasNextPage")
    private boolean hasNextPage;
    
    @SerializedName("hasPreviousPage")
    private boolean hasPreviousPage;

    // Getters
    public List<Product> getData() {
        return data;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }
}


package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserResponse {
    @SerializedName("data")
    private List<User> data;
    
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
    
    public List<User> getData() {
        return data;
    }
    
    public void setData(List<User> data) {
        this.data = data;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public boolean isHasNextPage() {
        return hasNextPage;
    }
    
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
    
    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }
    
    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }
}


package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StoreLocationsResponse {
    @SerializedName("data")
    private List<StoreLocation> data;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalItems")
    private int totalItems;

    @SerializedName("currentPage")
    private int currentPage;

    @SerializedName("pageSize")
    private int pageSize;

    public List<StoreLocation> getData() { return data; }
    public int getTotalPages() { return totalPages; }
    public int getTotalItems() { return totalItems; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }
}



package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VoucherResponse {
    @SerializedName("vouchers")
    private List<Voucher> vouchers;
    
    @SerializedName("totalPages")
    private int totalPages;
    
    @SerializedName("totalItems")
    private int totalItems;
    
    @SerializedName("currentPage")
    private int currentPage;
    
    @SerializedName("pageSize")
    private int pageSize;
    
    public List<Voucher> getVouchers() {
        return vouchers;
    }
    
    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
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
}

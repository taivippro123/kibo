package com.example.kibo.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.kibo.R;
import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.Payment;
import com.example.kibo.models.OrderDetail;
import com.example.kibo.models.OrderDetailsResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminDashboardFragment extends Fragment {
    private BarChart revenueChart;
    private BarChart bestSellingChart;
    private ProgressBar loadingIndicator;
    private TextView errorMessage;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        setupViews(view);
        loadRevenueData();
        loadBestSellingProducts();

        return view;
    }

    private void setupViews(View view) {
        revenueChart = view.findViewById(R.id.revenueChart);
        bestSellingChart = view.findViewById(R.id.bestSellingChart);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        errorMessage = view.findViewById(R.id.errorMessage);
        apiService = ApiClient.getApiService();
        
        setupRevenueChart();
        setupBestSellingChart();
    }

    private void setupRevenueChart() {
        // Cấu hình chart cơ bản
        revenueChart.getDescription().setEnabled(false);
        revenueChart.setPinchZoom(false);
        revenueChart.setDoubleTapToZoomEnabled(false);
        revenueChart.setScaleEnabled(false);
        revenueChart.setDrawBarShadow(false);
        revenueChart.setDrawValueAboveBar(true);
        
        // Tắt description
        revenueChart.getDescription().setEnabled(false);
        
        // Cấu hình X-axis với mũi tên
        XAxis xAxis = revenueChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7, false);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.parseColor("#333333"));
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.parseColor("#333333"));
        xAxis.setDrawAxisLine(true);
        
        // Label cho trục X (hiển thị ngày)
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return super.getFormattedValue(value);
            }
        });
        
        // Cấu hình Y-axis với mũi tên (bỏ VNĐ ở các mốc)
        YAxis leftAxis = revenueChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisLineWidth(2f);
        leftAxis.setAxisLineColor(Color.parseColor("#333333"));
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.parseColor("#333333"));
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setSpaceTop(20f); // Tăng khoảng cách phía trên
        leftAxis.setLabelCount(6, false); // Giảm số lượng labels để tránh chồng lên
        
        // Custom formatter cho Y-axis (đổi M thành triệu)
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format("%.1f triệu", value / 1000000);
                } else if (value >= 1000) {
                    return String.format("%.0fK", value / 1000);
                } else {
                    return String.format("%.0f", value);
                }
            }
        });
        
        // Cấu hình X-axis labels
        xAxis.setLabelCount(4, false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        
        YAxis rightAxis = revenueChart.getAxisRight();
        rightAxis.setEnabled(false);
        
        // Cấu hình legend
        revenueChart.getLegend().setEnabled(false);
        
        // Animation
        revenueChart.animateY(1500);
    }

    private void setupBestSellingChart() {
        // Cấu hình chart cơ bản
        bestSellingChart.getDescription().setEnabled(false);
        bestSellingChart.setPinchZoom(false);
        bestSellingChart.setDoubleTapToZoomEnabled(false);
        bestSellingChart.setScaleEnabled(false);
        bestSellingChart.setDrawBarShadow(false);
        bestSellingChart.setDrawValueAboveBar(true);
        
        // Cấu hình X-axis
        XAxis xAxis = bestSellingChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.parseColor("#333333"));
        xAxis.setAxisLineWidth(2f);
        xAxis.setAxisLineColor(Color.parseColor("#333333"));
        xAxis.setDrawAxisLine(true);
        
        // Cấu hình Y-axis
        YAxis leftAxis = bestSellingChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisLineWidth(2f);
        leftAxis.setAxisLineColor(Color.parseColor("#333333"));
        leftAxis.setDrawAxisLine(true);
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.parseColor("#333333"));
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setSpaceTop(20f);
        leftAxis.setGranularity(1f);
        leftAxis.setGranularityEnabled(true);
        
        // Custom formatter cho Y-axis (hiển thị số lượng đẹp hơn)
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == (int) value) {
                    return String.format("%.0f", value);
                } else {
                    return String.format("%.1f", value);
                }
            }
        });
        
        YAxis rightAxis = bestSellingChart.getAxisRight();
        rightAxis.setEnabled(false);
        
        // Cấu hình legend
        bestSellingChart.getLegend().setEnabled(false);
        
        // Animation
        bestSellingChart.animateY(1500);
    }

    private void loadRevenueData() {
        loadingIndicator.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
        
        // Tính toán ngày bắt đầu (10 ngày trước) và ngày kết thúc (hôm nay)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // Ngày kết thúc: hôm nay + 1 ngày để bao gồm cả ngày hôm nay
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String endDate = dateFormat.format(calendar.getTime());
        
        // Ngày bắt đầu: 10 ngày trước
        calendar.add(Calendar.DAY_OF_MONTH, -11);
        String startDate = dateFormat.format(calendar.getTime());
        
        // Gọi API với pagination và date filtering
        apiService.getAllPayments(1, 100, startDate, endDate).enqueue(new Callback<List<Payment>>() {
            @Override
            public void onResponse(Call<List<Payment>> call, Response<List<Payment>> response) {
                loadingIndicator.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    processPaymentData(response.body());
                } else {
                    showError("Không thể tải dữ liệu thanh toán - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Payment>> call, Throwable t) {
                loadingIndicator.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadBestSellingProducts() {
        // Tính toán ngày bắt đầu (10 ngày trước) và ngày kết thúc (hôm nay)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        
        // Ngày kết thúc: hôm nay + 1 ngày để bao gồm cả ngày hôm nay
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String endDate = dateFormat.format(calendar.getTime());
        
        // Ngày bắt đầu: 10 ngày trước
        calendar.add(Calendar.DAY_OF_MONTH, -11);
        String startDate = dateFormat.format(calendar.getTime());
        
        // Gọi API để lấy payments
        apiService.getAllPayments(1, 100, startDate, endDate).enqueue(new Callback<List<Payment>>() {
            @Override
            public void onResponse(Call<List<Payment>> call, Response<List<Payment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processBestSellingProducts(response.body());
                } else {
                    showError("Không thể tải dữ liệu sản phẩm bán chạy - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Payment>> call, Throwable t) {
                showError("Lỗi kết nối khi tải sản phẩm bán chạy: " + t.getMessage());
            }
        });
    }

    private void processBestSellingProducts(List<Payment> payments) {
        // Map để lưu tổng quantity theo productName
        Map<String, Integer> productSales = new HashMap<>();
        
        if (payments.isEmpty()) {
            showError("Không có dữ liệu thanh toán để tính sản phẩm bán chạy");
            return;
        }
        
        // Đếm số lượng payments để biết khi nào hoàn thành
        final int totalPayments = payments.size();
        final int[] completedRequests = {0};
        
        // Gọi API getOrderDetails cho mỗi payment
        for (Payment payment : payments) {
            apiService.getOrderDetails(payment.getOrderId()).enqueue(new Callback<OrderDetailsResponse>() {
                @Override
                public void onResponse(Call<OrderDetailsResponse> call, Response<OrderDetailsResponse> response) {
                    synchronized (productSales) {
                        completedRequests[0]++;
                        
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            // Cộng dồn quantity theo productName
                            for (OrderDetail orderDetail : response.body().getData()) {
                                String productName = orderDetail.getProductName();
                                if (productName != null && !productName.isEmpty()) {
                                    int currentQuantity = productSales.getOrDefault(productName, 0);
                                    productSales.put(productName, currentQuantity + orderDetail.getQuantity());
                                }
                            }
                        }
                        
                        // Khi hoàn thành tất cả requests, hiển thị chart
                        if (completedRequests[0] == totalPayments) {
                            getActivity().runOnUiThread(() -> displayBestSellingChart(productSales));
                        }
                    }
                }

                @Override
                public void onFailure(Call<OrderDetailsResponse> call, Throwable t) {
                    synchronized (productSales) {
                        completedRequests[0]++;
                        
                        // Khi hoàn thành tất cả requests, hiển thị chart
                        if (completedRequests[0] == totalPayments) {
                            getActivity().runOnUiThread(() -> displayBestSellingChart(productSales));
                        }
                    }
                }
            });
        }
    }

    private void displayBestSellingChart(Map<String, Integer> productSales) {
        if (productSales.isEmpty()) {
            // Hiển thị chart trống với message
            bestSellingChart.clear();
            bestSellingChart.invalidate();
            return;
        }
        
        // Sắp xếp theo quantity giảm dần và lấy top 5
        List<Map.Entry<String, Integer>> sortedProducts = new ArrayList<>(productSales.entrySet());
        sortedProducts.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // Lấy top 5 sản phẩm
        int maxProducts = Math.min(5, sortedProducts.size());
        List<Map.Entry<String, Integer>> topProducts = sortedProducts.subList(0, maxProducts);
        
        // Tạo BarEntry data
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < topProducts.size(); i++) {
            Map.Entry<String, Integer> entry = topProducts.get(i);
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
        }
        
        // Tạo dataset với màu sắc đẹp
        BarDataSet dataSet = new BarDataSet(entries, "Số lượng bán");
        
        int[] colors = {
            Color.parseColor("#FF6B35"), // Cam
            Color.parseColor("#4ECDC4"), // Xanh ngọc
            Color.parseColor("#45B7D1"), // Xanh dương
            Color.parseColor("#96CEB4"), // Xanh lá
            Color.parseColor("#FECA57")  // Vàng
        };
        
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#333333"));
        dataSet.setDrawValues(true);
        
        // Custom formatter cho giá trị trên cột
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });
        
        // Set data to chart
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        bestSellingChart.setData(barData);
        
        // Tính toán scale động cho Y-axis
        int maxQuantity = 0;
        for (Map.Entry<String, Integer> entry : topProducts) {
            maxQuantity = Math.max(maxQuantity, entry.getValue());
        }
        
        // Tự động tính số lượng labels phù hợp
        int labelCount = Math.min(6, maxQuantity + 1);
        if (maxQuantity > 10) {
            labelCount = 6; // Tối đa 6 labels để không bị chồng
        }
        
        // Cấu hình Y-axis với scale động
        YAxis leftAxis = bestSellingChart.getAxisLeft();
        leftAxis.setAxisMaximum(maxQuantity * 1.1f); // Thêm 10% padding
        leftAxis.setLabelCount(labelCount, false);
        
        // Set labels for X-axis
        bestSellingChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        
        // Set số lượng labels hiển thị cho X-axis
        bestSellingChart.getXAxis().setLabelCount(labels.size(), false);
        
        bestSellingChart.invalidate(); // Refresh chart
    }

    private void processPaymentData(List<Payment> payments) {
        // Group payments by date
        Map<String, Double> dailyRevenue = new TreeMap<>();
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        for (Payment payment : payments) {
            try {
                Date paymentDate = inputFormat.parse(payment.getPaymentDate());
                String dateKey = outputFormat.format(paymentDate);
                
                double currentAmount = dailyRevenue.getOrDefault(dateKey, 0.0);
                dailyRevenue.put(dateKey, currentAmount + payment.getAmount());
                
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        
        if (dailyRevenue.isEmpty()) {
            showError("Không có dữ liệu doanh thu trong khoảng thời gian này");
            return;
        }
        
        // Tạo BarEntry data với tất cả ngày có dữ liệu
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>(dailyRevenue.keySet());
        
        for (int i = 0; i < labels.size(); i++) {
            String date = labels.get(i);
            double revenue = dailyRevenue.get(date);
            entries.add(new BarEntry(i, (float) revenue));
        }
        
        // Tạo dataset với màu sắc đẹp cho từng cột
        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        
        // Màu sắc gradient đẹp cho các cột
        int[] colors = {
            Color.parseColor("#FF6B35"), // Cam
            Color.parseColor("#4ECDC4"), // Xanh ngọc
            Color.parseColor("#45B7D1"), // Xanh dương
            Color.parseColor("#96CEB4"), // Xanh lá
            Color.parseColor("#FECA57"), // Vàng
            Color.parseColor("#FF9FF3"), // Hồng
            Color.parseColor("#54A0FF"), // Xanh nhạt
            Color.parseColor("#5F27CD"), // Tím
            Color.parseColor("#00D2D3"), // Xanh cyan
            Color.parseColor("#FF9F43")  // Cam đậm
        };
        
        // Áp dụng màu khác nhau cho từng cột
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#333333"));
        dataSet.setDrawValues(true);
        
        // Custom formatter cho giá trị trên cột (đổi M thành "triệu")
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format("%.1f triệu", value / 1000000);
                } else if (value >= 1000) {
                    return String.format("%.0fK", value / 1000);
                } else {
                    return String.format("%.0f", value);
                }
            }
        });
        
        // Set data to chart
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);
        revenueChart.setData(barData);
        
        // Set labels for X-axis với format "dd/MM"
        revenueChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        
        // Set số lượng labels hiển thị
        revenueChart.getXAxis().setLabelCount(Math.min(labels.size(), 10), false);
        
        revenueChart.invalidate(); // Refresh chart
    }

    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
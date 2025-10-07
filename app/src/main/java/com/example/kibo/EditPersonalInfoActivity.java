package com.example.kibo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.District;
import com.example.kibo.models.Province;
import com.example.kibo.models.UpdateUserRequest;
import com.example.kibo.models.User;
import com.example.kibo.models.Ward;
import com.example.kibo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPersonalInfoActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    private ApiService apiService;
    
    // Views
    private ImageView buttonBack;
    private EditText editTextName;
    private TextView textViewEmail;
    private EditText editTextPhone;
    private Spinner spinnerProvince;
    private Spinner spinnerDistrict;
    private Spinner spinnerWard;
    private EditText editTextAddress;
    private Button buttonUpdate;
    
    // Data lists
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    
    // Adapters
    private ArrayAdapter<Province> provinceAdapter;
    private ArrayAdapter<District> districtAdapter;
    private ArrayAdapter<Ward> wardAdapter;
    
    // Selected IDs
    private int selectedProvinceId = 0;
    private int selectedDistrictId = 0;
    private int selectedWardId = 0;
    
    // User data
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);
        
        // Initialize
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();
        
        // Get current user
        currentUser = sessionManager.getUser();
        
        // Initialize views
        initViews();
        
        // Load provinces
        loadProvinces();
        
        // Auto fill current data
        autoFillUserData();
        
        // Setup listeners
        setupListeners();
    }
    
    private void initViews() {
        buttonBack = findViewById(R.id.button_back);
        editTextName = findViewById(R.id.edit_text_name);
        textViewEmail = findViewById(R.id.text_view_email);
        editTextPhone = findViewById(R.id.edit_text_phone);
        spinnerProvince = findViewById(R.id.spinner_province);
        spinnerDistrict = findViewById(R.id.spinner_district);
        spinnerWard = findViewById(R.id.spinner_ward);
        editTextAddress = findViewById(R.id.edit_text_address);
        buttonUpdate = findViewById(R.id.button_update);
    }
    
    private void autoFillUserData() {
        if (currentUser != null) {
            // Fill basic info
            editTextName.setText(currentUser.getUsername());
            textViewEmail.setText(currentUser.getEmail());
            editTextPhone.setText(currentUser.getPhonenumber());
            editTextAddress.setText(currentUser.getAddress());
            
            // Save selected IDs for later comparison
            selectedProvinceId = currentUser.getProvinceid();
            selectedDistrictId = currentUser.getDistrictid();
            selectedWardId = currentUser.getWardid();
        }
    }
    
    private void setupListeners() {
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip placeholder
                    Province selectedProvince = provinceList.get(position - 1);
                    selectedProvinceId = selectedProvince.getProvinceID();
                    loadDistricts(selectedProvinceId);
                } else {
                    selectedProvinceId = 0;
                    clearDistricts();
                    clearWards();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip placeholder
                    District selectedDistrict = districtList.get(position - 1);
                    selectedDistrictId = selectedDistrict.getDistrictID();
                    loadWards(selectedDistrictId);
                } else {
                    selectedDistrictId = 0;
                    clearWards();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip placeholder
                    Ward selectedWard = wardList.get(position - 1);
                    selectedWardId = selectedWard.getWardID();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });
    }
    
    private void loadProvinces() {
        Call<List<Province>> call = apiService.getProvinces();
        call.enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList = response.body();
                    setupProvinceSpinner();
                } else {
                    Toast.makeText(EditPersonalInfoActivity.this, "Không thể tải danh sách tỉnh/thành phố", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Toast.makeText(EditPersonalInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupProvinceSpinner() {
        List<String> provinceNames = new ArrayList<>();
        provinceNames.add("-- Chọn Tỉnh/Thành phố --");
        for (Province province : provinceList) {
            provinceNames.add(province.getProvinceName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapter);
        
        // Select current province if exists
        if (selectedProvinceId > 0) {
            for (int i = 0; i < provinceList.size(); i++) {
                if (provinceList.get(i).getProvinceID() == selectedProvinceId) {
                    spinnerProvince.setSelection(i + 1);
                    break;
                }
            }
        }
    }
    
    private void loadDistricts(int provinceId) {
        Call<List<District>> call = apiService.getDistricts(provinceId);
        call.enqueue(new Callback<List<District>>() {
            @Override
            public void onResponse(Call<List<District>> call, Response<List<District>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districtList = response.body();
                    setupDistrictSpinner();
                } else {
                    Toast.makeText(EditPersonalInfoActivity.this, "Không thể tải danh sách quận/huyện", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<District>> call, Throwable t) {
                Toast.makeText(EditPersonalInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupDistrictSpinner() {
        List<String> districtNames = new ArrayList<>();
        districtNames.add("-- Chọn Quận/Huyện --");
        for (District district : districtList) {
            districtNames.add(district.getDistrictName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districtNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);
        spinnerDistrict.setEnabled(true);
        
        // Select current district if exists
        if (selectedDistrictId > 0) {
            for (int i = 0; i < districtList.size(); i++) {
                if (districtList.get(i).getDistrictID() == selectedDistrictId) {
                    spinnerDistrict.setSelection(i + 1);
                    break;
                }
            }
        }
    }
    
    private void loadWards(int districtId) {
        Call<List<Ward>> call = apiService.getWards(districtId);
        call.enqueue(new Callback<List<Ward>>() {
            @Override
            public void onResponse(Call<List<Ward>> call, Response<List<Ward>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wardList = response.body();
                    setupWardSpinner();
                } else {
                    Toast.makeText(EditPersonalInfoActivity.this, "Không thể tải danh sách phường/xã", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Ward>> call, Throwable t) {
                Toast.makeText(EditPersonalInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setupWardSpinner() {
        List<String> wardNames = new ArrayList<>();
        wardNames.add("-- Chọn Phường/Xã --");
        for (Ward ward : wardList) {
            wardNames.add(ward.getWardName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wardNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWard.setAdapter(adapter);
        spinnerWard.setEnabled(true);
        
        // Select current ward if exists
        if (selectedWardId > 0) {
            for (int i = 0; i < wardList.size(); i++) {
                if (wardList.get(i).getWardID() == selectedWardId) {
                    spinnerWard.setSelection(i + 1);
                    break;
                }
            }
        }
    }
    
    private void clearDistricts() {
        districtList.clear();
        spinnerDistrict.setAdapter(null);
        spinnerDistrict.setEnabled(false);
        selectedDistrictId = 0;
    }
    
    private void clearWards() {
        wardList.clear();
        spinnerWard.setAdapter(null);
        spinnerWard.setEnabled(false);
        selectedWardId = 0;
    }
    
    private void updateUserInfo() {
        // Validate
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        
        if (name.isEmpty()) {
            editTextName.setError("Vui lòng nhập họ và tên");
            editTextName.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            editTextPhone.setError("Vui lòng nhập số điện thoại");
            editTextPhone.requestFocus();
            return;
        }
        
        // Disable button
        buttonUpdate.setEnabled(false);
        buttonUpdate.setText("Đang cập nhật...");
        
        // Create update request
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setUserid(currentUser.getUserid());
        updateRequest.setUsername(name);
        updateRequest.setPassword(""); // Don't update password
        updateRequest.setEmail(currentUser.getEmail());
        updateRequest.setRole(currentUser.getRole());
        updateRequest.setPhonenumber(phone);
        updateRequest.setAddress(address);
        updateRequest.setProvinceid(selectedProvinceId);
        updateRequest.setDistrictid(selectedDistrictId);
        updateRequest.setWardid(selectedWardId);
        
        // Call API
        Call<User> call = apiService.updateUser(currentUser.getUserid(), updateRequest);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                buttonUpdate.setEnabled(true);
                buttonUpdate.setText("Cập nhật thông tin");
                
                if (response.isSuccessful()) {
                    // HTTP 204 No Content hoặc 200 OK đều là thành công
                    if (response.body() != null) {
                        // Response có body (200 OK)
                        User updatedUser = response.body();
                        
                        // Update SessionManager với data từ API
                        sessionManager.updateUserName(updatedUser.getUsername());
                        sessionManager.updateUserPhone(updatedUser.getPhonenumber());
                        sessionManager.updateUserAddress(
                            updatedUser.getAddress(),
                            updatedUser.getProvinceid(),
                            updatedUser.getDistrictid(),
                            updatedUser.getWardid()
                        );
                    } else {
                        // Response không có body (204 No Content)
                        // Update SessionManager với data từ form
                        sessionManager.updateUserName(name);
                        sessionManager.updateUserPhone(phone);
                        sessionManager.updateUserAddress(
                            address,
                            selectedProvinceId,
                            selectedDistrictId,
                            selectedWardId
                        );
                    }
                    
                    Toast.makeText(EditPersonalInfoActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditPersonalInfoActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                buttonUpdate.setEnabled(true);
                buttonUpdate.setText("Cập nhật thông tin");
                Toast.makeText(EditPersonalInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


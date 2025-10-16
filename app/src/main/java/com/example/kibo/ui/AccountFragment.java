package com.example.kibo.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kibo.AdminChatListActivity;
import com.example.kibo.ChatActivity;
import com.example.kibo.LoginActivity;
import com.example.kibo.MainActivity;
import com.example.kibo.PersonalInfoActivity;
import com.example.kibo.R;
import com.example.kibo.models.User;
import com.example.kibo.utils.SessionManager;

public class AccountFragment extends Fragment {
    
    private SessionManager sessionManager;
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private TextView textViewUserRole;
    private LinearLayout layoutPersonalInfo;
    private LinearLayout layoutChatSupport;
    private LinearLayout layoutAdminChatManagement;
    private LinearLayout layoutLogout;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        
        // Initialize SessionManager
        sessionManager = new SessionManager(requireContext());
        
        // Initialize views
        initViews(view);
        
        // Load user data
        loadUserData();
        
        // Setup click listeners
        setupClickListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        textViewUserName = view.findViewById(R.id.text_view_user_name);
        textViewUserEmail = view.findViewById(R.id.text_view_user_email);
        textViewUserRole = view.findViewById(R.id.text_view_user_role);
        layoutPersonalInfo = view.findViewById(R.id.layout_personal_info);
        layoutChatSupport = view.findViewById(R.id.layout_chat_support);
        layoutAdminChatManagement = view.findViewById(R.id.layout_admin_chat_management);
        layoutLogout = view.findViewById(R.id.layout_logout);
    }
    
    private void loadUserData() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }
        
        // Get user data from SessionManager
        User user = sessionManager.getUser();
        
        if (user != null) {
            // Display user name
            String userName = user.getUsername();
            if (userName != null && !userName.isEmpty()) {
                textViewUserName.setText(userName);
            } else {
                textViewUserName.setText("Người dùng");
            }
            
            // Display user email
            String userEmail = user.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                textViewUserEmail.setText(userEmail);
            } else {
                textViewUserEmail.setText("");
            }
            
            // Display user role
            String roleName = user.getRoleName();
            if (roleName != null && !roleName.isEmpty()) {
                textViewUserRole.setText(roleName);
            } else {
                textViewUserRole.setText("Khách hàng");
            }
            
            // Show/hide admin chat management based on role
            // Check both role number and role name for admin
            boolean isAdmin = (user.getRole() == 0) || 
                             (roleName != null && roleName.toLowerCase().contains("admin"));
            
            System.out.println("=== ADMIN DETECTION DEBUG ===");
            System.out.println("User Role: " + user.getRole());
            System.out.println("Role Name: " + roleName);
            System.out.println("Is Admin: " + isAdmin);
            System.out.println("Layout Admin Chat Management: " + (layoutAdminChatManagement != null ? "NOT NULL" : "NULL"));
            
            if (isAdmin) {
                if (layoutAdminChatManagement != null) {
                    layoutAdminChatManagement.setVisibility(View.VISIBLE);
                    System.out.println("Admin detected - showing chat management option");
                } else {
                    System.out.println("ERROR: layoutAdminChatManagement is NULL!");
                }
            } else {
                if (layoutAdminChatManagement != null) {
                    layoutAdminChatManagement.setVisibility(View.GONE);
                    System.out.println("Not admin - hiding chat management option");
                } else {
                    System.out.println("ERROR: layoutAdminChatManagement is NULL!");
                }
            }
        }
    }
    
    private void setupClickListeners() {
        // Personal Info click listener
        layoutPersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPersonalInfo();
            }
        });
        
        // Chat Support click listener
        layoutChatSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatSupport();
            }
        });
        
        // Admin Chat Management click listener
        layoutAdminChatManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAdminChatManagement();
            }
        });
        
        // Logout click listener
        layoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call MainActivity's performLogout method
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).performLogout();
                }
            }
        });
    }
    
    private void openPersonalInfo() {
        Intent intent = new Intent(requireContext(), PersonalInfoActivity.class);
        startActivity(intent);
    }
    
    private void openChatSupport() {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        startActivity(intent);
    }
    
    private void openAdminChatManagement() {
        Intent intent = new Intent(requireContext(), AdminChatListActivity.class);
        startActivity(intent);
    }
    
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload user data when fragment resumes (in case data was updated)
        loadUserData();
    }
}

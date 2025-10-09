package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userid")
    private int userid;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phonenumber")
    private String phonenumber;
    
    @SerializedName("role")
    private int role;
    
    @SerializedName("roleName")
    private String roleName;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("provinceid")
    private int provinceid;
    
    @SerializedName("districtid")
    private int districtid;
    
    @SerializedName("wardid")
    private int wardid;
    
    // Additional fields for token (used in OTP verification)
    private String token;
    private String refreshToken;

    public User() {}

    public User(int userid, String username, String email, String phonenumber, int role, String roleName) {
        this.userid = userid;
        this.username = username;
        this.email = email;
        this.phonenumber = phonenumber;
        this.role = role;
        this.roleName = roleName;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getProvinceid() {
        return provinceid;
    }

    public void setProvinceid(int provinceid) {
        this.provinceid = provinceid;
    }

    public int getDistrictid() {
        return districtid;
    }

    public void setDistrictid(int districtid) {
        this.districtid = districtid;
    }

    public int getWardid() {
        return wardid;
    }

    public void setWardid(int wardid) {
        this.wardid = wardid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

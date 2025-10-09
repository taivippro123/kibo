package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {
    @SerializedName("userid")
    private int userid;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("role")
    private int role;
    
    @SerializedName("phonenumber")
    private String phonenumber;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("provinceid")
    private int provinceid;
    
    @SerializedName("districtid")
    private int districtid;
    
    @SerializedName("wardid")
    private int wardid;

    public UpdateUserRequest() {}

    public UpdateUserRequest(int userid, String username, String password, String email, 
                            int role, String phonenumber, String address, 
                            int provinceid, int districtid, int wardid) {
        this.userid = userid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.phonenumber = phonenumber;
        this.address = address;
        this.provinceid = provinceid;
        this.districtid = districtid;
        this.wardid = wardid;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
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
}


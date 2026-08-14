package com.dawsons.laundry.dto;

public class LoginResponse {

    private String token;
    private Integer userId;
    private String fullName;
    private String username;
    private String role;

    public LoginResponse(String token, Integer userId, String fullName, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public Integer getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}

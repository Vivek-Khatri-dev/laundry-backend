package com.dawsons.laundry.dto;

import com.dawsons.laundry.entity.User;

public class UserResponse {
    private Integer id;
    private String fullName;
    private String username;
    private String role;
    private boolean active;

    public UserResponse(User u) {
        this.id = u.getId();
        this.fullName = u.getFullName();
        this.username = u.getUsername();
        this.role = u.getRole().getName();
        this.active = u.isActive();
    }

    public Integer getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}

package com.dawsons.laundry.dto;

import com.dawsons.laundry.entity.Customer;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerResponse {
    private Integer id;
    private String name;
    private String phone;
    private String email;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Karachi")
    private LocalDateTime lastOrderDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Karachi")
    private LocalDateTime createdAt;
    
    private String notes;
    private String sapCardCode;

    public CustomerResponse(Customer customer) {
        this.id = customer.getId();
        this.name = customer.getName();
        this.phone = customer.getPhone();
        this.email = customer.getEmail();
        this.totalOrders = customer.getTotalOrders();
        this.totalSpent = customer.getTotalSpent();
        this.lastOrderDate = customer.getLastOrderDate();
        this.createdAt = customer.getCreatedAt();
        this.notes = customer.getNotes();
        this.sapCardCode = customer.getSapCardCode();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSapCardCode() { return sapCardCode; }
    public void setSapCardCode(String sapCardCode) { this.sapCardCode = sapCardCode; }
}
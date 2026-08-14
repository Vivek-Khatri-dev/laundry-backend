package com.dawsons.laundry.dto;

import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.BillItem;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BillResponse {
    private Integer receiptNo;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Karachi")
    private LocalDate createDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Karachi")
    private LocalDate deliveryDate;
    
    private BigDecimal totalAmount;
    private String status;
    private String createdBy;
    private String updatedBy;
    private String voidReason;
    private List<BillItemResponse> items;
    private Integer customerId;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

    public BillResponse(Bill bill) {
        this.receiptNo = bill.getReceiptNo();
        this.customerName = bill.getCustomerName();
        this.customerPhone = bill.getCustomerPhone();
        this.customerEmail = bill.getCustomerEmail();
        this.createDate = bill.getCreateDate();
        this.deliveryDate = bill.getDeliveryDate();
        this.totalAmount = bill.getTotalAmount();
        this.status = bill.getStatus().name();
        this.createdBy = bill.getCreatedBy() != null ? bill.getCreatedBy().getFullName() : null;
        this.updatedBy = bill.getUpdatedBy() != null ? bill.getUpdatedBy().getFullName() : null;
        this.voidReason = bill.getVoidReason();
        this.items = bill.getItems().stream().map(BillItemResponse::new).collect(Collectors.toList());
        this.customerId = bill.getCustomer() != null ? bill.getCustomer().getId() : null;
        this.discountType = bill.getDiscountType();
        this.discountValue = bill.getDiscountValue();
        this.discountAmount = bill.getDiscountAmount();
        this.finalAmount = bill.getFinalAmount();
    }

    // Getters and Setters
    public Integer getReceiptNo() { return receiptNo; }
    public void setReceiptNo(Integer receiptNo) { this.receiptNo = receiptNo; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public LocalDate getCreateDate() { return createDate; }
    public void setCreateDate(LocalDate createDate) { this.createDate = createDate; }

    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getVoidReason() { return voidReason; }
    public void setVoidReason(String voidReason) { this.voidReason = voidReason; }

    public List<BillItemResponse> getItems() { return items; }
    public void setItems(List<BillItemResponse> items) { this.items = items; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
}
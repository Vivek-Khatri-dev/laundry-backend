package com.dawsons.laundry.dto;

import com.dawsons.laundry.entity.BillItem;
import java.math.BigDecimal;

public class BillItemResponse {
    private String name;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private boolean isCustom;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;

    public BillItemResponse(BillItem item) {
        this.name = item.getName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
        this.subtotal = item.getSubtotal();
        this.isCustom = item.isCustom();
        this.discountType = item.getDiscountType();
        this.discountValue = item.getDiscountValue();
        this.discountAmount = item.getDiscountAmount();
        this.finalPrice = item.getFinalPrice();
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getSubtotal() { return subtotal; }
    public boolean isCustom() { return isCustom; }
    public String getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getFinalPrice() { return finalPrice; }
}
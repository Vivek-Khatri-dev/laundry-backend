package com.dawsons.laundry.dto;

import com.dawsons.laundry.entity.Product;
import java.math.BigDecimal;

public class ProductResponse {
    private Integer id;
    private String name;
    private BigDecimal price;
    private boolean active;

    public ProductResponse(Product p) {
        this.id = p.getId();
        this.name = p.getName();
        this.price = p.getPrice();
        this.active = p.isActive();
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
}

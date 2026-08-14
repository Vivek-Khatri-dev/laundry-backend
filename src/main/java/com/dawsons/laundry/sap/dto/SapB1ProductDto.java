// SapB1ProductDto.java
package com.dawsons.laundry.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class SapB1ProductDto {
    @JsonProperty("ItemCode")
    private String itemCode;
    
    @JsonProperty("ItemName")
    private String itemName;
    
    @JsonProperty("Price")
    private BigDecimal price;
    
    @JsonProperty("ItemType")
    private String itemType = "I"; // I = Inventory item
    
    @JsonProperty("Active")
    private String active = "Y";
    
    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    
    public String getActive() { return active; }
    public void setActive(String active) { this.active = active; }
}
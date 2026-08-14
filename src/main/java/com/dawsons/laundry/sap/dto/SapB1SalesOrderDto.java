// SapB1SalesOrderDto.java
package com.dawsons.laundry.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SapB1SalesOrderDto {
    @JsonProperty("CardCode")
    private String cardCode;
    
    @JsonProperty("CardName")
    private String cardName;
    
    @JsonProperty("DocDate")
    private String docDate;
    
    @JsonProperty("TaxDate")
    private String taxDate;
    
    @JsonProperty("DocDueDate")
    private String docDueDate;
    
    @JsonProperty("Comments")
    private String comments;
    
    @JsonProperty("DocumentLines")
    private List<DocumentLine> documentLines;
    
    public static class DocumentLine {
        @JsonProperty("ItemCode")
        private String itemCode;
        
        @JsonProperty("Quantity")
        private Double quantity;
        
        @JsonProperty("Price")
        private BigDecimal price;
        
        @JsonProperty("LineTotal")
        private BigDecimal lineTotal;
        
        @JsonProperty("Currency")
        private String currency = "PKR";
        
        // Getters and Setters
        public String getItemCode() { return itemCode; }
        public void setItemCode(String itemCode) { this.itemCode = itemCode; }
        
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    // Getters and Setters
    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }
    
    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }
    
    public String getDocDate() { return docDate; }
    public void setDocDate(LocalDate date) { 
        this.docDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    public String getTaxDate() { return taxDate; }
    public void setTaxDate(LocalDate date) { 
        this.taxDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    public String getDocDueDate() { return docDueDate; }
    public void setDocDueDate(LocalDate date) { 
        this.docDueDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public List<DocumentLine> getDocumentLines() { return documentLines; }
    public void setDocumentLines(List<DocumentLine> documentLines) { this.documentLines = documentLines; }
}
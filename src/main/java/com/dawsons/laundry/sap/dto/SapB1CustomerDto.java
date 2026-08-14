// SapB1CustomerDto.java
package com.dawsons.laundry.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SapB1CustomerDto {
    @JsonProperty("CardCode")
    private String cardCode;
    
    @JsonProperty("CardName")
    private String cardName;
    
    @JsonProperty("Phone1")
    private String phone1;
    
    @JsonProperty("E_Mail")
    private String email;
    
    @JsonProperty("CardType")
    private String cardType = "C"; // C = Customer
    
    @JsonProperty("Currency")
    private String currency = "PKR";
    
    // Getters and Setters
    public String getCardCode() { return cardCode; }
    public void setCardCode(String cardCode) { this.cardCode = cardCode; }
    
    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }
    
    public String getPhone1() { return phone1; }
    public void setPhone1(String phone1) { this.phone1 = phone1; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
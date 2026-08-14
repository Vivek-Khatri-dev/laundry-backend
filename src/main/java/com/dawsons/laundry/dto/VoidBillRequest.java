package com.dawsons.laundry.dto;

import jakarta.validation.constraints.NotBlank;

public class VoidBillRequest {

    @NotBlank(message = "A reason is required to void/return a bill")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

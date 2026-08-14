package com.dawsons.laundry.entity;

public enum BillStatus {
    PENDING,      // Order received - waiting to be processed
    PROCESSING,   // Being worked on in the laundry
    READY,        // Ready for customer collection
    PAID,         // Paid and collected by customer
    VOIDED,       // Cancelled/voided
    RETURNED      // Returned by customer
}
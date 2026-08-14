// SapB1SyncService.java
package com.dawsons.laundry.sap;

import com.dawsons.laundry.entity.*;
import com.dawsons.laundry.sap.dto.SapB1CustomerDto;
import com.dawsons.laundry.sap.dto.SapB1ProductDto;
import com.dawsons.laundry.sap.dto.SapB1SalesOrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SapB1SyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(SapB1SyncService.class);
    
    private final SapB1Client sapB1Client;
    
    public SapB1SyncService(SapB1Client sapB1Client) {
        this.sapB1Client = sapB1Client;
    }
    
    /**
     * Sync a customer to SAP B1 as a Business Partner
     * Returns the SAP B1 CardCode
     */
    public String syncCustomerToSap(Customer customer) {
        if (!sapB1Client.isEnabled()) {
            logger.info("SAP B1 integration disabled, skipping customer sync for: {}", customer.getName());
            return null;
        }
        
        try {
            logger.info("Syncing customer {} to SAP B1", customer.getName());
            
            // First check if customer already exists in B1
            String cardCode = findCustomerInSap(customer.getPhone(), customer.getEmail());
            
            if (cardCode != null) {
                logger.info("Customer already exists in SAP B1 with CardCode: {}", cardCode);
                return cardCode;
            }
            
            // Create new Business Partner
            SapB1CustomerDto dto = new SapB1CustomerDto();
            dto.setCardName(customer.getName());
            dto.setPhone1(customer.getPhone());
            dto.setEmail(customer.getEmail());
            
            Map<String, Object> response = sapB1Client.post("/BusinessPartners", dto);
            
            if (response != null && response.containsKey("CardCode")) {
                String newCardCode = response.get("CardCode").toString();
                logger.info("Successfully created Business Partner in SAP B1 with CardCode: {}", newCardCode);
                return newCardCode;
            } else {
                logger.error("Failed to create Business Partner in SAP B1: {}", response);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error syncing customer to SAP B1: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Sync a product to SAP B1 as an Item
     * Returns the SAP B1 ItemCode
     */
    public String syncProductToSap(Product product) {
        if (!sapB1Client.isEnabled()) {
            logger.info("SAP B1 integration disabled, skipping product sync for: {}", product.getName());
            return null;
        }
        
        try {
            logger.info("Syncing product {} to SAP B1", product.getName());
            
            // Check if product already exists in B1
            String itemCode = findProductInSap(product.getName());
            
            if (itemCode != null) {
                // Update existing product
                logger.info("Product already exists in SAP B1 with ItemCode: {}", itemCode);
                updateProductInSap(product, itemCode);
                return itemCode;
            }
            
            // Create new product
            SapB1ProductDto dto = new SapB1ProductDto();
            dto.setItemCode(String.valueOf(product.getId())); // Use your internal ID as ItemCode
            dto.setItemName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setActive(product.isActive() ? "Y" : "N");
            
            Map<String, Object> response = sapB1Client.post("/Items", dto);
            
            if (response != null && response.containsKey("ItemCode")) {
                String newItemCode = response.get("ItemCode").toString();
                logger.info("Successfully created Item in SAP B1 with ItemCode: {}", newItemCode);
                return newItemCode;
            } else {
                logger.error("Failed to create Item in SAP B1: {}", response);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error syncing product to SAP B1: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Sync a bill to SAP B1 as a Sales Order
     */
    public void syncBillToSap(Bill bill) {
        if (!sapB1Client.isEnabled()) {
            logger.info("SAP B1 integration disabled, skipping bill sync for receipt #{}", bill.getReceiptNo());
            return;
        }
        
        try {
            logger.info("Syncing bill receipt #{} to SAP B1", bill.getReceiptNo());
            
            // Get the customer's CardCode
            String cardCode = null;
            if (bill.getCustomer() != null) {
                cardCode = syncCustomerToSap(bill.getCustomer());
            }
            
            if (cardCode == null) {
                // Try to find by phone/email
                cardCode = findCustomerInSap(bill.getCustomerPhone(), bill.getCustomerEmail());
            }
            
            if (cardCode == null) {
                logger.warn("Cannot sync bill #{} - no customer found in SAP B1", bill.getReceiptNo());
                return;
            }
            
            // Create Sales Order
            SapB1SalesOrderDto dto = new SapB1SalesOrderDto();
            dto.setCardCode(cardCode);
            dto.setCardName(bill.getCustomerName());
            dto.setDocDate(bill.getCreateDate());
            dto.setTaxDate(bill.getCreateDate());
            dto.setDocDueDate(bill.getDeliveryDate());
            dto.setComments("Laundry Bill #" + bill.getReceiptNo() + " - " + bill.getCustomerName());
            
            // Build line items
            List<SapB1SalesOrderDto.DocumentLine> lines = new ArrayList<>();
            for (BillItem item : bill.getItems()) {
                String itemCode = syncProductToSapIfNeeded(item);
                
                SapB1SalesOrderDto.DocumentLine line = new SapB1SalesOrderDto.DocumentLine();
                line.setItemCode(itemCode != null ? itemCode : "SERVICE_" + System.currentTimeMillis());
                line.setQuantity(Double.valueOf(item.getQuantity()));
                line.setPrice(item.getPrice());
                line.setLineTotal(item.getFinalPrice());
                lines.add(line);
            }
            
            dto.setDocumentLines(lines);
            
            Map<String, Object> response = sapB1Client.post("/Orders", dto);
            
            if (response != null && response.containsKey("DocEntry")) {
                String docEntry = response.get("DocEntry").toString();
                logger.info("Successfully created Sales Order in SAP B1 with DocEntry: {}", docEntry);
            } else {
                logger.error("Failed to create Sales Order in SAP B1: {}", response);
            }
            
        } catch (Exception e) {
            logger.error("Error syncing bill to SAP B1: {}", e.getMessage(), e);
        }
    }
    
    // Helper methods to find existing records in SAP B1
    
    private String findCustomerInSap(String phone, String email) {
        try {
            // Try to find by phone first
            String query = phone != null ? "$filter=Phone1 eq '" + phone + "'" : "";
            if (email != null && !email.isEmpty()) {
                query = "$filter=E_Mail eq '" + email + "'";
            }
            
            Map<String, Object> response = sapB1Client.get("/BusinessPartners?" + query);
            
            if (response != null && response.containsKey("value")) {
                List<Map<String, Object>> values = (List<Map<String, Object>>) response.get("value");
                if (values != null && !values.isEmpty()) {
                    return values.get(0).get("CardCode").toString();
                }
            }
            return null;
        } catch (Exception e) {
            logger.warn("Error finding customer in SAP B1: {}", e.getMessage());
            return null;
        }
    }
    
    private String findProductInSap(String productName) {
        try {
            Map<String, Object> response = sapB1Client.get("/Items?$filter=ItemName eq '" + productName + "'");
            
            if (response != null && response.containsKey("value")) {
                List<Map<String, Object>> values = (List<Map<String, Object>>) response.get("value");
                if (values != null && !values.isEmpty()) {
                    return values.get(0).get("ItemCode").toString();
                }
            }
            return null;
        } catch (Exception e) {
            logger.warn("Error finding product in SAP B1: {}", e.getMessage());
            return null;
        }
    }
    
    private void updateProductInSap(Product product, String itemCode) {
        try {
            SapB1ProductDto dto = new SapB1ProductDto();
            dto.setItemName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setActive(product.isActive() ? "Y" : "N");
            
            sapB1Client.patch("/Items('" + itemCode + "')", dto);
            logger.info("Updated product in SAP B1: {}", product.getName());
        } catch (Exception e) {
            logger.error("Error updating product in SAP B1: {}", e.getMessage());
        }
    }
    
    private String syncProductToSapIfNeeded(BillItem item) {
        if (item.isCustom()) {
            // Create a temporary service item in SAP B1
            try {
                SapB1ProductDto dto = new SapB1ProductDto();
                dto.setItemCode("CUSTOM_" + System.currentTimeMillis());
                dto.setItemName(item.getName());
                dto.setPrice(item.getPrice());
                
                Map<String, Object> response = sapB1Client.post("/Items", dto);
                if (response != null && response.containsKey("ItemCode")) {
                    return response.get("ItemCode").toString();
                }
            } catch (Exception e) {
                logger.error("Error creating custom item in SAP B1: {}", e.getMessage());
            }
            return null;
        } else {
            // Product should already be synced
            return String.valueOf(item.getId());
        }
    }
}
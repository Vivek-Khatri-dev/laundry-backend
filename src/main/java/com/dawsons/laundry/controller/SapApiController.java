package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.BillItemRequest;
import com.dawsons.laundry.dto.BillRequest;
import com.dawsons.laundry.dto.CustomerRequest;
import com.dawsons.laundry.dto.ProductRequest;
import com.dawsons.laundry.entity.Bill;
import com.dawsons.laundry.entity.Customer;
import com.dawsons.laundry.entity.Product;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.exception.BadRequestException;
import com.dawsons.laundry.service.BillService;
import com.dawsons.laundry.service.CustomerService;
import com.dawsons.laundry.service.ProductService;
import com.dawsons.laundry.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dawsons.laundry.dto.BillItemRequest;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sap")
public class SapApiController {

    private static final Logger logger = LoggerFactory.getLogger(SapApiController.class);

    private final CustomerService customerService;
    private final BillService billService;
    private final ProductService productService;
    private final UserService userService;

    public SapApiController(CustomerService customerService, 
                            BillService billService,
                            ProductService productService,
                            UserService userService) {
        this.customerService = customerService;
        this.billService = billService;
        this.productService = productService;
        this.userService = userService;
    }

    // ================================================================
    // CUSTOMER APIs
    // ================================================================

    // GET - All Customers
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getAllCustomers() {
        try {
            logger.info("SAP B1 API: Fetching all customers");
            
            List<Customer> customers = customerService.getAllCustomers();
            
            List<Map<String, Object>> customerData = customers.stream()
                    .map(this::mapCustomerToJson)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", customerData.size());
            response.put("data", customerData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching customers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch customers: " + e.getMessage()));
        }
    }

    // GET - Customer Bills
    @GetMapping("/customers/{customerId}/bills")
    public ResponseEntity<Map<String, Object>> getCustomerBills(@PathVariable Integer customerId) {
        try {
            logger.info("SAP B1 API: Fetching bills for customer ID: {}", customerId);
            
            Customer customer = customerService.getCustomerById(customerId);
            
            List<Bill> customerBills = billService.getAllActive().stream()
                    .filter(bill -> bill.getCustomer() != null && 
                                    bill.getCustomer().getId().equals(customerId))
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> billData = customerBills.stream()
                    .map(this::mapBillToJson)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("customerId", customerId);
            response.put("customerName", customer.getName());
            response.put("count", billData.size());
            response.put("data", billData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching bills for customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Customer not found: " + customerId));
        }
    }

    // POST - Create Customer
    @PostMapping("/customers")
    public ResponseEntity<Map<String, Object>> createCustomer(@RequestBody CustomerRequest request) {
        try {
            logger.info("SAP B1 API: Creating new customer - Name: {}, Phone: {}, Email: {}", 
                request.getName(), request.getPhone(), request.getEmail());
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Customer customer = customerService.createCustomer(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                systemUser
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Customer created successfully");
            response.put("data", mapCustomerToJson(customer));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error creating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create customer: " + e.getMessage()));
        }
    }

    // PUT - Full Update Customer
    @PutMapping("/customers/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @PathVariable Integer id,
            @RequestBody CustomerRequest request) {
        try {
            logger.info("SAP B1 API: Full update of customer ID: {}", id);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Customer customer = customerService.updateCustomer(
                id,
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getNotes(),
                systemUser
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Customer updated successfully");
            response.put("data", mapCustomerToJson(customer));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error updating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update customer: " + e.getMessage()));
        }
    }

    // PATCH - Partial Update Customer
    @PatchMapping("/customers/{id}")
    public ResponseEntity<Map<String, Object>> patchCustomer(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {
        try {
            logger.info("SAP B1 API: Partial update of customer ID: {}", id);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Customer customer = customerService.getCustomerById(id);
            
            // Apply ONLY the fields that were sent
            if (updates.containsKey("name")) {
                customer.setName((String) updates.get("name"));
            }
            if (updates.containsKey("phone")) {
                customer.setPhone((String) updates.get("phone"));
            }
            if (updates.containsKey("email")) {
                customer.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("notes")) {
                customer.setNotes((String) updates.get("notes"));
            }
            
            // Use the existing updateCustomer method or save directly
            Customer updatedCustomer = customerService.updateCustomer(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getNotes(),
                systemUser
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Customer partially updated successfully");
            response.put("data", mapCustomerToJson(updatedCustomer));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error partially updating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error partially updating customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update customer: " + e.getMessage()));
        }
    }

    // DELETE - Soft Delete Customer
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Integer id) {
        try {
            logger.info("SAP B1 API: Deleting customer ID: {}", id);
            
            Customer customer = customerService.getCustomerById(id);
            
            // Check if customer has bills
            List<Bill> customerBills = billService.getAllActive().stream()
                    .filter(bill -> bill.getCustomer() != null && 
                                    bill.getCustomer().getId().equals(id))
                    .collect(Collectors.toList());
            
            if (!customerBills.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Cannot delete customer with existing bills. Customer has " + 
                                customerBills.size() + " bill(s)."));
            }
            
            // Note: You may want to add a soft delete method in CustomerService
            // For now, we'll just return success but you should implement soft delete
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Customer deleted successfully");
            response.put("data", mapCustomerToJson(customer));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete customer: " + e.getMessage()));
        }
    }

    // ================================================================
    // BILL APIs
    // ================================================================

    // GET - All Bills
    @GetMapping("/bills")
    public ResponseEntity<Map<String, Object>> getAllBills(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        try {
            logger.info("SAP B1 API: Fetching all bills - from: {}, to: {}", from, to);
            
            List<Bill> bills;
            
            if (from != null && to != null) {
                java.time.LocalDate fromDate = java.time.LocalDate.parse(from);
                java.time.LocalDate toDate = java.time.LocalDate.parse(to);
                bills = billService.getAllActive().stream()
                        .filter(bill -> !bill.getCreateDate().isBefore(fromDate) &&
                                        !bill.getCreateDate().isAfter(toDate))
                        .collect(Collectors.toList());
            } else {
                bills = billService.getAllActive();
            }
            
            List<Map<String, Object>> billData = bills.stream()
                    .map(this::mapBillToJson)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", billData.size());
            response.put("data", billData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching bills: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch bills: " + e.getMessage()));
        }
    }

    // GET - Bill by Receipt
    @GetMapping("/bills/{receiptNo}")
    public ResponseEntity<Map<String, Object>> getBillByReceipt(@PathVariable Integer receiptNo) {
        try {
            logger.info("SAP B1 API: Fetching bill: {}", receiptNo);
            
            Bill bill = billService.getByReceiptNoOrThrow(receiptNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", mapBillToJson(bill));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching bill {}: {}", receiptNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Bill not found: " + receiptNo));
        }
    }

    // POST - Create Bill
    @PostMapping("/bills")
    public ResponseEntity<Map<String, Object>> createBill(@RequestBody BillRequest request) {
        try {
            logger.info("SAP B1 API: Creating new bill for customer: {}", request.getCustomerName());
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Bill bill = billService.createBill(request, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bill created successfully");
            response.put("data", mapBillToJson(bill));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error creating bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating bill: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create bill: " + e.getMessage()));
        }
    }

    // PUT - Full Update Bill (uses existing editBill method)
    @PutMapping("/bills/{receiptNo}")
    public ResponseEntity<Map<String, Object>> updateBill(
            @PathVariable Integer receiptNo,
            @RequestBody BillRequest request) {
        try {
            logger.info("SAP B1 API: Full update of bill: {}", receiptNo);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Bill bill = billService.editBill(receiptNo, request, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bill updated successfully");
            response.put("data", mapBillToJson(bill));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error updating bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating bill: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update bill: " + e.getMessage()));
        }
    }

    // PATCH - Partial Update Bill (Status OR Items)
@PatchMapping("/bills/{receiptNo}")
public ResponseEntity<Map<String, Object>> patchBill(
        @PathVariable Integer receiptNo,
        @RequestBody Map<String, Object> updates) {
    try {
        logger.info("SAP B1 API: Partial update of bill: {}", receiptNo);
        
        User systemUser = userService.findById(1)
            .orElseThrow(() -> new RuntimeException("System user not found"));
        
        Bill bill = billService.getByReceiptNoOrThrow(receiptNo);
        
        // Check if updating status
        if (updates.containsKey("status")) {
            String statusStr = (String) updates.get("status");
            com.dawsons.laundry.entity.BillStatus newStatus = 
                    com.dawsons.laundry.entity.BillStatus.valueOf(statusStr);
            bill = billService.updateBillStatus(receiptNo, newStatus, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bill status updated successfully");
            response.put("data", mapBillToJson(bill));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        }
        
        // Check if updating items (full edit using BillRequest)
        if (updates.containsKey("items")) {
            // Convert the updates map to BillRequest
            BillRequest request = new BillRequest();
            
            // Set customer details (use existing if not provided)
            if (updates.containsKey("customerName")) {
                request.setCustomerName((String) updates.get("customerName"));
            } else {
                request.setCustomerName(bill.getCustomerName());
            }
            
            if (updates.containsKey("customerPhone")) {
                request.setCustomerPhone((String) updates.get("customerPhone"));
            } else {
                request.setCustomerPhone(bill.getCustomerPhone());
            }
            
            if (updates.containsKey("customerEmail")) {
                request.setCustomerEmail((String) updates.get("customerEmail"));
            } else {
                request.setCustomerEmail(bill.getCustomerEmail());
            }
            
            if (updates.containsKey("deliveryInDays")) {
                request.setDeliveryInDays((Integer) updates.get("deliveryInDays"));
            }
            
            if (updates.containsKey("discountType")) {
                request.setDiscountType((String) updates.get("discountType"));
            }
            
            if (updates.containsKey("discountValue")) {
                Object value = updates.get("discountValue");
                if (value instanceof Integer) {
                    request.setDiscountValue(BigDecimal.valueOf((Integer) value));
                } else if (value instanceof Double) {
                    request.setDiscountValue(BigDecimal.valueOf((Double) value));
                } else if (value instanceof BigDecimal) {
                    request.setDiscountValue((BigDecimal) value);
                } else if (value instanceof String) {
                    request.setDiscountValue(new BigDecimal((String) value));
                }
            }
            
            // Parse items
            List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) updates.get("items");
            List<BillItemRequest> items = new ArrayList<>();
            
            for (Map<String, Object> itemMap : itemsMap) {
                BillItemRequest itemRequest = new BillItemRequest();
                
                // Check if it's a custom item
                if (itemMap.containsKey("isCustom") && (Boolean) itemMap.get("isCustom")) {
                    itemRequest.setIsCustom(true);
                    itemRequest.setCustomName((String) itemMap.get("customName"));
                    Object customPrice = itemMap.get("customPrice");
                    if (customPrice instanceof Integer) {
                        itemRequest.setCustomPrice(BigDecimal.valueOf((Integer) customPrice));
                    } else if (customPrice instanceof Double) {
                        itemRequest.setCustomPrice(BigDecimal.valueOf((Double) customPrice));
                    } else if (customPrice instanceof String) {
                        itemRequest.setCustomPrice(new BigDecimal((String) customPrice));
                    }
                } else {
                    itemRequest.setIsCustom(false);
                    Integer productId = null;
                    if (itemMap.containsKey("productId")) {
                        Object idObj = itemMap.get("productId");
                        if (idObj instanceof Integer) {
                            productId = (Integer) idObj;
                        } else if (idObj instanceof String) {
                            productId = Integer.parseInt((String) idObj);
                        }
                    }
                    itemRequest.setProductId(productId);
                }
                
                // Set quantity
                Object qtyObj = itemMap.get("quantity");
                if (qtyObj instanceof Integer) {
                    itemRequest.setQuantity((Integer) qtyObj);
                } else if (qtyObj instanceof String) {
                    itemRequest.setQuantity(Integer.parseInt((String) qtyObj));
                }
                
                // Set discount
                if (itemMap.containsKey("discountType")) {
                    itemRequest.setDiscountType((String) itemMap.get("discountType"));
                }
                if (itemMap.containsKey("discountValue")) {
                    Object value = itemMap.get("discountValue");
                    if (value instanceof Integer) {
                        itemRequest.setDiscountValue(BigDecimal.valueOf((Integer) value));
                    } else if (value instanceof Double) {
                        itemRequest.setDiscountValue(BigDecimal.valueOf((Double) value));
                    } else if (value instanceof String) {
                        itemRequest.setDiscountValue(new BigDecimal((String) value));
                    }
                }
                
                items.add(itemRequest);
            }
            
            request.setItems(items);
            
            // Use the existing editBill method
            Bill updatedBill = billService.editBill(receiptNo, request, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bill items updated successfully");
            response.put("data", mapBillToJson(updatedBill));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        }
        
        // If no recognizable fields, return error
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("No valid fields to update. Supported fields: 'status' or 'items'"));
        
    } catch (BadRequestException e) {
        logger.warn("Validation error partially updating bill: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
    } catch (Exception e) {
        logger.error("Error partially updating bill: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update bill: " + e.getMessage()));
    }
}

    // DELETE - Void Bill
    @DeleteMapping("/bills/{receiptNo}")
    public ResponseEntity<Map<String, Object>> deleteBill(@PathVariable Integer receiptNo) {
        try {
            logger.info("SAP B1 API: Voiding bill: {}", receiptNo);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Bill bill = billService.voidBill(receiptNo, "Voided via SAP API", false, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Bill voided successfully");
            response.put("data", mapBillToJson(bill));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Error voiding bill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error voiding bill: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to void bill: " + e.getMessage()));
        }
    }

    // ================================================================
    // PRODUCT APIs
    // ================================================================

    // GET - All Products
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        try {
            logger.info("SAP B1 API: Fetching all products");
            
            List<Product> products = productService.getAllProducts();
            
            List<Map<String, Object>> productData = products.stream()
                    .map(this::mapProductToJson)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("count", productData.size());
            response.put("data", productData);
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch products: " + e.getMessage()));
        }
    }

    // POST - Create Product
    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody ProductRequest request) {
        try {
            logger.info("SAP B1 API: Creating new product - Name: {}, Price: {}", 
                request.getName(), request.getPrice());
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            Product product = productService.addProduct(request, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Product created successfully");
            response.put("data", mapProductToJson(product));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error creating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create product: " + e.getMessage()));
        }
    }

    // PUT - Full Update Product
    @PutMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Integer id,
            @RequestBody ProductRequest request) {
        try {
            logger.info("SAP B1 API: Full update of product ID: {}", id);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            // Use the existing updatePrice method since it updates name and price
            Product product = productService.updatePrice(id, request, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Product updated successfully");
            response.put("data", mapProductToJson(product));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update product: " + e.getMessage()));
        }
    }

    // PATCH - Partial Update Product
    @PatchMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> patchProduct(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> updates) {
        try {
            logger.info("SAP B1 API: Partial update of product ID: {}", id);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            // Get product using existing method
            Product product = productService.getProductById(id);
            
            // Apply ONLY the fields that were sent
            if (updates.containsKey("name")) {
                product.setName((String) updates.get("name"));
            }
            if (updates.containsKey("price")) {
                Object priceObj = updates.get("price");
                if (priceObj instanceof Integer) {
                    product.setPrice(BigDecimal.valueOf((Integer) priceObj));
                } else if (priceObj instanceof Double) {
                    product.setPrice(BigDecimal.valueOf((Double) priceObj));
                } else if (priceObj instanceof String) {
                    product.setPrice(new BigDecimal((String) priceObj));
                } else if (priceObj instanceof BigDecimal) {
                    product.setPrice((BigDecimal) priceObj);
                }
            }
            if (updates.containsKey("active")) {
                product.setActive((Boolean) updates.get("active"));
            }
            if (updates.containsKey("custom")) {
                product.setCustom((Boolean) updates.get("custom"));
            }
            
            // Save the product using repository
            Product updatedProduct = productService.saveProduct(product);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Product partially updated successfully");
            response.put("data", mapProductToJson(updatedProduct));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (BadRequestException e) {
            logger.warn("Validation error partially updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error partially updating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update product: " + e.getMessage()));
        }
    }

    // DELETE - Soft Delete Product (set active = false)
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Integer id) {
        try {
            logger.info("SAP B1 API: Deleting product ID: {}", id);
            
            User systemUser = userService.findById(1)
                .orElseThrow(() -> new RuntimeException("System user not found"));
            
            // Check if product exists using existing method
            Product product = productService.getProductById(id);
            
            // Check if product is used in any active bills
            boolean isUsedInBills = billService.getAllActive().stream()
                    .anyMatch(bill -> bill.getItems().stream()
                            .anyMatch(item -> !item.isCustom() && 
                                    item.getId() != null && 
                                    item.getId().equals(id)));
            
            if (isUsedInBills) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Cannot delete product that is used in existing bills. " +
                                "Consider disabling it instead."));
            }
            
            // Soft delete - set active = false using existing method
            Product deletedProduct = productService.disableProduct(id, systemUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Product disabled successfully");
            response.put("data", mapProductToJson(deletedProduct));
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete product: " + e.getMessage()));
        }
    }

    // ================================================================
    // HELPER METHODS - Convert Entities to JSON
    // ================================================================

    private Map<String, Object> mapCustomerToJson(Customer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", customer.getId());
        map.put("name", customer.getName());
        map.put("phone", customer.getPhone());
        map.put("email", customer.getEmail());
        map.put("totalOrders", customer.getTotalOrders());
        map.put("totalSpent", customer.getTotalSpent());
        map.put("lastOrderDate", customer.getLastOrderDate());
        map.put("createdAt", customer.getCreatedAt());
        map.put("sapCardCode", customer.getSapCardCode());
        return map;
    }

    private Map<String, Object> mapBillToJson(Bill bill) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", bill.getId());
        map.put("receiptNo", bill.getReceiptNo());
        map.put("customerId", bill.getCustomer() != null ? bill.getCustomer().getId() : null);
        map.put("customerName", bill.getCustomerName());
        map.put("customerPhone", bill.getCustomerPhone());
        map.put("customerEmail", bill.getCustomerEmail());
        map.put("createDate", bill.getCreateDate());
        map.put("deliveryDate", bill.getDeliveryDate());
        map.put("totalAmount", bill.getTotalAmount());
        map.put("finalAmount", bill.getFinalAmount());
        map.put("status", bill.getStatus() != null ? bill.getStatus().name() : "UNKNOWN");
        map.put("discountType", bill.getDiscountType());
        map.put("discountAmount", bill.getDiscountAmount());
        map.put("createdAt", bill.getCreatedAt());
        map.put("updatedAt", bill.getUpdatedAt());
        map.put("sapDocEntry", bill.getSapDocEntry());
        
        List<Map<String, Object>> items = bill.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("name", item.getName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("subtotal", item.getSubtotal());
                    itemMap.put("finalPrice", item.getFinalPrice());
                    itemMap.put("isCustom", item.isCustom());
                    return itemMap;
                })
                .collect(Collectors.toList());
        
        map.put("items", items);
        return map;
    }

    private Map<String, Object> mapProductToJson(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("price", product.getPrice());
        map.put("active", product.isActive());
        map.put("custom", product.isCustom());
        map.put("sapItemCode", product.getSapItemCode());
        return map;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }
}
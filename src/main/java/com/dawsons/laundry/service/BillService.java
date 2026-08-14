package com.dawsons.laundry.service;

import com.dawsons.laundry.dto.BillItemRequest;
import com.dawsons.laundry.dto.BillRequest;
import com.dawsons.laundry.entity.*;
import com.dawsons.laundry.exception.BadRequestException;
import com.dawsons.laundry.exception.ResourceNotFoundException;
import com.dawsons.laundry.repository.BillRepository;
import com.dawsons.laundry.repository.CustomerRepository;
import com.dawsons.laundry.repository.ProductRepository;
import com.dawsons.laundry.sap.SapB1SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final SapB1SyncService sapB1SyncService;

    public BillService(BillRepository billRepository, ProductRepository productRepository, 
                       AuditService auditService, CustomerService customerService,
                       CustomerRepository customerRepository, EmailService emailService,
                       SapB1SyncService sapB1SyncService) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.emailService = emailService;
        this.sapB1SyncService = sapB1SyncService;
    }

    @Transactional
    public Bill createBill(BillRequest request, User actor) {
        List<BillItem> items = buildItems(request.getItems(), actor);
        if (items.isEmpty()) {
            throw new BadRequestException("A bill must contain at least one item");
        }

        // Calculate totals with discounts
        BigDecimal subtotal = items.stream()
                .map(BillItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalDiscount = items.stream()
                .map(BillItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal finalTotal = items.stream()
                .map(BillItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int deliveryInDays = request.getDeliveryInDays() != null ? request.getDeliveryInDays() : 5;

        Bill bill = new Bill();
        bill.setReceiptNo(nextReceiptNo());
        bill.setCustomerName(request.getCustomerName());
        bill.setCustomerPhone(request.getCustomerPhone());
        bill.setCustomerEmail(request.getCustomerEmail());
        
        // Handle Customer - Auto create or find existing
        Customer customer = null;
        
        if (request.getCustomerId() != null) {
            customer = customerService.getCustomerById(request.getCustomerId());

            // Guard against a stale selection: the cashier may have picked this
            // customer from search earlier and then edited the name/phone/email
            // fields to someone else's details. Don't silently bill it to the
            // originally selected customer (and don't overwrite that customer's
            // saved data either) - reject and say whose record this actually is.
            if (request.getCustomerName() != null
                    && !request.getCustomerName().trim().equalsIgnoreCase(customer.getName().trim())) {
                throw new BadRequestException(
                        "The selected customer is \"" + customer.getName() + "\", but the name field shows \""
                                + request.getCustomerName().trim() + "\". Re-select the correct customer, clear the "
                                + "selection to create a new customer, or edit their details from the Customers tab.");
            }
            if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
                String cleanPhone = request.getCustomerPhone().replaceAll("[^0-9]", "");
                if (customer.getPhone() != null && !cleanPhone.equals(customer.getPhone())) {
                    throw new BadRequestException(
                            "The selected customer's phone number is " + customer.getPhone()
                                    + ", which doesn't match the phone number entered. Re-select the correct "
                                    + "customer or edit their details from the Customers tab.");
                }
            }
            if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()
                    && customer.getEmail() != null
                    && !customer.getEmail().equalsIgnoreCase(request.getCustomerEmail().trim())) {
                throw new BadRequestException(
                        "The selected customer's email is " + customer.getEmail()
                                + ", which doesn't match the email entered. Re-select the correct customer or "
                                + "edit their details from the Customers tab.");
            }
        } else if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            String cleanPhone = request.getCustomerPhone().replaceAll("[^0-9]", "");
            customer = customerService.getCustomerByPhone(cleanPhone);
            boolean matchedByEmail = false;

            if (customer == null && request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
                customer = customerService.getCustomerByEmail(request.getCustomerEmail());
                matchedByEmail = customer != null;
            }

            if (customer == null) {
                customer = customerService.createCustomer(
                        request.getCustomerName(),
                        request.getCustomerPhone(),
                        request.getCustomerEmail(),
                        actor
                );
            } else {
                // An existing customer already owns this phone/email. Never silently
                // overwrite their saved details from a bill form - that's how one
                // customer's record used to get clobbered by someone else's typed name.
                // Instead, reject the mismatch and tell the cashier who already owns it,
                // so they can pick the right customer or edit details from the Customers tab.
                String matchedOn = matchedByEmail ? "email address" : "phone number";

                if (request.getCustomerName() != null
                        && !request.getCustomerName().trim().equalsIgnoreCase(customer.getName().trim())) {
                    throw new BadRequestException(
                            "This " + matchedOn + " is already registered to customer \"" + customer.getName()
                                    + "\". Select them from the search results, or update their details from the Customers tab.");
                }

                if (!matchedByEmail && request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()
                        && customer.getEmail() != null
                        && !customer.getEmail().equalsIgnoreCase(request.getCustomerEmail().trim())) {
                    throw new BadRequestException(
                            "This phone number is already registered with a different email (" + customer.getEmail()
                                    + "). Update the customer's details from the Customers tab if this is incorrect.");
                }

                if (matchedByEmail && !cleanPhone.equals(customer.getPhone())) {
                    throw new BadRequestException(
                            "This email is already registered with a different phone number (" + customer.getPhone()
                                    + "). Update the customer's details from the Customers tab if this is incorrect.");
                }

                // Everything matches what's already on file - safe to reuse as-is.
            }
        } else {
            throw new BadRequestException("Phone number is required to create a customer");
        }
        
        if (customer != null) {
            bill.setCustomer(customer);
            customerService.updateCustomerStats(customer.getId(), finalTotal);
            bill.setCustomerName(customer.getName());
            bill.setCustomerPhone(customer.getPhone());
            bill.setCustomerEmail(customer.getEmail());
        }
        
        bill.setCreateDate(LocalDate.now());
        bill.setDeliveryDate(LocalDate.now().plusDays(deliveryInDays));
        bill.setTotalAmount(subtotal);  // Original total before discount
        bill.setDiscountType(request.getDiscountType());
        bill.setDiscountValue(request.getDiscountValue());
        bill.setDiscountAmount(totalDiscount);
        bill.setFinalAmount(finalTotal);
        bill.setStatus(BillStatus.PENDING);
        bill.setCreatedBy(actor);
        bill.setUpdatedBy(actor);

        items.forEach(i -> i.setBill(bill));
        bill.setItems(items);

        Bill saved = billRepository.save(bill);

        auditService.log(actor, "CREATE_BILL", "BILL", saved.getId(),
                "Created receipt #" + saved.getReceiptNo() + " for " + saved.getCustomerName()
                        + " total Rs " + finalTotal);

        // Send Order Confirmation Email
        try {
            if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                logger.info("Sending order confirmation to: {}", customer.getEmail());
                emailService.sendOrderConfirmation(saved, customer.getEmail());
                logger.info("Order confirmation email sent for receipt #{}", saved.getReceiptNo());
            }
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email: {}", e.getMessage());
        }

        // Sync to SAP B1
        try {
            sapB1SyncService.syncBillToSap(saved);
            logger.info("Bill #{} synced to SAP B1", saved.getReceiptNo());
        } catch (Exception e) {
            logger.error("Failed to sync bill to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Bill editBill(Integer receiptNo, BillRequest request, User actor) {

        Bill bill = getByReceiptNoOrThrow(receiptNo);

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Cannot edit a bill that is already PAID");
        }
        if (bill.getStatus() == BillStatus.VOIDED) {
            throw new BadRequestException("Cannot edit a VOIDED bill");
        }

        List<BillItem> newItems = buildItems(request.getItems(), actor);
        if (newItems.isEmpty()) {
            throw new BadRequestException("A bill must contain at least one item");
        }

        BigDecimal oldTotal = bill.getTotalAmount();
        BigDecimal newSubtotal = newItems.stream()
                .map(BillItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal newTotalDiscount = newItems.stream()
                .map(BillItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal newFinalTotal = newItems.stream()
                .map(BillItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        bill.getItems().clear();
        newItems.forEach(i -> i.setBill(bill));
        bill.getItems().addAll(newItems);

        bill.setCustomerName(request.getCustomerName());
        if (request.getCustomerPhone() != null) bill.setCustomerPhone(request.getCustomerPhone());
        if (request.getCustomerEmail() != null) bill.setCustomerEmail(request.getCustomerEmail());
        bill.setTotalAmount(newSubtotal);
        bill.setDiscountType(request.getDiscountType());
        bill.setDiscountValue(request.getDiscountValue());
        bill.setDiscountAmount(newTotalDiscount);
        bill.setFinalAmount(newFinalTotal);
        bill.setUpdatedBy(actor);

        // Handle customer update
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            String cleanPhone = request.getCustomerPhone().replaceAll("[^0-9]", "");
            Customer customer = customerService.getCustomerByPhone(cleanPhone);
            boolean matchedByEmail = false;

            if (customer == null && request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
                customer = customerService.getCustomerByEmail(request.getCustomerEmail());
                matchedByEmail = customer != null;
            }
            if (customer == null) {
                customer = customerService.createCustomer(
                        request.getCustomerName(),
                        request.getCustomerPhone(),
                        request.getCustomerEmail(),
                        actor
                );
            } else {
                // Same rule as creating a bill: a phone/email match on an existing
                // customer must not be re-labeled with a different typed name -
                // reject instead of silently re-pointing this bill at them.
                String matchedOn = matchedByEmail ? "email address" : "phone number";
                if (request.getCustomerName() != null
                        && !request.getCustomerName().trim().equalsIgnoreCase(customer.getName().trim())) {
                    throw new BadRequestException(
                            "This " + matchedOn + " is already registered to customer \"" + customer.getName()
                                    + "\". Re-select the correct customer, or update their details from the Customers tab.");
                }
                if (!matchedByEmail && request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()
                        && customer.getEmail() != null
                        && !customer.getEmail().equalsIgnoreCase(request.getCustomerEmail().trim())) {
                    throw new BadRequestException(
                            "This phone number is already registered with a different email (" + customer.getEmail()
                                    + "). Update the customer's details from the Customers tab if this is incorrect.");
                }
                if (matchedByEmail && !cleanPhone.equals(customer.getPhone())) {
                    throw new BadRequestException(
                            "This email is already registered with a different phone number (" + customer.getPhone()
                                    + "). Update the customer's details from the Customers tab if this is incorrect.");
                }
            }
            bill.setCustomer(customer);
            bill.setCustomerName(customer.getName());
            bill.setCustomerPhone(customer.getPhone());
            bill.setCustomerEmail(customer.getEmail());
        }

        Bill saved = billRepository.save(bill);

        auditService.log(actor, "EDIT_BILL", "BILL", saved.getId(),
                "Edited receipt #" + receiptNo + " total Rs " + oldTotal + " -> Rs " + newFinalTotal);

        // Sync to SAP B1 (update existing order or create new one)
        try {
            sapB1SyncService.syncBillToSap(saved);
            logger.info("Bill #{} edit synced to SAP B1", saved.getReceiptNo());
        } catch (Exception e) {
            logger.error("Failed to sync edited bill to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Bill markPaid(Integer receiptNo, User actor) {

        Bill bill = getByReceiptNoOrThrow(receiptNo);

        if (bill.getStatus() == BillStatus.VOIDED) {
            throw new BadRequestException("Cannot mark a VOIDED bill as PAID");
        }
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill is already PAID");
        }

        bill.setStatus(BillStatus.PAID);
        bill.setUpdatedBy(actor);
        Bill saved = billRepository.save(bill);

        auditService.log(actor, "MARK_PAID", "BILL", saved.getId(),
                "Receipt #" + receiptNo + " marked PAID / delivered");

        // Send Payment Confirmation Email
        try {
            Customer customer = saved.getCustomer();
            if (customer != null) {
                String email = customer.getEmail();
                logger.info("Customer email: {}", email);
                if (email != null && !email.isEmpty()) {
                    logger.info("Sending payment confirmation to: {}", email);
                    emailService.sendPaymentConfirmation(saved, email);
                    logger.info("Payment confirmation email sent for receipt #{}", receiptNo);
                } else {
                    logger.warn("Customer email is empty for receipt #{}", receiptNo);
                }
            } else {
                logger.warn("No customer found for receipt #{}", receiptNo);
            }
        } catch (Exception e) {
            logger.error("Failed to send payment email for receipt #{}: {}", receiptNo, e.getMessage());
        }

        // Sync to SAP B1 (update order status)
        try {
            // You might want to update the order in SAP B1 to reflect paid status
            // This could be a PATCH request to update the order
            logger.info("Bill #{} paid status should be synced to SAP B1", receiptNo);
            // sapB1SyncService.updateOrderStatusInSap(saved, "PAID");
        } catch (Exception e) {
            logger.error("Failed to sync bill paid status to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Bill voidBill(Integer receiptNo, String reason, boolean isReturn, User actor) {

        Bill bill = getByReceiptNoOrThrow(receiptNo);

        if (bill.getStatus() == BillStatus.VOIDED) {
            throw new BadRequestException("Bill is already voided");
        }

        bill.setStatus(isReturn ? BillStatus.RETURNED : BillStatus.VOIDED);
        bill.setVoidReason(reason);
        bill.setUpdatedBy(actor);
        Bill saved = billRepository.save(bill);

        auditService.log(actor, isReturn ? "RETURN_BILL" : "VOID_BILL", "BILL", saved.getId(),
                "Receipt #" + receiptNo + " " + (isReturn ? "returned" : "voided") + ". Reason: " + reason);

        // Sync to SAP B1 (cancel order)
        try {
            // You might want to cancel the order in SAP B1
            // sapB1SyncService.cancelOrderInSap(saved);
            logger.info("Bill #{} void/return should be synced to SAP B1", receiptNo);
        } catch (Exception e) {
            logger.error("Failed to sync bill void/return to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    public Bill getByReceiptNoOrThrow(Integer receiptNo) {
        return billRepository.findByReceiptNo(receiptNo)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt #" + receiptNo + " not found"));
    }

    public List<Bill> getAllActive() {
        return billRepository.findByStatusNotOrderByReceiptNoDesc(BillStatus.VOIDED);
    }

    public List<Bill> getAllIncludingVoided() {
        return billRepository.findAll();
    }

    public List<Bill> getByDate(LocalDate date) {
        return billRepository.findByCreateDateOrderByReceiptNoDesc(date);
    }

    public BigDecimal getDailyTotal(LocalDate date) {
        return billRepository.getDailyTotal(date);
    }

    public List<Bill> searchBills(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return billRepository.searchBills(query.trim());
    }

    public List<Bill> getBillsByStatus(BillStatus status) {
        return billRepository.findByStatus(status);
    }

    @Transactional
    public Bill updateBillStatus(Integer receiptNo, BillStatus newStatus, User actor) {
        Bill bill = getByReceiptNoOrThrow(receiptNo);
        
        if (bill.getStatus() == BillStatus.VOIDED || bill.getStatus() == BillStatus.RETURNED) {
            throw new BadRequestException("Cannot update status of " + bill.getStatus() + " bill");
        }
        
        BillStatus oldStatus = bill.getStatus();
        bill.setStatus(newStatus);
        bill.setUpdatedBy(actor);
        bill.setStatusUpdatedAt(java.time.LocalDateTime.now());
        
        if (newStatus == BillStatus.PROCESSING) {
            bill.setProcessingStartedAt(java.time.LocalDateTime.now());
        } else if (newStatus == BillStatus.READY) {
            bill.setReadyAt(java.time.LocalDateTime.now());
        }
        
        Bill saved = billRepository.save(bill);
        
        auditService.log(actor, "UPDATE_STATUS", "BILL", saved.getId(),
                "Receipt #" + receiptNo + " status: " + oldStatus + " → " + newStatus);

        // Send Ready Notification Email
        if (newStatus == BillStatus.READY) {
            try {
                Customer customer = saved.getCustomer();
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    emailService.sendReadyNotification(saved, customer.getEmail());
                    logger.info("Ready notification email sent for receipt #{}", receiptNo);
                }
            } catch (Exception e) {
                logger.error("Failed to send ready notification email: {}", e.getMessage());
            }
        }

        // Send Payment Confirmation Email
        if (newStatus == BillStatus.PAID) {
            try {
                Customer customer = saved.getCustomer();
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                    emailService.sendPaymentConfirmation(saved, customer.getEmail());
                    logger.info("Payment confirmation email sent for receipt #{}", receiptNo);
                }
            } catch (Exception e) {
                logger.error("Failed to send payment confirmation email: {}", e.getMessage());
            }
        }

        // Sync status update to SAP B1
        try {
            // sapB1SyncService.updateOrderStatusInSap(saved, newStatus.name());
            logger.info("Bill #{} status update should be synced to SAP B1: {}", receiptNo, newStatus);
        } catch (Exception e) {
            logger.error("Failed to sync bill status update to SAP B1: {}", e.getMessage());
        }
        
        return saved;
    }

    private int nextReceiptNo() {
        return billRepository.findMaxReceiptNo() + 1;
    }

    private List<BillItem> buildItems(List<BillItemRequest> requests, User actor) {
        List<BillItem> items = new ArrayList<>();
        for (BillItemRequest r : requests) {
            
            BillItem item = new BillItem();
            int qty = r.getQuantity();
            BigDecimal price;
            String name;
            boolean isCustom = false;
            
            if (r.getIsCustom() != null && r.getIsCustom()) {
                name = r.getCustomName();
                price = r.getCustomPrice();
                if (name == null || name.trim().isEmpty()) {
                    throw new BadRequestException("Custom item name is required");
                }
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BadRequestException("Custom item price must be greater than 0");
                }
                isCustom = true;
            } else {
                Product product = productRepository.findById(r.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product #" + r.getProductId() + " not found"));
                name = product.getName();
                price = product.getPrice();
            }
            
            item.setName(name);
            item.setQuantity(qty);
            item.setPrice(price);
            item.setCustom(isCustom);
            
            // ============================================================
            // Apply Discount Per Item
            // ============================================================
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal finalPrice = subtotal;
            String discountType = r.getDiscountType();
            BigDecimal discountValue = r.getDiscountValue();
            
            if (discountType != null && discountValue != null && discountValue.compareTo(BigDecimal.ZERO) > 0) {
                if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
                    if (discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                        throw new BadRequestException("Percentage discount cannot exceed 100%");
                    }
                    discountAmount = subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                } else if ("FIXED".equalsIgnoreCase(discountType)) {
                    if (discountValue.compareTo(subtotal) > 0) {
                        discountAmount = subtotal;
                    } else {
                        discountAmount = discountValue;
                    }
                }
                finalPrice = subtotal.subtract(discountAmount);
            }
            
            item.setSubtotal(subtotal);
            item.setDiscountType(discountType);
            item.setDiscountValue(discountValue);
            item.setDiscountAmount(discountAmount);
            item.setFinalPrice(finalPrice);
            
            items.add(item);
        }
        return items;
    }

    
}
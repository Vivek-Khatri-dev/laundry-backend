package com.dawsons.laundry.service;

import com.dawsons.laundry.entity.Customer;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.exception.BadRequestException;
import com.dawsons.laundry.exception.ResourceNotFoundException;
import com.dawsons.laundry.repository.CustomerRepository;
import com.dawsons.laundry.sap.SapB1SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final SapB1SyncService sapB1SyncService;

    public CustomerService(CustomerRepository customerRepository, 
                           AuditService auditService,
                           SapB1SyncService sapB1SyncService) {
        this.customerRepository = customerRepository;
        this.auditService = auditService;
        this.sapB1SyncService = sapB1SyncService;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public Customer getCustomerByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        String cleanPhone = cleanPhoneNumber(phone);
        return customerRepository.findByPhone(cleanPhone).orElse(null);
    }

    public Customer getCustomerByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        return customerRepository.findByEmail(email).orElse(null);
    }

    public List<Customer> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return customerRepository.findAll();
        }
        String cleanQuery = query.trim().replaceAll("[^0-9]", "");
        return customerRepository.searchCustomers(cleanQuery);
    }

    @Transactional
    public Customer createCustomer(String name, String phone, String email, User actor) {
        // Clean and validate phone number
        String cleanPhone = cleanPhoneNumber(phone);

        if (cleanPhone == null || cleanPhone.isEmpty()) {
            throw new BadRequestException("Phone number is required");
        }

        if (cleanPhone.length() != 11) {
            throw new BadRequestException("Phone number must be exactly 11 digits");
        }

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        String cleanEmail = email.trim();

        // Reject duplicates instead of silently merging into an existing customer
        Optional<Customer> existingByPhone = customerRepository.findByPhone(cleanPhone);
        if (existingByPhone.isPresent()) {
            throw new BadRequestException("A customer with this phone number already exists");
        }

        Optional<Customer> existingByEmail = customerRepository.findByEmail(cleanEmail);
        if (existingByEmail.isPresent()) {
            throw new BadRequestException("A customer with this email already exists");
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(cleanPhone);
        customer.setEmail(cleanEmail);
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);

        Customer saved = customerRepository.save(customer);
        auditService.log(actor, "CREATE_CUSTOMER", "CUSTOMER", saved.getId(),
                "Created customer: " + name + " (" + cleanPhone + ", " + cleanEmail + ")");

        // Sync to SAP B1
        try {
            String cardCode = sapB1SyncService.syncCustomerToSap(saved);
            if (cardCode != null) {
                saved.setSapCardCode(cardCode);
                customerRepository.save(saved);
                logger.info("Customer synced to SAP B1 with CardCode: {}", cardCode);
            }
        } catch (Exception e) {
            // Log error but don't rollback - we want local operation to succeed
            logger.error("Failed to sync customer to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Customer updateCustomer(Integer id, String name, String phone, String email, String notes, User actor) {
        Customer customer = getCustomerById(id);
        
        // Check if phone is being changed
        if (phone != null && !phone.isEmpty()) {
            String cleanPhone = cleanPhoneNumber(phone);
            if (cleanPhone.length() != 11) {
                throw new BadRequestException("Phone number must be exactly 11 digits");
            }
            Optional<Customer> existing = customerRepository.findByPhone(cleanPhone);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new BadRequestException("Phone number already registered to another customer");
            }
            customer.setPhone(cleanPhone);
        }
        
        // Check if email is being changed
        if (email != null && !email.isEmpty()) {
            Optional<Customer> existing = customerRepository.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new BadRequestException("Email already registered to another customer");
            }
            customer.setEmail(email);
        }
        
        customer.setName(name);
        customer.setNotes(notes);
        
        Customer saved = customerRepository.save(customer);
        auditService.log(actor, "UPDATE_CUSTOMER", "CUSTOMER", saved.getId(),
                "Updated customer: " + name);

        // Sync to SAP B1
        try {
            String cardCode = sapB1SyncService.syncCustomerToSap(saved);
            if (cardCode != null) {
                saved.setSapCardCode(cardCode);
                customerRepository.save(saved);
                logger.info("Customer update synced to SAP B1 with CardCode: {}", cardCode);
            }
        } catch (Exception e) {
            logger.error("Failed to sync customer update to SAP B1: {}", e.getMessage());
        }

        return saved;
    }
    
    @Transactional
    public void updateCustomerStats(Integer customerId, BigDecimal amount) {
        Customer customer = getCustomerById(customerId);
        customer.setTotalOrders(customer.getTotalOrders() + 1);
        customer.setTotalSpent(customer.getTotalSpent().add(amount));
        customer.setLastOrderDate(LocalDateTime.now());
        customerRepository.save(customer);
    }

    private String cleanPhoneNumber(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^0-9]", "");
    }
    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}
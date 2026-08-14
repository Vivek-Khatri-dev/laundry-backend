package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.CustomerRequest;
import com.dawsons.laundry.dto.CustomerResponse;
import com.dawsons.laundry.entity.Customer;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.security.UserPrincipal;
import com.dawsons.laundry.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class CustomerController {

    private final CustomerService customerService;
    private final UserRepository userRepository;

    public CustomerController(CustomerService customerService, UserRepository userRepository) {
        this.customerService = customerService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers().stream()
                .map(CustomerResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<CustomerResponse> searchCustomers(@RequestParam String q) {
        return customerService.searchCustomers(q).stream()
                .map(CustomerResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(@PathVariable String phone) {
        Customer customer = customerService.getCustomerByPhone(phone);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new CustomerResponse(customer));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        Customer customer = customerService.getCustomerByEmail(email);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new CustomerResponse(customer));
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Integer id) {
        return new CustomerResponse(customerService.getCustomerById(id));
    }

    @PostMapping
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        User actor = getCurrentUser(principal);
        Customer customer = customerService.createCustomer(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                actor
        );
        return new CustomerResponse(customer);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(@PathVariable Integer id,
                                            @Valid @RequestBody CustomerRequest request,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        User actor = getCurrentUser(principal);
        Customer customer = customerService.updateCustomer(
                id,
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getNotes(),
                actor
        );
        return new CustomerResponse(customer);
    }

    private User getCurrentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
package com.dawsons.laundry.controller;

import com.dawsons.laundry.dto.ProductRequest;
import com.dawsons.laundry.dto.ProductResponse;
import com.dawsons.laundry.entity.Product;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.repository.UserRepository;
import com.dawsons.laundry.security.UserPrincipal;
import com.dawsons.laundry.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;
    

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    // Cashier needs this list to build bills, so it's open to both roles (see SecurityConfig).
    @GetMapping
    public List<ProductResponse> getActiveProducts() {
        return productService.getActiveProducts().stream().map(ProductResponse::new).collect(Collectors.toList());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts().stream().map(ProductResponse::new).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse addProduct(@Valid @RequestBody ProductRequest request,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return new ProductResponse(productService.addProduct(request, currentUser(principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(@PathVariable Integer id,
                                          @Valid @RequestBody ProductRequest request,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return new ProductResponse(productService.updatePrice(id, request, currentUser(principal)));
    }

    // No hard delete - disable/enable only, so historical bills referencing this product stay valid.
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse disableProduct(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        return new ProductResponse(productService.disableProduct(id, currentUser(principal)));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse enableProduct(@PathVariable Integer id,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return new ProductResponse(productService.enableProduct(id, currentUser(principal)));
    }

    private User currentUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished from DB"));
    }

    // Add this method to ProductController.java
    @GetMapping("/search")
    public List<ProductResponse> searchProducts(@RequestParam String q) {
        return productService.searchProducts(q).stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }
}

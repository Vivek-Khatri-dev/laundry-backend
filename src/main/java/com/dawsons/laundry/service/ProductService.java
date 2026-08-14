package com.dawsons.laundry.service;

import com.dawsons.laundry.dto.ProductRequest;
import com.dawsons.laundry.entity.Product;
import com.dawsons.laundry.entity.User;
import com.dawsons.laundry.exception.ResourceNotFoundException;
import com.dawsons.laundry.repository.ProductRepository;
import com.dawsons.laundry.sap.SapB1SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final SapB1SyncService sapB1SyncService;

    public ProductService(ProductRepository productRepository, 
                          AuditService auditService,
                          SapB1SyncService sapB1SyncService) {
        this.productRepository = productRepository;
        this.auditService = auditService;
        this.sapB1SyncService = sapB1SyncService;
    }

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get product by ID (throws exception if not found)
     */
    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product addProduct(ProductRequest request, User actor) {
        Product product = new Product(request.getName(), request.getPrice());
        Product saved = productRepository.save(product);

        auditService.log(actor, "ADD_PRODUCT", "PRODUCT", saved.getId(),
                "Added '" + saved.getName() + "' at Rs " + saved.getPrice());

        // Sync to SAP B1
        try {
            String itemCode = sapB1SyncService.syncProductToSap(saved);
            if (itemCode != null) {
                saved.setSapItemCode(itemCode);
                productRepository.save(saved);
                logger.info("Product synced to SAP B1 with ItemCode: {}", itemCode);
            }
        } catch (Exception e) {
            logger.error("Failed to sync product to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Full update of product (PUT)
     */
    @Transactional
    public Product updateProduct(Integer id, ProductRequest request, User actor) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        // Only update active if provided, otherwise keep existing
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        Product saved = productRepository.save(product);
        
        auditService.log(actor, "UPDATE_PRODUCT", "PRODUCT", saved.getId(),
                "Updated product: " + saved.getName() + " - Price: Rs " + saved.getPrice());
        
        // Sync to SAP B1
        try {
            sapB1SyncService.syncProductToSap(saved);
        } catch (Exception e) {
            logger.error("Failed to sync product update to SAP B1: {}", e.getMessage());
        }
        
        return saved;
    }

    @Transactional
    public Product updatePrice(Integer id, ProductRequest request, User actor) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product #" + id + " not found"));

        BigDecimal oldPrice = product.getPrice();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        Product saved = productRepository.save(product);

        auditService.log(actor, "EDIT_PRODUCT", "PRODUCT", saved.getId(),
                "'" + saved.getName() + "' price Rs " + oldPrice + " -> Rs " + saved.getPrice());

        // Sync to SAP B1
        try {
            String itemCode = sapB1SyncService.syncProductToSap(saved);
            if (itemCode != null) {
                saved.setSapItemCode(itemCode);
                productRepository.save(saved);
                logger.info("Product update synced to SAP B1 with ItemCode: {}", itemCode);
            }
        } catch (Exception e) {
            logger.error("Failed to sync product update to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Save an existing product (for partial updates)
     */
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    /** Soft-disable instead of hard delete - keeps historical bills intact and valid. */
    @Transactional
    public Product disableProduct(Integer id, User actor) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product #" + id + " not found"));

        product.setActive(false);
        Product saved = productRepository.save(product);

        auditService.log(actor, "DISABLE_PRODUCT", "PRODUCT", saved.getId(),
                "Disabled '" + saved.getName() + "'");

        // Sync to SAP B1
        try {
            String itemCode = sapB1SyncService.syncProductToSap(saved);
            if (itemCode != null) {
                saved.setSapItemCode(itemCode);
                productRepository.save(saved);
                logger.info("Product disable synced to SAP B1 with ItemCode: {}", itemCode);
            }
        } catch (Exception e) {
            logger.error("Failed to sync product disable to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    @Transactional
    public Product enableProduct(Integer id, User actor) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product #" + id + " not found"));

        product.setActive(true);
        Product saved = productRepository.save(product);

        auditService.log(actor, "ENABLE_PRODUCT", "PRODUCT", saved.getId(),
                "Re-enabled '" + saved.getName() + "'");

        // Sync to SAP B1
        try {
            String itemCode = sapB1SyncService.syncProductToSap(saved);
            if (itemCode != null) {
                saved.setSapItemCode(itemCode);
                productRepository.save(saved);
                logger.info("Product enable synced to SAP B1 with ItemCode: {}", itemCode);
            }
        } catch (Exception e) {
            logger.error("Failed to sync product enable to SAP B1: {}", e.getMessage());
        }

        return saved;
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String searchTerm = "%" + query.trim() + "%";
        return productRepository.searchProducts(searchTerm);
    }
}
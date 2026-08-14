package com.dawsons.laundry.repository;

import com.dawsons.laundry.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByActiveTrue();

    // Add this method to ProductRepository.java
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:searchTerm)")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
}



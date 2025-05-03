package com.iqbusiness.khaya.controller;

import com.iqbusiness.khaya.model.Product;
import com.iqbusiness.khaya.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Non-cached endpoint
    @GetMapping("/nocache/{id}")
    public ResponseEntity<Product> getProductNoCache(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();

        Product product = productService.getProductByIdNoCache(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

    // Cached endpoint
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();

        Product product = productService.getProductByIdWithCache(id);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(product);
    }

}

package com.iqbusiness.khaya.service;

import com.iqbusiness.khaya.model.Product;
import com.iqbusiness.khaya.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private static final Random random = new Random();
    private static final String CACHE_NAME = "products";

    @Value("${app.database.simulate-latency:false}")
    private boolean simulateLatency;

    @Value("${app.database.latency-ms:500}")
    private int latencyMs;

    @PostConstruct
    public void init() {
        // Load some example products
        if (productRepository.count() == 0) {
            createSampleProducts();
        }
    }

    private void createSampleProducts() {
        List<Product> products = new ArrayList<>();
        String[] categories = {"Electronics", "Books", "Clothing", "Food", "Home"};

        for (int i = 1; i <= 50; i++) {
            Product product = Product.builder()
                    .name("Product " + i)
                    .description("Description for product " + i)
                    .price(BigDecimal.valueOf(10 + random.nextDouble() * 990).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .stockQuantity(random.nextInt(1000))
                    .category(categories[random.nextInt(categories.length)])
                    .imageUrl("https://example.com/products/" + i + ".jpg")
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            products.add(product);
        }

        productRepository.saveAll(products);
        log.info("Created {} sample products", products.size());
    }

    @Transactional
    public List<Product> getAllProducts() {
        simulateDatabaseLatency();
        return productRepository.findAll();
    }

    // Non-cached method - directly hits the database
    public Product getProductByIdNoCache(Long id) {
        long startTime = System.currentTimeMillis();

        simulateDatabaseLatency();
        Optional<Product> product = productRepository.findById(id);

        long endTime = System.currentTimeMillis();
//        cacheMetrics.recordNonCachedRequest(endTime - startTime);

        return product.orElse(null);
    }

    // Cached method - uses Redis cache provider
    @Cacheable(value = CACHE_NAME, key = "#id")
    public Product getProductByIdWithCache(Long id) {
        long startTime = System.currentTimeMillis();

        simulateDatabaseLatency();

        Optional<Product> product = productRepository.findById(id);

        long endTime = System.currentTimeMillis();
//        cacheMetrics.recordCachedRequest(endTime - startTime);

        return product.orElse(null);
    }

    @Transactional
    @CachePut(value = CACHE_NAME, key = "#product.id")
    public Product updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        simulateDatabaseLatency();
        product.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = CACHE_NAME, key = "#id")
    public void deleteProduct(Long id) {
        simulateDatabaseLatency();
        productRepository.deleteById(id);
    }

    private void simulateDatabaseLatency() {
        if (simulateLatency) {
            try {
                // Add a small random factor to the latency
                int actualLatency = latencyMs + random.nextInt(latencyMs / 2);
                Thread.sleep(actualLatency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}

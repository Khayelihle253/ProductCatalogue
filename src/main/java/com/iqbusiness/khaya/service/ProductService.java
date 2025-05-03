package com.iqbusiness.khaya.service;

import com.iqbusiness.khaya.model.Product;
import com.iqbusiness.khaya.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
//        simulateDatabaseLatency();
        return productRepository.findAll();
    }

    // Non-cached method - directly hits the database
    public Product getProductByIdNoCache(Long id) {
        long startTime = System.currentTimeMillis();

//        simulateDatabaseLatency();
        Optional<Product> product = productRepository.findById(id);

        long endTime = System.currentTimeMillis();
//        cacheMetrics.recordNonCachedRequest(endTime - startTime);

        return product.orElse(null);
    }

}

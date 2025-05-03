package com.iqbusiness.khaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class RedisCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedisCatalogApplication.class, args);
    }
}
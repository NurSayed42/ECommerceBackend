package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_brand", columnList = "brand"),
        @Index(name = "idx_product_active", columnList = "active")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, length = 300)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products"})
    private Category category;

    @Column(length = 100)
    private String brand;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private int lowStockThreshold = 5;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean flashSale = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean trending = false;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avgRating = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int soldCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(columnDefinition = "TEXT")
    private String deliveryInfo;

    @Column(columnDefinition = "TEXT")
    private String returnPolicy;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(length = 100)
    private String dimensions;

    // Images stored as JSON string
    @Column(columnDefinition = "TEXT")
    private String images; // JSON array of image URLs

    @Column(length = 500)
    private String videoUrl;

    // FAQ stored as JSON
    @Column(columnDefinition = "TEXT")
    private String faq;

    // Meta tags
    @Column(length = 255)
    private String metaTitle;

    @Column(columnDefinition = "TEXT")
    private String metaDescription;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"product"})
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"product"})
    private List<Review> reviews = new ArrayList<>();
}
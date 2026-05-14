package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"variants", "reviews", "category"})
    private Product product;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(length = 255)
    private String colorCode;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(precision = 12, scale = 2)
    private BigDecimal additionalPrice;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Column(length = 500)
    private String image;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
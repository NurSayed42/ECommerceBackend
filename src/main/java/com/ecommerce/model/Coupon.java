package com.ecommerce.model;

import com.ecommerce.enums.CouponType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(nullable = false)
    @Builder.Default
    private int usageLimit = 0;

    @Column(nullable = false)
    @Builder.Default
    private int usageCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int perUserLimit = 1;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column
    private Long buyProductId;

    @Column
    private Long getProductId;
}
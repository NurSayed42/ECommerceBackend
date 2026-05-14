package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipping_zones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShippingZone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String districts; // JSON array of district names

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal freeShippingThreshold;

    @Column(nullable = false)
    @Builder.Default
    private int estimatedDays = 3;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
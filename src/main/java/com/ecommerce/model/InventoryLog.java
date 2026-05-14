package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"inventoryLogs", "variants", "reviews", "category"})
    private Product product;

    @Column(length = 50)
    private String variantSku;

    @Column(nullable = false)
    private int quantityChange;

    @Column(nullable = false)
    private int stockBefore;

    @Column(nullable = false)
    private int stockAfter;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(length = 255)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    @JsonIgnoreProperties({"password", "orders", "wishlists", "carts", "reviews"})
    private User admin;
}
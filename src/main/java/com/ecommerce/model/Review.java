package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "variants", "category"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "password", "orders", "wishlists", "carts", "addresses", "roles"})
    private User user;

    @Column(nullable = false)
    private int rating; // 1-5

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String images; // JSON array

    @Column(length = 500)
    private String videoUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean verifiedPurchase = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean approved = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean reported = false;

    @Column(columnDefinition = "TEXT")
    private String reportReason;

    @Column(nullable = false)
    @Builder.Default
    private int helpfulCount = 0;
}
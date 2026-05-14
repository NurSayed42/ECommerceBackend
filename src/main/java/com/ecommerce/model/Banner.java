package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Banner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String mobileImageUrl;

    @Column(length = 500)
    private String linkUrl;

    @Column(length = 50)
    @Builder.Default
    private String position = "HOME_HERO"; // HOME_HERO, HOME_MIDDLE, CATEGORY

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;
}
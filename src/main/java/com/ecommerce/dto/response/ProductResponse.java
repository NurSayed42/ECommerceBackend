package com.ecommerce.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal salePrice;
    private BigDecimal discountPercent;
    private String categoryName;
    private Long categoryId;
    private String brand;
    private String sku;
    private int stock;
    private boolean inStock;
    private boolean featured;
    private boolean flashSale;
    private boolean trending;
    private BigDecimal avgRating;
    private int reviewCount;
    private int soldCount;
    private List<String> images;
    private String videoUrl;
    private String deliveryInfo;
    private String returnPolicy;
    private String tags;
    private List<VariantResponse> variants;
    private LocalDateTime createdAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VariantResponse {
        private Long id;
        private String size;
        private String color;
        private String colorCode;
        private String sku;
        private BigDecimal additionalPrice;
        private int stock;
        private String image;
    }
}

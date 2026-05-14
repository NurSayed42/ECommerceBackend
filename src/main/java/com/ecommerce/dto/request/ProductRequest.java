package com.ecommerce.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private Long id;
    @NotBlank private String name;
    @NotBlank private String description;
    private String shortDescription;
    @NotNull @Positive private BigDecimal price;
    private BigDecimal salePrice;
    @NotNull private Long categoryId;
    private String brand;
    private String sku;
    @Min(0) private int stock;
    private int lowStockThreshold = 5;
    private BigDecimal discountPercent; // এই line যোগ করো
    private boolean active = true;      // এটাও
    private boolean featured;
    private boolean flashSale;
    private boolean trending;
    private String deliveryInfo;
    private String returnPolicy;
    private BigDecimal weight;
    private String dimensions;
    private List<String> images;
    private String videoUrl;
    private String tags;
    private String metaTitle;
    private String metaDescription;
    private List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        private String size;
        private String color;
        private String colorCode;
        private String sku;
        private BigDecimal additionalPrice;
        private int stock;
        private String image;
    }
}

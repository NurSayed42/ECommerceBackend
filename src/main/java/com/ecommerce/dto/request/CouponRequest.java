package com.ecommerce.dto.request;
import com.ecommerce.enums.CouponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @NotBlank private String code;
    private String description;
    @NotNull private CouponType type;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private int usageLimit;
    private int perUserLimit = 1;
    @NotNull private LocalDateTime startDate;
    @NotNull private LocalDateTime endDate;
    private boolean active = true;
    private Long buyProductId;
    private Long getProductId;
}

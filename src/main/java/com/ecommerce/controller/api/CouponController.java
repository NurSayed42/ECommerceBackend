package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.Coupon;
import com.ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponRepository couponRepository;

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount) {
        Coupon c = couponRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));
        if (c.getMinOrderAmount() != null && orderAmount.compareTo(c.getMinOrderAmount()) < 0)
            throw new BadRequestException("Minimum order amount is ৳" + c.getMinOrderAmount());

        // Map.of() null value accept করে না — HashMap ব্যবহার করো
        Map<String, Object> result = new HashMap<>();
        result.put("code", c.getCode());
        result.put("type", c.getType());
        result.put("discountValue", c.getDiscountValue());
        result.put("minOrderAmount", c.getMinOrderAmount());       // null হতে পারে
        result.put("maxDiscountAmount", c.getMaxDiscountAmount()); // null হতে পারে

        return ResponseEntity.ok(ApiResponse.success("Coupon valid", result));
    }
}

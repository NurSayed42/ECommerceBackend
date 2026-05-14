package com.ecommerce.controller.api;

import com.ecommerce.dto.response.*;
import com.ecommerce.service.impl.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.search(q, categoryId, minPrice, maxPrice, brand, minRating, inStock, sort, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getBySlug(slug)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(productService.getFeatured()));
    }

    @GetMapping("/flash-sale")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFlashSale() {
        return ResponseEntity.ok(ApiResponse.success(productService.getFlashSale()));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTrending() {
        return ResponseEntity.ok(ApiResponse.success(productService.getTrending()));
    }

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTopSelling(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getTopSelling(limit)));
    }
}

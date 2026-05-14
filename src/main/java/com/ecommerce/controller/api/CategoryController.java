package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findByParentIsNullAndActiveTrue()));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<ApiResponse<List<Category>>> getSubs(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findByParentIdAndActiveTrue(id)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Category>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findByFeaturedTrueAndActiveTrue()));
    }
}

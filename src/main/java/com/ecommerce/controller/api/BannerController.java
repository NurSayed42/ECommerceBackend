package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.model.Banner;
import com.ecommerce.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {
    private final BannerRepository bannerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Banner>>> getActiveBanners(
            @RequestParam(defaultValue = "HOME_HERO") String position) {
        return ResponseEntity.ok(ApiResponse.success(
                bannerRepository.findByActiveTrueAndPositionOrderBySortOrderAsc(position)));
    }
}

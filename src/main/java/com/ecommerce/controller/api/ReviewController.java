package com.ecommerce.controller.api;

import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Review;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.impl.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<Review>>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getProductReviews(productId, page, size)));
    }

    // User review দিতে পারবে কিনা check
    @GetMapping("/can-review/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canReview(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long productId) {
        Long userId = getUserId(ud);
        boolean canReview = reviewService.canUserReview(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("canReview", canReview)));
    }

    // Review submit — multipart (text + images)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Review>> createReview(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam("productId") Long productId,
            @RequestParam("rating") int rating,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam("comment") String comment,
            @RequestParam(value = "videoUrl", required = false) String videoUrl,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        Long userId = getUserId(ud);
        ReviewRequest req = ReviewRequest.builder()
                .productId(productId)
                .rating(rating)
                .title(title)
                .comment(comment)
                .videoUrl(videoUrl)
                .build();

        Review review = reviewService.createReview(userId, req, images);
        return ResponseEntity.ok(
                ApiResponse.success("Review submitted successfully!", review));
    }

    private Long getUserId(UserDetails ud) {
        return userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}
package com.ecommerce.service.impl;

import com.ecommerce.dto.request.ReviewRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    // ─── Delivered order check ───────────────────────────────────────
    public boolean canUserReview(Long userId, Long productId) {
        // delivered order আছে কিনা
        boolean hasDeliveredOrder = orderRepository
                .hasUserDeliveredOrderForProduct(userId, productId);
        // already reviewed কিনা
        boolean alreadyReviewed = reviewRepository
                .existsByProductIdAndUserId(productId, userId);
        return hasDeliveredOrder && !alreadyReviewed;
    }

    // ─── Create review with images ───────────────────────────────────
    @Transactional
    public Review createReview(Long userId, ReviewRequest req,
                               List<MultipartFile> images) {
        // Delivered order check
        if (!orderRepository.hasUserDeliveredOrderForProduct(userId, req.getProductId()))
            throw new BadRequestException(
                    "You can only review products from delivered orders");

        // Already reviewed check
        if (reviewRepository.existsByProductIdAndUserId(req.getProductId(), userId))
            throw new BadRequestException("You have already reviewed this product");

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // Image upload
        String imagesJson = "[]";
        if (images != null && !images.isEmpty()) {
            imagesJson = saveReviewImages(images);
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(req.getRating())
                .title(req.getTitle())
                .comment(req.getComment())
                .videoUrl(req.getVideoUrl())
                .images(imagesJson)
                .verifiedPurchase(true) // delivered order আছে তাই verified
                .approved(true)         // auto approve (চাইলে false করতে পারো)
                .build();

        Review saved = reviewRepository.save(review);
        updateProductRating(product);
        return saved;
    }

    // ─── Image save ──────────────────────────────────────────────────
    private String saveReviewImages(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        // Upload directory — application.properties এ configure করো
        String uploadDir = "uploads/reviews/";
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String ext = getExtension(file.getOriginalFilename());
                String filename = UUID.randomUUID() + "." + ext;
                Path dest = dir.resolve(filename);
                Files.copy(file.getInputStream(), dest,
                        StandardCopyOption.REPLACE_EXISTING);
                urls.add("/uploads/reviews/" + filename);
            }
            return objectMapper.writeValueAsString(urls);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload images");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "jpg";
    }

    // ─── Get reviews ─────────────────────────────────────────────────
    public Page<Review> getProductReviews(Long productId, int page, int size) {
        return reviewRepository.findByProductIdAndApprovedTrue(
                productId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    @Transactional
    public Review approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setApproved(true);
        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct());
        return saved;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
        updateProductRating(review.getProduct());
    }

    private void updateProductRating(Product product) {
        Double avg = reviewRepository.getAverageRating(product.getId());
        long count = reviewRepository.countApprovedByProductId(product.getId());
        product.setAvgRating(avg != null
                ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        product.setReviewCount((int) count);
        productRepository.save(product);
    }
}
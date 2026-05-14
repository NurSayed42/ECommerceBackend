package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.model.Wishlist;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Wishlist>>> getWishlist(@AuthenticationPrincipal UserDetails ud) {
        Long userId = getUserId(ud);
        return ResponseEntity.ok(ApiResponse.success(wishlistRepository.findByUserId(userId)));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse<String>> add(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long productId,
            @RequestParam(defaultValue = "false") boolean priceDropAlert,
            @RequestParam(defaultValue = "false") boolean stockAlert) {
        Long userId = getUserId(ud);
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId))
            return ResponseEntity.ok(ApiResponse.success("Already in wishlist", null));

        com.ecommerce.model.User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Wishlist w = Wishlist.builder()
                .user(user).product(product)
                .priceDropAlert(priceDropAlert).stockAlert(stockAlert)
                .build();
        wishlistRepository.save(w);
        return ResponseEntity.ok(ApiResponse.success("Added to wishlist", null));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse<String>> remove(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long productId) {
        Long userId = getUserId(ud);
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> check(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long productId) {
        Long userId = getUserId(ud);
        return ResponseEntity.ok(ApiResponse.success(wishlistRepository.existsByUserIdAndProductId(userId, productId)));
    }

    private Long getUserId(UserDetails ud) {
        return userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername())
                .map(u -> u.getId()).orElseThrow();
    }
}

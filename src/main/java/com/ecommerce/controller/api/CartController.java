package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.impl.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(getUserId(ud))));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.addToCart(getUserId(ud), productId, variantId, quantity)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Cart>> updateItem(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateQuantity(getUserId(ud), itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Cart>> removeItem(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeFromCart(getUserId(ud), itemId)));
    }

    @PostMapping("/items/{itemId}/save-for-later")
    public ResponseEntity<ApiResponse<String>> saveForLater(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long itemId) {
        cartService.saveForLater(getUserId(ud), itemId);
        return ResponseEntity.ok(ApiResponse.success("Saved for later"));
    }

    // null return এর বদলে exception throw করো
    private Long getUserId(UserDetails ud) {
        if (ud == null) throw new ResourceNotFoundException("User not authenticated");
        return userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ud.getUsername()))
                .getId();
    }
}
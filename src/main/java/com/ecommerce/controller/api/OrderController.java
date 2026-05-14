package com.ecommerce.controller.api;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.*;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.service.impl.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest req) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", orderService.placeOrder(userId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> myOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(orderService.getUserOrders(userId, page, size)));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getByOrderNumber(orderNumber)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, OrderStatus.CANCELLED, reason)));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse<OrderResponse>> returnOrder(
            @PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, OrderStatus.RETURN_REQUESTED, reason)));
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findByEmailOrPhone(userDetails.getUsername(), userDetails.getUsername())
                .map(u -> u.getId()).orElse(null);
    }
}

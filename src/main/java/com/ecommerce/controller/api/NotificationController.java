package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.model.Notification;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(ud);
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId, page, size)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(@AuthenticationPrincipal UserDetails ud) {
        Long userId = getUserId(ud);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", notificationService.getUnreadCount(userId))));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<String>> markAllRead(@AuthenticationPrincipal UserDetails ud) {
        notificationService.markAllRead(getUserId(ud));
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    private Long getUserId(UserDetails ud) {
        return userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername())
                .map(u -> u.getId()).orElseThrow();
    }
}

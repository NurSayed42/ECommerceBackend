package com.ecommerce.service.impl;

import com.ecommerce.enums.NotificationType;
import com.ecommerce.model.Notification;
import com.ecommerce.model.User;
import com.ecommerce.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createOrderNotification(User user, String orderNumber, String status) {
        String title = "Order " + status;
        String message = "Your order #" + orderNumber + " has been " + status.toLowerCase();
        NotificationType type = switch(status) {
            case "PLACED" -> NotificationType.ORDER_PLACED;
            case "SHIPPED" -> NotificationType.ORDER_SHIPPED;
            case "DELIVERED" -> NotificationType.ORDER_DELIVERED;
            case "CANCELLED" -> NotificationType.ORDER_CANCELLED;
            default -> NotificationType.GENERAL;
        };
        Notification notif = Notification.builder()
                .user(user).title(title).message(message).type(type).build();
        notificationRepository.save(notif);
    }

    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}

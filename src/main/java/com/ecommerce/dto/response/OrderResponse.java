package com.ecommerce.dto.response;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String couponCode;
    private String shippingAddress;
    private String trackingNumber;
    private String courierService;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime deliveredAt;
    private String orderNotes;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private String size;
        private String color;
        private String sku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private boolean reviewed;
    }
}

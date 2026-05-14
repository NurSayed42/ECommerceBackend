//package com.ecommerce.service.impl;
//
//import com.ecommerce.dto.request.OrderRequest;
//import com.ecommerce.dto.response.OrderResponse;
//import com.ecommerce.dto.response.PageResponse;
//import com.ecommerce.enums.OrderStatus;
//import com.ecommerce.enums.PaymentMethod;
//import com.ecommerce.exception.BadRequestException;
//import com.ecommerce.exception.ResourceNotFoundException;
//import com.ecommerce.model.*;
//import com.ecommerce.repository.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.UUID;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final CartRepository cartRepository;
//    private final UserRepository userRepository;
//    private final AddressRepository addressRepository;
//    private final CouponRepository couponRepository;
//    private final ShippingZoneRepository shippingZoneRepository;
//    private final NotificationService notificationService;
//    private final EmailService emailService;
//    private final ObjectMapper objectMapper;
//
//    @Transactional
//    public OrderResponse placeOrder(Long userId, OrderRequest req) {
//        User user = null;
//        Cart cart = null;
//        String shippingAddressJson;
//
//        if (userId != null) {
//            user = userRepository.findById(userId)
//                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
//            cart = cartRepository.findByUserId(userId)
//                    .orElseThrow(() -> new BadRequestException("Cart is empty"));
//            if (cart.getItems() == null || cart.getItems().isEmpty())
//                throw new BadRequestException("Cart is empty");
//
//            Address address = addressRepository.findById(req.getAddressId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Address", req.getAddressId()));
//            shippingAddressJson = toAddressJson(address);
//        } else {
//            // Guest order
//            if (req.getGuestInfo() == null) throw new BadRequestException("Guest info is required");
//            shippingAddressJson = toGuestAddressJson(req.getGuestInfo());
//        }
//
//        // Determine cart items to use
//        List<CartItem> itemsToOrder = (cart != null)
//                ? cart.getItems().stream().filter(i -> !i.isSavedForLater()).collect(Collectors.toList())
//                : List.of();
//
//        if (itemsToOrder.isEmpty() && userId != null)
//            throw new BadRequestException("No items selected for order");
//
//        // Calculate subtotal
//        BigDecimal subtotal = itemsToOrder.stream()
//                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Shipping cost
//        BigDecimal shippingCost = calculateShipping(req, subtotal);
//
//        // Coupon discount
//        BigDecimal discount = BigDecimal.ZERO;
//        Coupon coupon = null;
//        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
//            coupon = couponRepository.findByCodeAndActiveTrue(req.getCouponCode())
//                    .orElseThrow(() -> new BadRequestException("Invalid coupon code"));
//            discount = applyCoupon(coupon, subtotal);
//        }
//
//        // Tax (15% VAT for BD)
//        BigDecimal tax = subtotal.subtract(discount)
//                .multiply(BigDecimal.valueOf(0.00)); // Set to 0 or adjust
//
//        BigDecimal total = subtotal.add(shippingCost).subtract(discount).add(tax);
//
//        Order order = Order.builder()
//                .orderNumber(generateOrderNumber())
//                .user(user)
//                .guestEmail(req.getGuestInfo() != null ? req.getGuestInfo().getEmail() : null)
//                .guestName(req.getGuestInfo() != null ? req.getGuestInfo().getFullName() : null)
//                .guestPhone(req.getGuestInfo() != null ? req.getGuestInfo().getPhone() : null)
//                .shippingAddress(shippingAddressJson)
//                .paymentMethod(req.getPaymentMethod())
//                .subtotal(subtotal)
//                .shippingCost(shippingCost)
//                .discount(discount)
//                .tax(tax)
//                .totalAmount(total)
//                .couponCode(req.getCouponCode())
//                .orderNotes(req.getOrderNotes())
//                .estimatedDelivery(LocalDateTime.now().plusDays(3))
//                .build();
//
//        // Build order items
//        List<OrderItem> orderItems = itemsToOrder.stream().map(ci -> {
//            Product p = ci.getProduct();
//            // Validate stock
//            if (p.getStock() < ci.getQuantity())
//                throw new BadRequestException("Insufficient stock for: " + p.getName());
//
//            // Deduct stock
//            p.setStock(p.getStock() - ci.getQuantity());
//            p.setSoldCount(p.getSoldCount() + ci.getQuantity());
//
//            String image = "";
//            try {
//                List<String> imgs = objectMapper.readValue(p.getImages() != null ? p.getImages() : "[]", List.class);
//                if (!imgs.isEmpty()) image = imgs.get(0);
//            } catch (Exception ignored) {}
//
//            return OrderItem.builder()
//                    .order(order).product(p)
//                    .productName(p.getName()).productImage(image)
//                    .size(ci.getVariant() != null ? ci.getVariant().getSize() : null)
//                    .color(ci.getVariant() != null ? ci.getVariant().getColor() : null)
//                    .sku(ci.getVariant() != null ? ci.getVariant().getSku() : p.getSku())
//                    .quantity(ci.getQuantity())
//                    .unitPrice(ci.getUnitPrice())
//                    .totalPrice(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
//                    .build();
//        }).collect(Collectors.toList());
//
//        order.setItems(orderItems);
//
//        // Update coupon usage
//        if (coupon != null) {
//            coupon.setUsageCount(coupon.getUsageCount() + 1);
//            couponRepository.save(coupon);
//        }
//
//        Order saved = orderRepository.save(order);
//
//        // Clear cart
//        if (cart != null) {
//            cart.getItems().removeIf(i -> !i.isSavedForLater());
//            cartRepository.save(cart);
//        }
//
//        // Send notifications
//        String email = user != null ? user.getEmail() : (req.getGuestInfo() != null ? req.getGuestInfo().getEmail() : null);
//        String name = user != null ? user.getFullName() : (req.getGuestInfo() != null ? req.getGuestInfo().getFullName() : "Customer");
//        if (email != null) emailService.sendOrderConfirmationEmail(email, saved.getOrderNumber(), name);
//        if (user != null) notificationService.createOrderNotification(user, saved.getOrderNumber(), "PLACED");
//
//        return toResponse(saved);
//    }
//
//    @Transactional
//    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus, String reason) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
//        order.setStatus(newStatus);
//        if (newStatus == OrderStatus.CANCELLED) order.setCancelReason(reason);
//        if (newStatus == OrderStatus.DELIVERED) order.setDeliveredAt(LocalDateTime.now());
//        if (newStatus == OrderStatus.RETURN_REQUESTED) {
//            order.setReturnReason(reason);
//            order.setReturnRequestedAt(LocalDateTime.now());
//        }
//        orderRepository.save(order);
//
//        if (order.getUser() != null) {
//            String email = order.getUser().getEmail();
//            if (email != null) emailService.sendOrderStatusUpdateEmail(email, order.getOrderNumber(), newStatus.name());
//            notificationService.createOrderNotification(order.getUser(), order.getOrderNumber(), newStatus.name());
//        }
//        return toResponse(order);
//    }
//
//    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
//        Page<Order> orders = orderRepository.findByUserId(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
//        return PageResponse.of(orders.map(this::toResponse));
//    }
//
//    public PageResponse<OrderResponse> getAllOrders(String search, OrderStatus status, int page, int size) {
//        Page<Order> orders = orderRepository.searchOrders(search, status,
//                PageRequest.of(page, size, Sort.by("createdAt").descending()));
//        return PageResponse.of(orders.map(this::toResponse));
//    }
//
//    public OrderResponse getByOrderNumber(String orderNumber) {
//        Order order = orderRepository.findByOrderNumber(orderNumber)
//                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
//        return toResponse(order);
//    }
//
//    private BigDecimal calculateShipping(OrderRequest req, BigDecimal subtotal) {
//        List<ShippingZone> zones = shippingZoneRepository.findByActiveTrue();
//        if (zones.isEmpty()) return BigDecimal.valueOf(60); // default BDT 60
//
//        ShippingZone zone = zones.get(0);
//        if (zone.getFreeShippingThreshold() != null &&
//                subtotal.compareTo(zone.getFreeShippingThreshold()) >= 0)
//            return BigDecimal.ZERO;
//
//        return zone.getShippingCost();
//    }
//
//    private BigDecimal applyCoupon(Coupon coupon, BigDecimal subtotal) {
//        if (coupon.getMinOrderAmount() != null &&
//                subtotal.compareTo(coupon.getMinOrderAmount()) < 0)
//            throw new BadRequestException("Minimum order amount for this coupon is " + coupon.getMinOrderAmount());
//
//        return switch (coupon.getType()) {
//            case PERCENTAGE -> {
//                BigDecimal disc = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
//                if (coupon.getMaxDiscountAmount() != null)
//                    disc = disc.min(coupon.getMaxDiscountAmount());
//                yield disc;
//            }
//            case FIXED_AMOUNT -> coupon.getDiscountValue().min(subtotal);
//            case FREE_SHIPPING -> BigDecimal.ZERO; // handled in shipping calc
//            default -> BigDecimal.ZERO;
//        };
//    }
//
//    private String generateOrderNumber() {
//        // timestamp + random — collision practically impossible
//        return "ORD-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())
//                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//    }
//
//    private String toAddressJson(Address a) {
//        return "{\"name\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"district\":\"%s\"}"
//                .formatted(a.getFullName(), a.getPhone(), a.getStreetAddress(), a.getCity(), a.getDistrict());
//    }
//
//    private String toGuestAddressJson(OrderRequest.GuestInfo g) {
//        return "{\"name\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"district\":\"%s\"}"
//                .formatted(g.getFullName(), g.getPhone(), g.getStreetAddress(), g.getCity(), g.getDistrict());
//    }
//
//    public OrderResponse toResponse(Order o) {
//        List<OrderResponse.OrderItemResponse> items = o.getItems() == null ? List.of() :
//                o.getItems().stream().map(i -> OrderResponse.OrderItemResponse.builder()
//                        .id(i.getId()).productId(i.getProduct().getId())
//                        .productName(i.getProductName()).productImage(i.getProductImage())
//                        .size(i.getSize()).color(i.getColor()).sku(i.getSku())
//                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
//                        .totalPrice(i.getTotalPrice()).reviewed(i.isReviewed())
//                        .build()).collect(Collectors.toList());
//
//        return OrderResponse.builder()
//                .id(o.getId()).orderNumber(o.getOrderNumber()).status(o.getStatus())
//                .paymentMethod(o.getPaymentMethod()).paymentStatus(o.getPaymentStatus())
//                .subtotal(o.getSubtotal()).shippingCost(o.getShippingCost())
//                .discount(o.getDiscount()).tax(o.getTax()).totalAmount(o.getTotalAmount())
//                .couponCode(o.getCouponCode()).shippingAddress(o.getShippingAddress())
//                .trackingNumber(o.getTrackingNumber()).courierService(o.getCourierService())
//                .estimatedDelivery(o.getEstimatedDelivery()).deliveredAt(o.getDeliveredAt())
//                .orderNotes(o.getOrderNotes()).items(items).createdAt(o.getCreatedAt())
//                .build();
//    }
//}



























package com.ecommerce.service.impl;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final ShippingZoneRepository shippingZoneRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest req) {
        User user = null;
        Cart cart = null;
        String shippingAddressJson;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new BadRequestException("Cart is empty"));
            if (cart.getItems() == null || cart.getItems().isEmpty())
                throw new BadRequestException("Cart is empty");

            Address address = addressRepository.findById(req.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address", req.getAddressId()));
            shippingAddressJson = toAddressJson(address);
        } else {
            // Guest order
            if (req.getGuestInfo() == null) throw new BadRequestException("Guest info is required");
            shippingAddressJson = toGuestAddressJson(req.getGuestInfo());
        }

        List<CartItem> itemsToOrder = (cart != null)
                ? cart.getItems().stream().filter(i -> !i.isSavedForLater()).collect(Collectors.toList())
                : List.of();

        if (itemsToOrder.isEmpty() && userId != null)
            throw new BadRequestException("No items selected for order");

        BigDecimal subtotal = itemsToOrder.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingCost = calculateShipping(req, subtotal);

        BigDecimal discount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            coupon = couponRepository.findByCodeAndActiveTrue(req.getCouponCode())
                    .orElseThrow(() -> new BadRequestException("Invalid coupon code"));
            discount = applyCoupon(coupon, subtotal);
        }

        BigDecimal tax = subtotal.subtract(discount)
                .multiply(BigDecimal.valueOf(0.00));

        BigDecimal total = subtotal.add(shippingCost).subtract(discount).add(tax);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .guestEmail(req.getGuestInfo() != null ? req.getGuestInfo().getEmail() : null)
                .guestName(req.getGuestInfo() != null ? req.getGuestInfo().getFullName() : null)
                .guestPhone(req.getGuestInfo() != null ? req.getGuestInfo().getPhone() : null)
                .shippingAddress(shippingAddressJson)
                .paymentMethod(req.getPaymentMethod())
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .discount(discount)
                .tax(tax)
                .totalAmount(total)
                .couponCode(req.getCouponCode())
                .orderNotes(req.getOrderNotes())
                .estimatedDelivery(LocalDateTime.now().plusDays(3))
                .build();

        List<OrderItem> orderItems = itemsToOrder.stream().map(ci -> {
            Product p = ci.getProduct();
            if (p.getStock() < ci.getQuantity())
                throw new BadRequestException("Insufficient stock for: " + p.getName());

            p.setStock(p.getStock() - ci.getQuantity());
            p.setSoldCount(p.getSoldCount() + ci.getQuantity());

            String image = "";
            try {
                List<String> imgs = objectMapper.readValue(
                        p.getImages() != null ? p.getImages() : "[]", List.class);
                if (!imgs.isEmpty()) image = imgs.get(0);
            } catch (Exception ignored) {}

            return OrderItem.builder()
                    .order(order).product(p)
                    .productName(p.getName()).productImage(image)
                    .size(ci.getVariant() != null ? ci.getVariant().getSize() : null)
                    .color(ci.getVariant() != null ? ci.getVariant().getColor() : null)
                    .sku(ci.getVariant() != null ? ci.getVariant().getSku() : p.getSku())
                    .quantity(ci.getQuantity())
                    .unitPrice(ci.getUnitPrice())
                    .totalPrice(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build();
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        if (coupon != null) {
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
        }

        Order saved = orderRepository.save(order);

        if (cart != null) {
            cart.getItems().removeIf(i -> !i.isSavedForLater());
            cartRepository.save(cart);
        }

        String email = user != null ? user.getEmail()
                : (req.getGuestInfo() != null ? req.getGuestInfo().getEmail() : null);
        String name = user != null ? user.getFullName()
                : (req.getGuestInfo() != null ? req.getGuestInfo().getFullName() : "Customer");
        if (email != null) emailService.sendOrderConfirmationEmail(email, saved.getOrderNumber(), name);
        if (user != null) notificationService.createOrderNotification(user, saved.getOrderNumber(), "PLACED");

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) order.setCancelReason(reason);
        if (newStatus == OrderStatus.DELIVERED) order.setDeliveredAt(LocalDateTime.now());
        if (newStatus == OrderStatus.RETURN_REQUESTED) {
            order.setReturnReason(reason);
            order.setReturnRequestedAt(LocalDateTime.now());
        }
        orderRepository.save(order);

        if (order.getUser() != null) {
            String email = order.getUser().getEmail();
            if (email != null)
                emailService.sendOrderStatusUpdateEmail(email, order.getOrderNumber(), newStatus.name());
            notificationService.createOrderNotification(order.getUser(), order.getOrderNumber(), newStatus.name());
        }
        return toResponse(order);
    }

    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUserId(
                userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.of(orders.map(this::toResponse));
    }

    /**
     * Two-step fetch:
     *  1. Paginate order IDs in the DB (no collection join → no in-memory pagination warning).
     *  2. Hydrate only the current page's orders with their collections via a second query.
     */
    public PageResponse<OrderResponse> getAllOrders(String search, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // Step 1 — paginated query without entity graph (fixes bytea error + HHH90003004)
        Page<Order> orderPage = orderRepository.searchOrders(search, status, pageable);

        List<Long> ids = orderPage.getContent().stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return PageResponse.of(orderPage.map(this::toResponse));
        }

        // Step 2 — load full graph only for this page's IDs
        List<Order> hydrated = orderRepository.findByIdsWithItems(ids);

        // Preserve the original sort order from the paginated result
        Map<Long, Order> hydratedMap = hydrated.stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        List<Order> sorted = ids.stream()
                .map(hydratedMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<Order> hydratedPage = new PageImpl<>(sorted, pageable, orderPage.getTotalElements());
        return PageResponse.of(hydratedPage.map(this::toResponse));
    }

    public OrderResponse getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        return toResponse(order);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private BigDecimal calculateShipping(OrderRequest req, BigDecimal subtotal) {
        List<ShippingZone> zones = shippingZoneRepository.findByActiveTrue();
        if (zones.isEmpty()) return BigDecimal.valueOf(60);

        ShippingZone zone = zones.get(0);
        if (zone.getFreeShippingThreshold() != null &&
                subtotal.compareTo(zone.getFreeShippingThreshold()) >= 0)
            return BigDecimal.ZERO;

        return zone.getShippingCost();
    }

    private BigDecimal applyCoupon(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getMinOrderAmount() != null &&
                subtotal.compareTo(coupon.getMinOrderAmount()) < 0)
            throw new BadRequestException(
                    "Minimum order amount for this coupon is " + coupon.getMinOrderAmount());

        return switch (coupon.getType()) {
            case PERCENTAGE -> {
                BigDecimal disc = subtotal.multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
                if (coupon.getMaxDiscountAmount() != null)
                    disc = disc.min(coupon.getMaxDiscountAmount());
                yield disc;
            }
            case FIXED_AMOUNT -> coupon.getDiscountValue().min(subtotal);
            case FREE_SHIPPING -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    private String generateOrderNumber() {
        return "ORD-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String toAddressJson(Address a) {
        return "{\"name\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"district\":\"%s\"}"
                .formatted(a.getFullName(), a.getPhone(), a.getStreetAddress(), a.getCity(), a.getDistrict());
    }

    private String toGuestAddressJson(OrderRequest.GuestInfo g) {
        return "{\"name\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"district\":\"%s\"}"
                .formatted(g.getFullName(), g.getPhone(), g.getStreetAddress(), g.getCity(), g.getDistrict());
    }

    public OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> items = o.getItems() == null ? List.of() :
                o.getItems().stream().map(i -> OrderResponse.OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProductName())
                        .productImage(i.getProductImage())
                        .size(i.getSize()).color(i.getColor()).sku(i.getSku())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .reviewed(i.isReviewed())
                        .build()).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId()).orderNumber(o.getOrderNumber()).status(o.getStatus())
                .paymentMethod(o.getPaymentMethod()).paymentStatus(o.getPaymentStatus())
                .subtotal(o.getSubtotal()).shippingCost(o.getShippingCost())
                .discount(o.getDiscount()).tax(o.getTax()).totalAmount(o.getTotalAmount())
                .couponCode(o.getCouponCode()).shippingAddress(o.getShippingAddress())
                .trackingNumber(o.getTrackingNumber()).courierService(o.getCourierService())
                .estimatedDelivery(o.getEstimatedDelivery()).deliveredAt(o.getDeliveredAt())
                .orderNotes(o.getOrderNotes()).items(items).createdAt(o.getCreatedAt())
                .build();
    }
}
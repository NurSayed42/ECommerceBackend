package com.ecommerce.service.impl;

import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${sslcommerz.store-id}")
    private String storeId;

    @Value("${sslcommerz.store-passwd}")
    private String storePassword;

    @Value("${sslcommerz.base-url}")
    private String baseUrl;

    @Value("${sslcommerz.success-url}")
    private String successUrl;

    @Value("${sslcommerz.fail-url}")
    private String failUrl;

    @Value("${sslcommerz.cancel-url}")
    private String cancelUrl;

    /**
     * Initiate SSLCommerz payment session
     */
    @Transactional
    public String initiateSSLCommerz(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .transactionId(transactionId)
                .method(PaymentMethod.SSLCOMMERZ)
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .build();
        paymentRepository.save(payment);

        // Build SSLCommerz request
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("store_id", storeId);
        params.add("store_passwd", storePassword);
        params.add("total_amount", order.getTotalAmount().toString());
        params.add("currency", "BDT");
        params.add("tran_id", transactionId);
        params.add("success_url", successUrl);
        params.add("fail_url", failUrl);
        params.add("cancel_url", cancelUrl);

        // Customer info
        String name = order.getUser() != null ? order.getUser().getFullName() : order.getGuestName();
        String email = order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail();
        String phone = order.getUser() != null ? order.getUser().getPhone() : order.getGuestPhone();

        params.add("cus_name", name != null ? name : "Customer");
        params.add("cus_email", email != null ? email : "customer@example.com");
        params.add("cus_phone", phone != null ? phone : "01700000000");
        params.add("cus_add1", "Bangladesh");
        params.add("cus_city", "Dhaka");
        params.add("cus_country", "Bangladesh");

        params.add("product_name", "Order " + order.getOrderNumber());
        params.add("product_category", "E-commerce");
        params.add("product_profile", "general");

        params.add("shipping_method", "NO");
        params.add("num_of_item", String.valueOf(order.getItems().size()));

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/gwprocess/v4/api.php", request, Map.class);

            if (response.getBody() != null && "SUCCESS".equals(response.getBody().get("status"))) {
                return (String) response.getBody().get("GatewayPageURL");
            }
        } catch (Exception e) {
            log.error("SSLCommerz init failed: {}", e.getMessage());
        }
        throw new BadRequestException("Payment gateway initialization failed");
    }

    /**
     * Handle SSLCommerz success callback
     */
    @Transactional
    public void handleSSLCommerzSuccess(Map<String, String> params) {
        String transactionId = params.get("tran_id");
        String status = params.get("status");

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + transactionId));

        if ("VALID".equals(status) || "VALIDATED".equals(status)) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setGatewayTransactionId(params.get("bank_tran_id"));
            payment.setGatewayResponse(params.toString());

            Order order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaidAmount(payment.getAmount());
            orderRepository.save(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(params.getOrDefault("failedreason", "Unknown"));
        }
        paymentRepository.save(payment);
    }

    /**
     * Process refund
     */
    @Transactional
    public void processRefund(Long orderId, BigDecimal amount, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        orderRepository.save(order);
        log.info("Refund processed for order {} - Amount: {} - Reason: {}", order.getOrderNumber(), amount, reason);
    }
}

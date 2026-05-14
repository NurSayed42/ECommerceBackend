package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.service.impl.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/sslcommerz/init/{orderId}")
    public ResponseEntity<ApiResponse<String>> initSSLCommerz(@PathVariable Long orderId) {
        String gatewayUrl = paymentService.initiateSSLCommerz(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment initiated", gatewayUrl));
    }

    @PostMapping("/sslcommerz/success")
    public String handleSuccess(@RequestParam Map<String, String> params) {
        paymentService.handleSSLCommerzSuccess(params);
        return "redirect:http://localhost:3000/order/success";
    }

    @PostMapping("/sslcommerz/fail")
    public String handleFail(@RequestParam Map<String, String> params) {
        return "redirect:http://localhost:3000/order/failed";
    }

    @PostMapping("/sslcommerz/cancel")
    public String handleCancel(@RequestParam Map<String, String> params) {
        return "redirect:http://localhost:3000/cart";
    }
}

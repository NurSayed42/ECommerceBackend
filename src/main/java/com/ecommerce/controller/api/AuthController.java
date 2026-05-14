//package com.ecommerce.controller.api;
//
//import com.ecommerce.dto.request.*;
//import com.ecommerce.dto.response.*;
//import com.ecommerce.service.impl.AuthService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//@Tag(name = "Authentication", description = "Auth endpoints")
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/register")
//    @Operation(summary = "Register new user")
//    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
//        return ResponseEntity.ok(ApiResponse.success(authService.register(req)));
//    }
//
//    @PostMapping("/verify-otp")
//    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestBody OtpVerifyRequest req) {
//        return ResponseEntity.ok(ApiResponse.success(authService.verifyOtp(req)));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
//        return ResponseEntity.ok(ApiResponse.success(authService.login(req)));
//    }
//
//    @PostMapping("/refresh-token")
//    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String token) {
//        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(token)));
//    }
//
//    @PostMapping("/forgot-password")
//    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String identifier) {
//        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(identifier)));
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<ApiResponse<String>> resetPassword(
//            @RequestParam String identifier,
//            @RequestParam String otp,
//            @RequestParam String newPassword) {
//        return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(identifier, otp, newPassword)));
//    }
//}










package com.ecommerce.controller.api;

import com.ecommerce.dto.request.*;
import com.ecommerce.dto.response.*;
import com.ecommerce.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(req)));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.verifyOtp(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(req)));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank())
            return ResponseEntity.badRequest().body(ApiResponse.error("idToken is required"));
        return ResponseEntity.ok(ApiResponse.success(authService.googleLogin(idToken)));
    }

    @PostMapping("/send-verification-otp")
    public ResponseEntity<ApiResponse<String>> sendVerificationOtp(@RequestParam String identifier) {
        return ResponseEntity.ok(ApiResponse.success(authService.sendVerificationOtp(identifier)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(token)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String identifier) {
        return ResponseEntity.ok(ApiResponse.success(authService.forgotPassword(identifier)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String identifier,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(ApiResponse.success(authService.resetPassword(identifier, otp, newPassword)));
    }
}
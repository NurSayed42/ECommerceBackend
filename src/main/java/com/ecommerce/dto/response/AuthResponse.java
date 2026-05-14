package com.ecommerce.dto.response;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private boolean emailVerified;
    private String profileImage;
    private List<String> roles;
}

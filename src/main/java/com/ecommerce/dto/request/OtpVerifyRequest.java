package com.ecommerce.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank private String identifier; // email or phone
    @NotBlank private String otp;
}

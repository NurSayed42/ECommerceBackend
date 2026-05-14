package com.ecommerce.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min=2, max=100)
    private String fullName;
    private String email;
    private String phone;
    @NotBlank @Size(min=6, max=100)
    private String password;
}

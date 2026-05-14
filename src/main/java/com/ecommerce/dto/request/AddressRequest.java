package com.ecommerce.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank private String fullName;
    @NotBlank private String phone;
    @NotBlank private String streetAddress;
    @NotBlank private String city;
    @NotBlank private String district;
    private String postalCode;
    private String type = "HOME";
    private boolean defaultAddress;
}

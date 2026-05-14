package com.ecommerce.controller.api;

import com.ecommerce.dto.request.AddressRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Address;
import com.ecommerce.model.User;
import com.ecommerce.repository.AddressRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        user.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String profileImage) {
        User user = getUser(ud);
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName);
        if (profileImage != null) user.setProfileImage(profileImage);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", null));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        User user = getUser(ud);
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Current password is incorrect"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PostMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivate(@AuthenticationPrincipal UserDetails ud) {
        User user = getUser(ud);
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Account deactivated", null));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<Address>>> getAddresses(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(addressRepository.findByUserId(getUser(ud).getId())));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<Address>> addAddress(
            @AuthenticationPrincipal UserDetails ud,
            @RequestBody AddressRequest req) {
        User user = getUser(ud);
        if (req.isDefaultAddress()) {
            addressRepository.findByUserId(user.getId())
                    .forEach(a -> { a.setDefaultAddress(false); addressRepository.save(a); });
        }
        Address addr = Address.builder()
                .user(user)
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .streetAddress(req.getStreetAddress())
                .city(req.getCity())
                .district(req.getDistrict())
                .postalCode(req.getPostalCode())
                .type(req.getType())
                .defaultAddress(req.isDefaultAddress())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Address added", addressRepository.save(addr)));
    }

    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(
            @AuthenticationPrincipal UserDetails ud,
            @PathVariable Long id) {
        addressRepository.deleteByIdAndUserId(id, getUser(ud).getId());
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    // null return এর বদলে exception throw করো
    private User getUser(UserDetails ud) {
        if (ud == null) throw new ResourceNotFoundException("User not authenticated");
        return userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ud.getUsername()));
    }
}
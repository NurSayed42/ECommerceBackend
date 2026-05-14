//package com.ecommerce.service.impl;
//
//import com.ecommerce.dto.request.LoginRequest;
//import com.ecommerce.dto.request.OtpVerifyRequest;
//import com.ecommerce.dto.request.RegisterRequest;
//import com.ecommerce.dto.response.AuthResponse;
//import com.ecommerce.enums.RoleName;
//import com.ecommerce.exception.BadRequestException;
//import com.ecommerce.exception.ResourceNotFoundException;
//import com.ecommerce.model.Role;
//import com.ecommerce.model.User;
//import com.ecommerce.repository.RoleRepository;
//import com.ecommerce.repository.UserRepository;
//import com.ecommerce.security.JwtUtils;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Random;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtils jwtUtils;
//    private final AuthenticationManager authenticationManager;
//    private final EmailService emailService;
//
//    @Transactional
//    public String register(RegisterRequest request) {
//        if (request.getEmail() == null && request.getPhone() == null)
//            throw new BadRequestException("Email or phone is required");
//
//        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail()))
//            throw new BadRequestException("Email already registered");
//
//        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone()))
//            throw new BadRequestException("Phone already registered");
//
//        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
//                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
//
//        String otp = generateOtp();
//        User user = User.builder()
//                .fullName(request.getFullName())
//                .email(request.getEmail())
//                .phone(request.getPhone())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .roles(Set.of(userRole))
//                .otp(otp)
//                .otpExpiry(LocalDateTime.now().plusMinutes(5))
//                .build();
//
//        userRepository.save(user);
//
//        // Send OTP
//        if (request.getEmail() != null) {
//            emailService.sendOtpEmail(request.getEmail(), otp);
//        }
//        // SMS would go here for phone
//
//        return "Registration successful. Please verify your OTP.";
//    }
//
//    @Transactional
//    public String verifyOtp(OtpVerifyRequest request) {
//        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp()))
//            throw new BadRequestException("Invalid OTP");
//
//        if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
//            throw new BadRequestException("OTP has expired");
//
//        if (user.getEmail() != null && user.getEmail().equals(request.getIdentifier())) {
//            user.setEmailVerified(true);
//        } else {
//            user.setPhoneVerified(true);
//        }
//        user.setOtp(null);
//        user.setOtpExpiry(null);
//        userRepository.save(user);
//
//        return "OTP verified successfully";
//    }
//
//    public AuthResponse login(LoginRequest request) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        User user = userRepository.findByEmailOrPhone(request.getUsername(), request.getUsername())
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        user.setLastLoginAt(LocalDateTime.now());
//        userRepository.save(user);
//
//        String username = user.getEmail() != null ? user.getEmail() : user.getPhone();
//        return buildAuthResponse(user, username);
//    }
//
//    public AuthResponse refreshToken(String refreshToken) {
//        if (!jwtUtils.validateToken(refreshToken))
//            throw new BadRequestException("Invalid refresh token");
//
//        String email = jwtUtils.getEmailFromToken(refreshToken);
//        User user = userRepository.findByEmailOrPhone(email, email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        return buildAuthResponse(user, email);
//    }
//
//    @Transactional
//    public String forgotPassword(String identifier) {
//        User user = userRepository.findByEmailOrPhone(identifier, identifier)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        String otp = generateOtp();
//        user.setOtp(otp);
//        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
//        userRepository.save(user);
//
//        if (user.getEmail() != null) emailService.sendPasswordResetEmail(user.getEmail(), otp);
//        return "OTP sent for password reset";
//    }
//
//    @Transactional
//    public String resetPassword(String identifier, String otp, String newPassword) {
//        User user = userRepository.findByEmailOrPhone(identifier, identifier)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        if (!otp.equals(user.getOtp()) || user.getOtpExpiry().isBefore(LocalDateTime.now()))
//            throw new BadRequestException("Invalid or expired OTP");
//
//        user.setPassword(passwordEncoder.encode(newPassword));
//        user.setOtp(null);
//        user.setOtpExpiry(null);
//        userRepository.save(user);
//        return "Password reset successful";
//    }
//
//    private AuthResponse buildAuthResponse(User user, String username) {
//        String accessToken = jwtUtils.generateAccessToken(username);
//        String refreshToken = jwtUtils.generateRefreshToken(username);
//        List<String> roles = user.getRoles().stream()
//                .map(r -> r.getName().name()).collect(Collectors.toList());
//
//        return AuthResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .userId(user.getId())
//                .fullName(user.getFullName())
//                .email(user.getEmail())
//                .phone(user.getPhone())
//                .profileImage(user.getProfileImage())
//                .roles(roles)
//                .build();
//    }
//
//    private String generateOtp() {
//        return String.format("%06d", new Random().nextInt(999999));
//    }
//}




package com.ecommerce.service.impl;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.OtpVerifyRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.enums.RoleName;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    // Google Client ID — application.properties এ রাখো
    @org.springframework.beans.factory.annotation.Value("${google.client-id:}")
    private String googleClientId;

    @Transactional
    public String register(RegisterRequest request) {
        if (request.getEmail() == null && request.getPhone() == null)
            throw new BadRequestException("Email or phone is required");
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email already registered");
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone()))
            throw new BadRequestException("Phone already registered");

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        String otp = generateOtp();
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5))
                .build();

        userRepository.save(user);
        if (request.getEmail() != null) emailService.sendOtpEmail(request.getEmail(), otp);
        return "Registration successful. Please verify your OTP.";
    }

    @Transactional
    public String verifyOtp(OtpVerifyRequest request) {
        User user = userRepository.findByEmailOrPhone(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp()))
            throw new BadRequestException("Invalid OTP");
        if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
            throw new BadRequestException("OTP has expired");
        if (user.getEmail() != null && user.getEmail().equals(request.getIdentifier())) {
            user.setEmailVerified(true);
        } else {
            user.setPhoneVerified(true);
        }
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "OTP verified successfully";
    }

    @Transactional
    public String sendVerificationOtp(String identifier) {
        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        if (user.getEmail() != null) emailService.sendOtpEmail(user.getEmail(), otp);
        return "OTP sent to " + identifier;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmailOrPhone(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String username = user.getEmail() != null ? user.getEmail() : user.getPhone();
        return buildAuthResponse(user, username);
    }

    // ─── GOOGLE LOGIN ───────────────────────────────────────────────
    @Transactional
    public AuthResponse googleLogin(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) throw new BadRequestException("Invalid Google token");

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name  = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            String googleId = payload.getSubject();

            // User আছে কিনা check করো
            User user = userRepository.findByEmailOrPhone(email, email).orElse(null);

            if (user == null) {
                // নতুন user তৈরি করো — Google দিয়ে এলে auto-verified
                Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                        .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
                user = User.builder()
                        .fullName(name)
                        .email(email)
                        .emailVerified(true) // Google verified
                        .profileImage(picture)
                        .provider("GOOGLE")
                        .providerId(googleId)
                        .password(passwordEncoder.encode(generateOtp() + googleId)) // dummy password
                        .roles(Set.of(userRole))
                        .active(true)
                        .build();
            } else {
                // আগে থেকে আছে — Google id update করো এবং verified করো
                user.setEmailVerified(true);
                user.setProvider("GOOGLE");
                user.setProviderId(googleId);
                if (picture != null && user.getProfileImage() == null) user.setProfileImage(picture);
            }

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            return buildAuthResponse(user, email);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google login error", e);
            throw new BadRequestException("Google authentication failed");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken))
            throw new BadRequestException("Invalid refresh token");
        String email = jwtUtils.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmailOrPhone(email, email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return buildAuthResponse(user, email);
    }

    @Transactional
    public String forgotPassword(String identifier) {
        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        if (user.getEmail() != null) emailService.sendPasswordResetEmail(user.getEmail(), otp);
        return "OTP sent for password reset";
    }

    @Transactional
    public String resetPassword(String identifier, String otp, String newPassword) {
        User user = userRepository.findByEmailOrPhone(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!otp.equals(user.getOtp()) || user.getOtpExpiry().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Invalid or expired OTP");
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "Password reset successful";
    }

    private AuthResponse buildAuthResponse(User user, String username) {
        String accessToken  = jwtUtils.generateAccessToken(username);
        String refreshToken = jwtUtils.generateRefreshToken(username);
        List<String> roles  = user.getRoles().stream()
                .map(r -> r.getName().name()).collect(Collectors.toList());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .emailVerified(user.isEmailVerified())
                .roles(roles)
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
package com.ecommerce.config;

import com.ecommerce.enums.RoleName;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed Roles
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Created role: {}", roleName);
            }
        }

        // Seed Super Admin
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN).orElseThrow();
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();

            User admin = User.builder()
                    .fullName("Super Admin")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("admin123"))
                    .emailVerified(true)
                    .active(true)
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("✅ Super Admin created: admin@ecommerce.com / admin123");
        }
    }
}

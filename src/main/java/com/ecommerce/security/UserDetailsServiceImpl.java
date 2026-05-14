package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseGet(() -> userRepository.findByPhone(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));

        if (!user.isActive()) throw new UsernameNotFoundException("Account is deactivated");
        if (user.isBlocked()) throw new UsernameNotFoundException("Account is blocked");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail() != null ? user.getEmail() : user.getPhone())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName().name()))
                        .collect(Collectors.toList()))
                .build();
    }
}

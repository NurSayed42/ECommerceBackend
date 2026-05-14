////package com.ecommerce.config;
////
////import com.ecommerce.security.JwtAuthFilter;
////import lombok.RequiredArgsConstructor;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.authentication.AuthenticationManager;
////import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
////import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
////import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.core.userdetails.UserDetailsService;
////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////import org.springframework.web.cors.CorsConfiguration;
////import org.springframework.web.cors.CorsConfigurationSource;
////import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
////import java.util.Arrays;
////import java.util.List;
////
////@Configuration
////@EnableWebSecurity
////@EnableMethodSecurity
////@RequiredArgsConstructor
////public class SecurityConfig {
////
////    private final JwtAuthFilter jwtAuthFilter;
////    private final UserDetailsService userDetailsService;
////
////    private static final String[] PUBLIC_URLS = {
////            "/api/v1/auth/**",
////            "/api/v1/products/**",
////            "/api/v1/categories/**",
////            "/api/v1/banners/**",
////            "/api/v1/payments/sslcommerz/**",
////            "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
////            "/actuator/health"
////    };
////
////    private static final String[] ADMIN_URLS = {
////            "/admin/**"
////    };
////
////    @Bean
////    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
////        http
////            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
////            .csrf(csrf -> csrf.disable())
////            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
////            .authorizeHttpRequests(auth -> auth
////                .requestMatchers(PUBLIC_URLS).permitAll()
////                .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
////                .requestMatchers("/api/v1/**").authenticated()
////                .anyRequest().permitAll()
////            )
////            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
////        return http.build();
////    }
////
////    @Bean
////    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
////        http
////            .securityMatcher("/admin/**")
////            .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/api/**"))
////            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
////            .authorizeHttpRequests(auth -> auth
////                .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**").permitAll()
////                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MODERATOR")
////                .anyRequest().authenticated()
////            )
////            .formLogin(form -> form
////                .loginPage("/admin/login")
////                .loginProcessingUrl("/admin/login")
////                .defaultSuccessUrl("/admin/dashboard", true)
////                .failureUrl("/admin/login?error=true")
////                .usernameParameter("email")
////                .passwordParameter("password")
////            )
////            .logout(logout -> logout
////                .logoutUrl("/admin/logout")
////                .logoutSuccessUrl("/admin/login?logout=true")
////                .invalidateHttpSession(true)
////                .deleteCookies("JSESSIONID")
////            )
////            .authenticationProvider(daoAuthenticationProvider());
////        return http.build();
////    }
////
////    @Bean
////    public DaoAuthenticationProvider daoAuthenticationProvider() {
////        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
////        provider.setUserDetailsService(userDetailsService);
////        provider.setPasswordEncoder(passwordEncoder());
////        return provider;
////    }
////
////    @Bean
////    public PasswordEncoder passwordEncoder() {
////        return new BCryptPasswordEncoder(12);
////    }
////
////    @Bean
////    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
////        return config.getAuthenticationManager();
////    }
////
////    @Bean
////    public CorsConfigurationSource corsConfigurationSource() {
////        CorsConfiguration config = new CorsConfiguration();
////        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
////        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
////        config.setAllowedHeaders(List.of("*"));
////        config.setAllowCredentials(true);
////        config.setMaxAge(3600L);
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        source.registerCorsConfiguration("/**", config);
////        return source;
////    }
////}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//package com.ecommerce.config;
//
//import com.ecommerce.security.JwtAuthFilter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthFilter jwtAuthFilter;
//    private final UserDetailsService userDetailsService;
//
//    private static final String[] PUBLIC_URLS = {
//            "/api/v1/auth/**",
//            "/api/v1/products/**",
//            "/api/v1/categories/**",
//            "/api/v1/banners/**",
//            "/api/v1/reviews/product/**",
//            "/api/v1/payments/sslcommerz/**",
//            "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
//            "/actuator/health"
//    };
//
//    // API Security Chain - Order 1 (highest priority)
//    @Bean
//    @Order(1)
//    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/api/**", "/actuator/**", "/api-docs/**", "/swagger-ui/**")
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(PUBLIC_URLS).permitAll()
//                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
//                        .requestMatchers("/api/v1/**").authenticated()
//                        .anyRequest().permitAll()
//                )
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//        return http.build();
//    }
//
//    // Admin Panel Security Chain - Order 2
//    @Bean
//    @Order(2)
//    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/admin/**")
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/api/**"))
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**", "/admin/images/**", "/admin/favicon.ico").permitAll()
//                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MODERATOR")
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/admin/login")
//                        .loginProcessingUrl("/admin/login")
//                        .defaultSuccessUrl("/admin/dashboard", true)
//                        .failureUrl("/admin/login?error=true")
//                        .usernameParameter("email")
//                        .passwordParameter("password")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/admin/logout")
//                        .logoutSuccessUrl("/admin/login?logout=true")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll()
//                )
//                .authenticationProvider(daoAuthenticationProvider());
//        return http.build();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
//        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//        config.setMaxAge(3600L);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//}






















package com.ecommerce.config;

import com.ecommerce.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/api/v1/products/**",
            "/api/v1/categories/**",
            "/api/v1/banners/**",
            "/api/v1/reviews/product/**",
            "/api/v1/payments/sslcommerz/**",
            "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/actuator/health"
    };

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/actuator/**", "/api-docs/**", "/swagger-ui/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                // ← এটাই সবচেয়ে গুরুত্বপূর্ণ fix: 403 এর বদলে 401 পাঠাবে
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.getWriter().write(
                                    "{\"success\":false,\"status\":401,\"message\":\"Unauthorized - Please login\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            response.getWriter().write(
                                    "{\"success\":false,\"status\":403,\"message\":\"Access denied\"}"
                            );
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.ignoringRequestMatchers("/admin/api/**"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**",
                                "/admin/images/**", "/admin/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MODERATOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(daoAuthenticationProvider());
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:3001",
            "https://e-commerce-frontend-five-snowy.vercel.app",
            "https://e-commerce-frontend-git-main-nur-sayeds-projects.vercel.app",
            "https://e-commerce-frontend-ftba9fpqx-nur-sayeds-projects.vercel.app"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

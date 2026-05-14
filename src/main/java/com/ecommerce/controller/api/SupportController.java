package com.ecommerce.controller.api;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.enums.TicketStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.SupportTicket;
import com.ecommerce.model.User;
import com.ecommerce.repository.SupportTicketRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<SupportTicket>> createTicket(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam String subject,
            @RequestParam String message,
            @RequestParam(defaultValue = "GENERAL") String category,
            @RequestParam(defaultValue = "NORMAL") String priority) {
        User user = userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername()).orElseThrow();
        String ticketNum = "TKT-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())
                + "-" + String.format("%04d", new Random().nextInt(9999));
        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(ticketNum).user(user).subject(subject)
                .message(message).category(category).priority(priority)
                .build();
        return ResponseEntity.ok(ApiResponse.success("Ticket created", ticketRepository.save(ticket)));
    }

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicket>>> myTickets(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0") int page) {
        Long userId = userRepository.findByEmailOrPhone(ud.getUsername(), ud.getUsername())
                .map(u -> u.getId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(
                ticketRepository.findByUserId(userId, PageRequest.of(page, 10, Sort.by("createdAt").descending()))));
    }
}

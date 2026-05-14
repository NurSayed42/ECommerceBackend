package com.ecommerce.controller.admin;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.TicketStatus;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATOR')")
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final SupportTicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        // Stats cards
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("todayOrders", orderRepository.countTodayOrders());
        model.addAttribute("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        model.addAttribute("deliveredOrders", orderRepository.countByStatus(OrderStatus.DELIVERED));
        model.addAttribute("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));

        model.addAttribute("totalRevenue", paymentRepository.getTotalRevenue());
        model.addAttribute("todayRevenue", orderRepository.getTodayRevenue());

        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("activeProducts", productRepository.countByActiveTrue());
        model.addAttribute("lowStockProducts", productRepository.findLowStockProducts().size());

        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("todayUsers", userRepository.countTodayRegistrations());
        model.addAttribute("blockedUsers", userRepository.countByBlocked(true));

        model.addAttribute("pendingReviews", reviewRepository.countApprovedByProductId(0L)); // approximate
        model.addAttribute("openTickets", ticketRepository.countByStatus(TicketStatus.OPEN));

        // Daily revenue for chart (last 30 days)
        List<Object[]> dailyRevenue = orderRepository.getDailyRevenue(LocalDateTime.now().minusDays(30));
        model.addAttribute("dailyRevenue", dailyRevenue);

        // Order status breakdown
        List<Object[]> orderStatusData = orderRepository.countByStatusGrouped();
        model.addAttribute("orderStatusData", orderStatusData);

        // Low stock alert
        model.addAttribute("lowStockList", productRepository.findLowStockProducts());

        return "admin/dashboard";
    }

//    @GetMapping("/login")
//    public String loginPage() {
//        return "admin/auth/login";
//    }
}

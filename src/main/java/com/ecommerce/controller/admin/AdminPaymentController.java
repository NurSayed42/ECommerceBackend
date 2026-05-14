package com.ecommerce.controller.admin;

import com.ecommerce.model.Payment;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.service.impl.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue = "0") int page) {
        Page<Payment> payments = paymentRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, 20));
        model.addAttribute("payments", payments);
        model.addAttribute("totalRevenue", paymentRepository.getTotalRevenue());
        return "admin/payments/list";
    }

    @PostMapping("/{orderId}/refund")
    public String refund(@PathVariable Long orderId,
                         @RequestParam BigDecimal amount,
                         @RequestParam String reason,
                         RedirectAttributes ra) {
        try {
            paymentService.processRefund(orderId, amount, reason);
            ra.addFlashAttribute("success", "Refund processed successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/payments";
    }
}

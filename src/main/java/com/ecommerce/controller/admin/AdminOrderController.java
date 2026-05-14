package com.ecommerce.controller.admin;

import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.dto.response.PageResponse;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.service.impl.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATOR')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) OrderStatus status) {
        PageResponse<OrderResponse> orders = orderService.getAllOrders(q, status, page, size);
        model.addAttribute("orders", orders);
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }

    @GetMapping("/{orderNumber}")
    public String detail(@PathVariable String orderNumber, Model model) {
        model.addAttribute("order", orderService.getByOrderNumber(orderNumber));
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam OrderStatus status,
                               @RequestParam(required = false) String reason,
                               RedirectAttributes ra) {
        try {
            orderService.updateStatus(id, status, reason);
            ra.addFlashAttribute("success", "Order status updated to " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}

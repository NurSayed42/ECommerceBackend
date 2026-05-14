package com.ecommerce.controller.admin;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String q) {

        String keyword = (q == null || q.isBlank()) ? "" : q.trim(); // ✅ add this

        Page<User> users = userRepository.searchUsers(keyword, PageRequest.of(page, size, Sort.by("createdAt").descending())); // ✅ q → keyword
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User u = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", u);
        return "admin/users/detail";
    }

    @PostMapping("/{id}/block")
    public String blockUser(@PathVariable Long id, @RequestParam String reason, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        u.setBlocked(true);
        u.setBlockReason(reason);
        userRepository.save(u);
        ra.addFlashAttribute("success", "User blocked");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unblock")
    public String unblockUser(@PathVariable Long id, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        u.setBlocked(false);
        u.setBlockReason(null);
        userRepository.save(u);
        ra.addFlashAttribute("success", "User unblocked");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/flag-fraud")
    public String flagFraud(@PathVariable Long id, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        u.setFraudFlag(!u.isFraudFlag());
        userRepository.save(u);
        ra.addFlashAttribute("success", "Fraud flag updated");
        return "redirect:/admin/users";
    }
}

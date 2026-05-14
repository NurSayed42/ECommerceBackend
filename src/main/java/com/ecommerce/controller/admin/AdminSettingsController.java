package com.ecommerce.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminSettingsController {

    @GetMapping
    public String settings(Model model) {
        return "admin/settings/index";
    }

    @PostMapping("/save")
    public String save(RedirectAttributes ra) {
        ra.addFlashAttribute("success", "Settings saved successfully!");
        return "redirect:/admin/settings";
    }
}

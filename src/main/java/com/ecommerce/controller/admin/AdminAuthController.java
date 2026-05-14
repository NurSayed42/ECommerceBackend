package com.ecommerce.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // কোনো @PreAuthorize নেই!
public class AdminAuthController {

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/auth/login";
    }
}

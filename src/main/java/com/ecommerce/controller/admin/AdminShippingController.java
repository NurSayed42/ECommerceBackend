package com.ecommerce.controller.admin;

import com.ecommerce.model.ShippingZone;
import com.ecommerce.repository.ShippingZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shipping")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminShippingController {

    private final ShippingZoneRepository shippingZoneRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("zones", shippingZoneRepository.findAll());
        model.addAttribute("newZone", new ShippingZone());
        return "admin/shipping/list";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute ShippingZone zone, RedirectAttributes ra) {
        shippingZoneRepository.save(zone);
        ra.addFlashAttribute("success", "Shipping zone created!");
        return "redirect:/admin/shipping";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        shippingZoneRepository.deleteById(id);
        ra.addFlashAttribute("success", "Shipping zone deleted!");
        return "redirect:/admin/shipping";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        ShippingZone z = shippingZoneRepository.findById(id).orElseThrow();
        z.setActive(!z.isActive());
        shippingZoneRepository.save(z);
        ra.addFlashAttribute("success", "Zone status updated!");
        return "redirect:/admin/shipping";
    }
}

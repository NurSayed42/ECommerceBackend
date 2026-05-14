package com.ecommerce.controller.admin;

import com.ecommerce.dto.request.CouponRequest;
import com.ecommerce.enums.CouponType;
import com.ecommerce.model.Coupon;
import com.ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCouponController {

    private final CouponRepository couponRepository;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("coupons", couponRepository.findAll(PageRequest.of(page, 20, Sort.by("createdAt").descending())));
        return "admin/coupons/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("coupon", new CouponRequest());
        model.addAttribute("types", CouponType.values());
        return "admin/coupons/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute CouponRequest req, RedirectAttributes ra) {
        try {
            Coupon c = new Coupon();
            c.setCode(req.getCode().toUpperCase());
            c.setDescription(req.getDescription());
            c.setType(req.getType());
            c.setDiscountValue(req.getDiscountValue());
            c.setMinOrderAmount(req.getMinOrderAmount());
            c.setMaxDiscountAmount(req.getMaxDiscountAmount());
            c.setUsageLimit(req.getUsageLimit());
            c.setPerUserLimit(req.getPerUserLimit());
            c.setStartDate(req.getStartDate());
            c.setEndDate(req.getEndDate());
            c.setActive(req.isActive());
            couponRepository.save(c);
            ra.addFlashAttribute("success", "Coupon created!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Coupon c = couponRepository.findById(id).orElseThrow();
        c.setActive(!c.isActive());
        couponRepository.save(c);
        ra.addFlashAttribute("success", "Coupon status changed");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        couponRepository.deleteById(id);
        ra.addFlashAttribute("success", "Coupon deleted");
        return "redirect:/admin/coupons";
    }
}

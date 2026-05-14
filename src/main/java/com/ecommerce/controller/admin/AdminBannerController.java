package com.ecommerce.controller.admin;

import com.ecommerce.model.Banner;
import com.ecommerce.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminBannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerRepository.findByActiveTrueOrderBySortOrderAsc());
        model.addAttribute("allBanners", bannerRepository.findAll());
        return "admin/banners/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("banner", new Banner());
        return "admin/banners/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Banner banner, RedirectAttributes ra) {
        bannerRepository.save(banner);
        ra.addFlashAttribute("success", "Banner created!");
        return "redirect:/admin/banners";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("banner", bannerRepository.findById(id).orElseThrow());
        return "admin/banners/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Banner banner, RedirectAttributes ra) {
        banner.setId(id);
        bannerRepository.save(banner);
        ra.addFlashAttribute("success", "Banner updated!");
        return "redirect:/admin/banners";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        bannerRepository.deleteById(id);
        ra.addFlashAttribute("success", "Banner deleted!");
        return "redirect:/admin/banners";
    }
}

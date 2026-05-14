package com.ecommerce.controller.admin;

import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.text.Normalizer;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("parents", categoryRepository.findByParentIsNullAndActiveTrue());
        model.addAttribute("newCategory", new Category());
        return "admin/categories/list";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Category category,
                         @RequestParam(required = false) Long parentId,
                         RedirectAttributes ra) {
        try {
            if (parentId != null) {
                Category parent = categoryRepository.findById(parentId).orElseThrow();
                category.setParent(parent);
            }
            category.setSlug(generateSlug(category.getName()));
            categoryRepository.save(category);
            ra.addFlashAttribute("success", "Category created!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryRepository.deleteById(id);
        ra.addFlashAttribute("success", "Category deleted!");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        Category c = categoryRepository.findById(id).orElseThrow();
        c.setActive(!c.isActive());
        categoryRepository.save(c);
        ra.addFlashAttribute("success", "Category status updated!");
        return "redirect:/admin/categories";
    }

    private String generateSlug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}

package com.ecommerce.controller.admin;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "15") int size,
                       @RequestParam(required = false) String q) {
        Page<Product> products = (q != null && !q.isBlank())
                ? productRepository.searchProducts(q, PageRequest.of(page, size))
                : productRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("q", q);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new ProductRequest());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("parentCategories", categoryRepository.findByParentIsNullAndActiveTrue());
        return "admin/products/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute ProductRequest req, RedirectAttributes ra) {
        try {
            productService.create(req);
            ra.addFlashAttribute("success", "Product created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        model.addAttribute("product", p);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("parentCategories", categoryRepository.findByParentIsNullAndActiveTrue());
        return "admin/products/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute ProductRequest req, RedirectAttributes ra) {
        try {
            productService.update(id, req);
            ra.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes ra) {
        Product p = productRepository.findById(id).orElseThrow();
        p.setActive(!p.isActive());
        productRepository.save(p);
        ra.addFlashAttribute("success", "Product status updated!");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Product deleted!");
        return "redirect:/admin/products";
    }

    @GetMapping("/low-stock")
    public String lowStock(Model model) {
        model.addAttribute("products", productRepository.findLowStockProducts());
        return "admin/products/low-stock";
    }
}

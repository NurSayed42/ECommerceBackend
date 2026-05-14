package com.ecommerce.controller.admin;

import com.ecommerce.model.Review;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.service.impl.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','MODERATOR')")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "pending") String tab) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        if ("reported".equals(tab)) {
            model.addAttribute("reviews", reviewRepository.findByReportedTrue(pageable));
        } else {
            model.addAttribute("reviews", reviewRepository.findByApprovedFalse(pageable));
        }
        model.addAttribute("tab", tab);
        return "admin/reviews/list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.approveReview(id);
        ra.addFlashAttribute("success", "Review approved");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        reviewService.deleteReview(id);
        ra.addFlashAttribute("success", "Review deleted");
        return "redirect:/admin/reviews";
    }
}

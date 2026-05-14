package com.ecommerce.repository;
import com.ecommerce.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    Page<Review> findByApprovedFalse(Pageable pageable);
    Page<Review> findByReportedTrue(Pageable pageable);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double getAverageRating(Long productId);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    long countApprovedByProductId(Long productId);
    List<Review> findByProductIdAndApprovedTrue(Long productId);
}

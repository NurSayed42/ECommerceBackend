package com.ecommerce.repository;
import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    List<Product> findByFeaturedTrueAndActiveTrue();
    List<Product> findByFlashSaleTrueAndActiveTrue();
    List<Product> findByTrendingTrueAndActiveTrue();
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock <= p.lowStockThreshold AND p.stock > 0")
    List<Product> findLowStockProducts();
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.soldCount DESC")
    List<Product> findTopSellingProducts(Pageable pageable);
    @Query("SELECT p FROM Product p WHERE p.active = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Product> searchProducts(String q, Pageable pageable);
    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(Long id);
    long countByActiveTrue();
}

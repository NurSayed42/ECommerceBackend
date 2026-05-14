package com.ecommerce.repository;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Step 1: Paginate orders in DB (no entity graph to avoid in-memory pagination warning)
    @Query("""
            SELECT o FROM Order o
            WHERE (CAST(:search AS string) IS NULL OR o.orderNumber LIKE CONCAT('%', CAST(:search AS string), '%'))
            AND   (CAST(:status AS string) IS NULL OR o.status = :status)
            ORDER BY o.createdAt DESC
            """)
    Page<Order> searchOrders(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    // Step 2: Eagerly load collections only for the already-paginated IDs
    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    @Query("SELECT o FROM Order o WHERE o.id IN :ids ORDER BY o.createdAt DESC")
    List<Order> findByIdsWithItems(@Param("ids") List<Long> ids);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE CAST(o.createdAt AS date) = CURRENT_DATE")
    long countTodayOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND CAST(o.createdAt AS date) = CURRENT_DATE")
    BigDecimal getTodayRevenue();

    long countByStatus(OrderStatus status);

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT FUNCTION('DATE', o.createdAt), COUNT(o), SUM(o.totalAmount) FROM Order o WHERE o.createdAt >= :since AND o.status = 'DELIVERED' GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> getDailyRevenue(@Param("since") LocalDateTime since);


    // এই query যোগ করো
    @Query("""
    SELECT COUNT(o) > 0 FROM Order o
    JOIN o.items i
    WHERE o.user.id = :userId
    AND i.product.id = :productId
    AND o.status = 'DELIVERED'
    """)
    boolean hasUserDeliveredOrderForProduct(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );
}
package com.ecommerce.repository;
import com.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "u.phone LIKE CONCAT('%',:search,'%')")
    Page<User> searchUsers(String search, Pageable pageable);

    long countByActive(boolean active);
    long countByBlocked(boolean blocked);
    @Query("SELECT COUNT(u) FROM User u WHERE CAST(u.createdAt AS date) = CURRENT_DATE")
    long countTodayRegistrations();
}

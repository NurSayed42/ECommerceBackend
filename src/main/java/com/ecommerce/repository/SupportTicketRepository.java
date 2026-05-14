package com.ecommerce.repository;
import com.ecommerce.enums.TicketStatus;
import com.ecommerce.model.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Page<SupportTicket> findByUserId(Long userId, Pageable pageable);
    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);
    long countByStatus(TicketStatus status);
}

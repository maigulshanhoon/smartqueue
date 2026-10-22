package com.smartqueue.repository;

import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.model.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {

    Optional<QueueToken> findByTokenNumber(String tokenNumber);

    List<QueueToken> findByStatusOrderByCreatedAtAsc(TokenStatus status);

    List<QueueToken> findByStatusOrderByServedAtDesc(TokenStatus status);

    List<QueueToken> findTop15ByStatusInOrderByCompletedAtDesc(List<TokenStatus> statuses);

    List<QueueToken> findTop50ByOrderByCreatedAtDesc();

    Optional<QueueToken> findFirstByStatusOrderByCreatedAtAsc(TokenStatus status);

    Optional<QueueToken> findFirstByServiceTypeAndStatusOrderByCreatedAtAsc(ServiceType serviceType, TokenStatus status);

    long countByStatus(TokenStatus status);

    long countByStatusAndIdLessThan(TokenStatus status, Long id);

    long countByStatusAndCreatedAtBefore(TokenStatus status, LocalDateTime createdAt);

    @Query("SELECT COUNT(t) FROM QueueToken t WHERE t.createdAt >= :startOfDay")
    long countCreatedSince(@Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(t) FROM QueueToken t WHERE t.serviceType = :serviceType AND t.createdAt >= :startOfDay")
    long countByServiceTypeAndCreatedSince(@Param("serviceType") ServiceType serviceType, @Param("startOfDay") LocalDateTime startOfDay);
}

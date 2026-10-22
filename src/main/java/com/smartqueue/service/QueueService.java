package com.smartqueue.service;

import com.smartqueue.model.QueueStats;
import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.model.TokenStatus;
import com.smartqueue.repository.QueueTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QueueService {

    private final QueueTokenRepository tokenRepository;

    public QueueService(QueueTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Issues a new sequential token for the selected service.
     */
    @Transactional
    public synchronized QueueToken issueToken(ServiceType serviceType, String customerName) {
        if (serviceType == null) {
            serviceType = ServiceType.GENERAL_ENQUIRY;
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long existingCount = tokenRepository.countByServiceTypeAndCreatedSince(serviceType, startOfDay);

        // Generate unique sequential token number e.g. GEN-001, BIL-002
        long sequenceNumber = existingCount + 1;
        String tokenNumber = formatTokenNumber(serviceType.getPrefix(), sequenceNumber);

        // Guard against collisions in case of sequence drift
        while (tokenRepository.findByTokenNumber(tokenNumber).isPresent()) {
            sequenceNumber++;
            tokenNumber = formatTokenNumber(serviceType.getPrefix(), sequenceNumber);
        }

        QueueToken token = new QueueToken(tokenNumber, serviceType, customerName);
        return tokenRepository.save(token);
    }

    /**
     * Calls the next waiting token to the specified counter.
     */
    @Transactional
    public Optional<QueueToken> callNext(String counterName, ServiceType serviceTypeFilter) {
        String effectiveCounter = (counterName == null || counterName.trim().isEmpty())
                ? "Counter 1"
                : counterName.trim();

        Optional<QueueToken> nextWaiting;
        if (serviceTypeFilter != null) {
            nextWaiting = tokenRepository.findFirstByServiceTypeAndStatusOrderByCreatedAtAsc(serviceTypeFilter, TokenStatus.WAITING);
        } else {
            nextWaiting = tokenRepository.findFirstByStatusOrderByCreatedAtAsc(TokenStatus.WAITING);
        }

        if (nextWaiting.isEmpty()) {
            return Optional.empty();
        }

        QueueToken token = nextWaiting.get();
        token.setStatus(TokenStatus.SERVING);
        token.setServedAt(LocalDateTime.now());
        token.setCounterName(effectiveCounter);
        return Optional.of(tokenRepository.save(token));
    }

    /**
     * Completes an active or waiting token.
     */
    @Transactional
    public QueueToken completeToken(Long tokenId) {
        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + tokenId));

        token.setStatus(TokenStatus.COMPLETED);
        token.setCompletedAt(LocalDateTime.now());
        return tokenRepository.save(token);
    }

    /**
     * Skips a token (e.g. absent customer).
     */
    @Transactional
    public QueueToken skipToken(Long tokenId) {
        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + tokenId));

        token.setStatus(TokenStatus.SKIPPED);
        token.setCompletedAt(LocalDateTime.now());
        return tokenRepository.save(token);
    }

    /**
     * Calls a specific token directly by ID.
     */
    @Transactional
    public QueueToken callSpecificToken(Long tokenId, String counterName) {
        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + tokenId));

        String effectiveCounter = (counterName == null || counterName.trim().isEmpty())
                ? "Counter 1"
                : counterName.trim();

        token.setStatus(TokenStatus.SERVING);
        token.setServedAt(LocalDateTime.now());
        token.setCounterName(effectiveCounter);
        return tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public QueueStats getQueueStats() {
        long waiting = tokenRepository.countByStatus(TokenStatus.WAITING);
        long serving = tokenRepository.countByStatus(TokenStatus.SERVING);
        long completed = tokenRepository.countByStatus(TokenStatus.COMPLETED);
        long skipped = tokenRepository.countByStatus(TokenStatus.SKIPPED);
        long totalToday = tokenRepository.countCreatedSince(LocalDate.now().atStartOfDay());

        return new QueueStats(waiting, serving, completed, skipped, totalToday);
    }

    @Transactional(readOnly = true)
    public List<QueueToken> getCurrentlyServing() {
        return tokenRepository.findByStatusOrderByServedAtDesc(TokenStatus.SERVING);
    }

    @Transactional(readOnly = true)
    public List<QueueToken> getWaitingTokens() {
        return tokenRepository.findByStatusOrderByCreatedAtAsc(TokenStatus.WAITING);
    }

    @Transactional(readOnly = true)
    public List<QueueToken> getRecentTokens() {
        return tokenRepository.findTop15ByStatusInOrderByCompletedAtDesc(List.of(TokenStatus.COMPLETED, TokenStatus.SKIPPED));
    }

    @Transactional(readOnly = true)
    public Optional<QueueToken> findByTokenNumber(String tokenNumber) {
        if (tokenNumber == null || tokenNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return tokenRepository.findByTokenNumber(tokenNumber.trim().toUpperCase());
    }

    @Transactional(readOnly = true)
    public Optional<QueueToken> findById(Long id) {
        return tokenRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public long getPeopleAhead(QueueToken token) {
        if (token == null || token.getStatus() != TokenStatus.WAITING || token.getId() == null) {
            return 0;
        }
        return tokenRepository.countByStatusAndIdLessThan(TokenStatus.WAITING, token.getId());
    }

    @Transactional(readOnly = true)
    public int getEstimatedWaitMinutes(QueueToken token) {
        if (token == null || token.getStatus() != TokenStatus.WAITING) {
            return 0;
        }
        long ahead = getPeopleAhead(token);
        int avgMinutes = token.getServiceType() != null ? token.getServiceType().getAverageWaitMinutes() : 5;
        return (int) Math.max(avgMinutes, ahead * avgMinutes);
    }

    private String formatTokenNumber(String prefix, long sequenceNumber) {
        return String.format("%s-%03d", prefix, sequenceNumber);
    }
}

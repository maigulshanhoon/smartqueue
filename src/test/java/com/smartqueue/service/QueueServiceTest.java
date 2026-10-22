package com.smartqueue.service;

import com.smartqueue.model.QueueStats;
import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.model.TokenStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.smartqueue.repository.QueueTokenRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class QueueServiceTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void shouldIssueSequentialTokensForServices() {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Alice");
        QueueToken t2 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Bob");
        QueueToken t3 = queueService.issueToken(ServiceType.BILLING, "Charlie");

        assertNotNull(t1.getId());
        assertEquals("GEN-001", t1.getTokenNumber());
        assertEquals("Alice", t1.getCustomerName());
        assertEquals(TokenStatus.WAITING, t1.getStatus());

        assertEquals("GEN-002", t2.getTokenNumber());
        assertEquals("BIL-001", t3.getTokenNumber());
    }

    @Test
    void shouldCalculatePeopleAheadAndEstimatedWait() {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Alice");
        QueueToken t2 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Bob");
        QueueToken t3 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Charlie");

        assertEquals(0, queueService.getPeopleAhead(t1));
        assertEquals(1, queueService.getPeopleAhead(t2));
        assertEquals(2, queueService.getPeopleAhead(t3));

        assertTrue(queueService.getEstimatedWaitMinutes(t1) > 0);
        assertTrue(queueService.getEstimatedWaitMinutes(t3) >= queueService.getEstimatedWaitMinutes(t2));
    }

    @Test
    void shouldCallNextWaitingToken() {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Alice");
        QueueToken t2 = queueService.issueToken(ServiceType.BILLING, "Bob");

        Optional<QueueToken> called = queueService.callNext("Counter 2", null);

        assertTrue(called.isPresent());
        assertEquals(t1.getId(), called.get().getId());
        assertEquals(TokenStatus.SERVING, called.get().getStatus());
        assertEquals("Counter 2", called.get().getCounterName());
        assertNotNull(called.get().getServedAt());

        // Alice is now serving, Bob should have 0 people ahead
        assertEquals(0, queueService.getPeopleAhead(t2));
    }

    @Test
    void shouldReturnEmptyWhenCallingNextOnEmptyQueue() {
        Optional<QueueToken> called = queueService.callNext("Counter 1", null);
        assertTrue(called.isEmpty());
    }

    @Test
    void shouldCompleteToken() {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "Alice");
        queueService.callNext("Counter 1", null);

        QueueToken completed = queueService.completeToken(t1.getId());

        assertEquals(TokenStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    void shouldSkipToken() {
        QueueToken t1 = queueService.issueToken(ServiceType.TECHNICAL_SUPPORT, "Alice");

        QueueToken skipped = queueService.skipToken(t1.getId());

        assertEquals(TokenStatus.SKIPPED, skipped.getStatus());
        assertNotNull(skipped.getCompletedAt());
    }

    @Test
    void shouldThrowExceptionForInvalidTokenActions() {
        assertThrows(IllegalArgumentException.class, () -> queueService.completeToken(99999L));
        assertThrows(IllegalArgumentException.class, () -> queueService.skipToken(99999L));
        assertThrows(IllegalArgumentException.class, () -> queueService.callSpecificToken(99999L, "Counter 1"));
    }

    @Test
    void shouldTrackAccurateQueueStatistics() {
        QueueToken t1 = queueService.issueToken(ServiceType.GENERAL_ENQUIRY, "T1");
        QueueToken t2 = queueService.issueToken(ServiceType.BILLING, "T2");
        QueueToken t3 = queueService.issueToken(ServiceType.TECHNICAL_SUPPORT, "T3");

        queueService.callNext("Counter 1", null); // T1 serving
        queueService.completeToken(t1.getId()); // T1 completed
        queueService.callNext("Counter 2", null); // T2 serving
        queueService.skipToken(t3.getId()); // T3 skipped

        QueueStats stats = queueService.getQueueStats();
        assertEquals(0, stats.totalWaiting());
        assertEquals(1, stats.totalServing());
        assertEquals(1, stats.totalCompleted());
        assertEquals(1, stats.totalSkipped());
        assertEquals(3, stats.totalToday());
    }
}

package com.smartqueue.controller;

import com.smartqueue.model.QueueStats;
import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/queue")
public class QueueApiController {

    private final QueueService queueService;

    public QueueApiController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        QueueStats stats = queueService.getQueueStats();
        List<QueueToken> serving = queueService.getCurrentlyServing();
        List<QueueToken> waiting = queueService.getWaitingTokens();

        return ResponseEntity.ok(Map.of(
                "stats", stats,
                "nowServing", serving,
                "waiting", waiting,
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    public record TokenRequest(String serviceType, String customerName) {}

    @PostMapping("/tokens")
    public ResponseEntity<Map<String, Object>> issueToken(@RequestBody TokenRequest request) {
        ServiceType service = ServiceType.fromString(request.serviceType());
        QueueToken token = queueService.issueToken(service, request.customerName());
        long peopleAhead = queueService.getPeopleAhead(token);
        int waitTime = queueService.getEstimatedWaitMinutes(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "token", token,
                "peopleAhead", peopleAhead,
                "estimatedWaitMinutes", waitTime,
                "status", "SUCCESS"
        ));
    }

    @GetMapping("/tokens/{tokenNumber}")
    public ResponseEntity<Map<String, Object>> getTokenDetails(@PathVariable String tokenNumber) {
        Optional<QueueToken> opt = queueService.findByTokenNumber(tokenNumber);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Token not found: " + tokenNumber
            ));
        }

        QueueToken token = opt.get();
        long peopleAhead = queueService.getPeopleAhead(token);
        int waitTime = queueService.getEstimatedWaitMinutes(token);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "peopleAhead", peopleAhead,
                "estimatedWaitMinutes", waitTime
        ));
    }

    public record CallNextRequest(String counterName, String serviceType) {}

    @PostMapping("/call-next")
    public ResponseEntity<?> callNext(@RequestBody(required = false) CallNextRequest request) {
        String counter = (request != null && request.counterName() != null) ? request.counterName() : "Counter 1";
        ServiceType serviceType = (request != null && request.serviceType() != null && !request.serviceType().isBlank())
                ? ServiceType.fromString(request.serviceType())
                : null;

        Optional<QueueToken> called = queueService.callNext(counter, serviceType);
        if (called.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "called", false,
                    "message", "No tokens currently waiting in queue"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "called", true,
                "token", called.get()
        ));
    }

    @PostMapping("/tokens/{id}/complete")
    public ResponseEntity<?> completeToken(@PathVariable Long id) {
        try {
            QueueToken token = queueService.completeToken(id);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tokens/{id}/skip")
    public ResponseEntity<?> skipToken(@PathVariable Long id) {
        try {
            QueueToken token = queueService.skipToken(id);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}

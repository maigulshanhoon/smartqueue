package com.smartqueue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_tokens", indexes = {
    @Index(name = "idx_token_number", columnList = "token_number", unique = true),
    @Index(name = "idx_token_status", columnList = "status"),
    @Index(name = "idx_token_created", columnList = "created_at")
})
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, unique = true, length = 32)
    private String tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 32)
    private ServiceType serviceType;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TokenStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "counter_name", length = 50)
    private String counterName;

    public QueueToken() {
    }

    public QueueToken(String tokenNumber, ServiceType serviceType, String customerName) {
        this.tokenNumber = tokenNumber;
        this.serviceType = serviceType;
        this.customerName = (customerName == null || customerName.trim().isEmpty()) ? "Guest" : customerName.trim();
        this.status = TokenStatus.WAITING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getServedAt() {
        return servedAt;
    }

    public void setServedAt(LocalDateTime servedAt) {
        this.servedAt = servedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }
}

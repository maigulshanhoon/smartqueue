package com.smartqueue.model;

public record QueueStats(
    long totalWaiting,
    long totalServing,
    long totalCompleted,
    long totalSkipped,
    long totalToday
) {
}

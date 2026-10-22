package com.smartqueue.model;

import java.util.Collections;
import java.util.Map;

public record QueueStats(
    long totalWaiting,
    long totalServing,
    long totalCompleted,
    long totalSkipped,
    long totalCancelled,
    long totalToday,
    Map<String, Long> byService
) {
    public QueueStats(long totalWaiting, long totalServing, long totalCompleted, long totalSkipped, long totalToday) {
        this(totalWaiting, totalServing, totalCompleted, totalSkipped, 0, totalToday, Collections.emptyMap());
    }

    public long totalServed() {
        return totalCompleted;
    }

    public long totalServedAndCancelled() {
        return totalCompleted + totalSkipped + totalCancelled;
    }
}

package com.smartqueue.model;

public enum TokenStatus {
    WAITING("Waiting"),
    SERVING("Now Serving"),
    COMPLETED("Served"),
    SKIPPED("Skipped"),
    CANCELLED("Cancelled");

    private final String displayName;

    TokenStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

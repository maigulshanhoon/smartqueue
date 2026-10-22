package com.smartqueue.model;

public enum ServiceType {
    GENERAL_ENQUIRY("General Enquiry", "GEN", "General questions, registrations, and basic information", 5),
    BILLING("Billing & Payments", "BIL", "Fee payments, invoices, refunds, and cashier services", 7),
    TECHNICAL_SUPPORT("Technical Support", "TEC", "Technical setup, account issues, and hardware diagnostics", 10);

    private final String displayName;
    private final String prefix;
    private final String description;
    private final int averageWaitMinutes;

    ServiceType(String displayName, String prefix, String description, int averageWaitMinutes) {
        this.displayName = displayName;
        this.prefix = prefix;
        this.description = description;
        this.averageWaitMinutes = averageWaitMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDescription() {
        return description;
    }

    public int getAverageWaitMinutes() {
        return averageWaitMinutes;
    }

    public static ServiceType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return GENERAL_ENQUIRY;
        }
        for (ServiceType st : ServiceType.values()) {
            if (st.name().equalsIgnoreCase(text.trim()) || st.prefix.equalsIgnoreCase(text.trim())) {
                return st;
            }
        }
        return GENERAL_ENQUIRY;
    }
}

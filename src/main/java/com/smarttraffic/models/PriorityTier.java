package com.smarttraffic.models;

/**
 * Transit Efficiency Tiers based on calculated Priority Score.
 */
public enum PriorityTier {
    GOLD_CORRIDOR("Gold Corridor", 90.0, 100.0, "Zero Delay / Seamless Transit"),
    STANDARD_CLEARANCE("Standard Clearance", 75.0, 89.99, "Normal Signal Preemption"),
    MINOR_TRAFFIC_DELAY("Minor Traffic Delay", 60.0, 74.99, "Congestion Slowdown Review"),
    ROUTE_BLOCKED("Route Blocked", 0.0, 59.99, "Reroute Alert Issued");

    private final String tierName;
    private final double minScore;
    private final double maxScore;
    private final String actionDescription;

    PriorityTier(String tierName, double minScore, double maxScore, String actionDescription) {
        this.tierName = tierName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.actionDescription = actionDescription;
    }

    public String getTierName() {
        return tierName;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public static PriorityTier fromScore(double score, boolean overrideFailed) {
        if (overrideFailed || score < 60.0) {
            return ROUTE_BLOCKED;
        } else if (score >= 90.0) {
            return GOLD_CORRIDOR;
        } else if (score >= 75.0) {
            return STANDARD_CLEARANCE;
        } else {
            return MINOR_TRAFFIC_DELAY;
        }
    }
}

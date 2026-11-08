package com.smarttraffic.models;

/**
 * Emergency Vehicle Categories with their priority weighting characteristics.
 */
public enum VehicleCategory {
    AMBULANCE("Ambulance Emergency Corridor", 3.0),
    FIRE_ENGINE("Fire Engine Response", 5.0),
    POLICE_VIP("Police VIP Convoy", 2.0);

    private final String displayName;
    private final double maxDelayLimitMinutes;

    VehicleCategory(String displayName, double maxDelayLimitMinutes) {
        this.displayName = displayName;
        this.maxDelayLimitMinutes = maxDelayLimitMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMaxDelayLimitMinutes() {
        return maxDelayLimitMinutes;
    }

    public static VehicleCategory fromString(String categoryStr) {
        if (categoryStr == null) return AMBULANCE;
        String clean = categoryStr.trim().toUpperCase().replace(" ", "_");
        for (VehicleCategory cat : values()) {
            if (cat.name().equals(clean)) {
                return cat;
            }
        }
        return AMBULANCE;
    }
}

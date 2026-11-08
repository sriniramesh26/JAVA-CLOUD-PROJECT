package com.smarttraffic.utils;

import java.util.regex.Pattern;

/**
 * String Processing and Validation Utilities.
 * Handles Beacon Code regex validation, CSV parsing with .split(","), and .trim() sanitization.
 */
public final class ValidationUtils {

    // Regex for Vehicle Beacon Code: EMG-[A-Z]{3}-\d{4}
    private static final String BEACON_CODE_REGEX = "^EMG-[A-Z]{3}-\\d{4}$";
    private static final Pattern BEACON_PATTERN = Pattern.compile(BEACON_CODE_REGEX);

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Validates if the given beacon code strictly matches EMG-[A-Z]{3}-\d{4}.
     * Example: EMG-AMB-1001, EMG-FIR-2042, EMG-POL-3099.
     */
    public static boolean isValidBeaconCode(String beaconId) {
        if (beaconId == null) {
            return false;
        }
        return BEACON_PATTERN.matcher(beaconId.trim()).matches();
    }

    /**
     * Parses a comma-separated line, trimming each field.
     */
    public static String[] parseCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        String[] tokens = line.split(",", -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

    /**
     * Cleans location tags and identifiers via .trim().
     */
    public static String cleanTag(String tag) {
        return (tag == null) ? "" : tag.trim();
    }

    /**
     * Formats signal override logs using String.format().
     */
    public static String formatOverrideLog(String vehicleId, String intersectionId,
                                           String prevSignal, String newSignal,
                                           boolean override, double etaMinutes,
                                           int delaySeconds) {
        return String.format(
            "OVERRIDE_EVENT | Vehicle: %-8s | Intersection: %-8s | ETA: %5.2f min | %-6s -> %-6s | Triggered: %-5s | Delay: %2ds",
            cleanTag(vehicleId),
            cleanTag(intersectionId),
            etaMinutes,
            cleanTag(prevSignal),
            cleanTag(newSignal),
            override ? "TRUE" : "FALSE",
            delaySeconds
        );
    }
}

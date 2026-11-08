package com.smarttraffic.services;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.models.VehicleCategory;

/**
 * Priority Scoring and Transit Efficiency Calculator.
 * Implements weighted priority formulas, ETE, and CMI according to project specifications.
 */
public final class PriorityCalculator {

    public static final double STANDARD_SIGNAL_CYCLE_SEC = 90.0;

    private PriorityCalculator() {
        // Utility class
    }

    /**
     * Clamps a value into the range [0.0, 100.0].
     */
    public static double clamp(double value) {
        if (value < 0.0) return 0.0;
        if (value > 100.0) return 100.0;
        return value;
    }

    /**
     * Calculates Priority Score based on vehicle category and weighted parameters.
     * Raw inputs are clamped to [0, 100].
     */
    public static double calculateScore(VehicleCategory category, double param1, double param2, double param3) {
        double p1 = clamp(param1);
        double p2 = clamp(param2);
        double p3 = clamp(param3);

        double score;
        switch (category) {
            case AMBULANCE:
                // ETA Precision (50%) + Queue Density (30%) + Patient Criticality (20%)
                score = (p1 * 0.50) + (p2 * 0.30) + (p3 * 0.20);
                break;
            case FIRE_ENGINE:
                // Queue Density (40%) + Weight Clearance (30%) + ETA Precision (30%)
                score = (p1 * 0.40) + (p2 * 0.30) + (p3 * 0.30);
                break;
            case POLICE_VIP:
                // Signal Synchronization (50%) + Security Clearance (30%) + Speed Maintenance (20%)
                score = (p1 * 0.50) + (p2 * 0.30) + (p3 * 0.20);
                break;
            default:
                score = 50.0;
        }
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Overload for EmergencyVehicle instance.
     */
    public static double calculateScore(EmergencyVehicle vehicle) {
        if (vehicle == null) return 0.0;
        return calculateScore(vehicle.getCategory(), vehicle.getParam1(), vehicle.getParam2(), vehicle.getParam3());
    }

    /**
     * Evaluates PriorityTier given score and override state.
     */
    public static PriorityTier evaluateTier(double score, boolean overrideFailed) {
        return PriorityTier.fromScore(score, overrideFailed);
    }

    /**
     * Emergency Transit Efficiency (ETE):
     * ETE = (UncongestedEstimatedTime / ActualEmergencyTransitTime) * 100
     */
    public static double calculateETE(double uncongestedEstimatedSeconds, double actualTransitSeconds) {
        if (actualTransitSeconds <= 0.0) {
            return 100.0;
        }
        double ete = (uncongestedEstimatedSeconds / actualTransitSeconds) * 100.0;
        return Math.round(ete * 100.0) / 100.0;
    }

    /**
     * Congestion Mitigation Index (CMI):
     * CMI = 100 - (IntersectionDelayTime / StandardSignalCycle) * 100
     */
    public static double calculateCMI(double delaySeconds, double standardSignalCycle) {
        if (standardSignalCycle <= 0.0) {
            standardSignalCycle = STANDARD_SIGNAL_CYCLE_SEC;
        }
        double cmi = 100.0 - ((delaySeconds / standardSignalCycle) * 100.0);
        return Math.round(cmi * 100.0) / 100.0;
    }
}

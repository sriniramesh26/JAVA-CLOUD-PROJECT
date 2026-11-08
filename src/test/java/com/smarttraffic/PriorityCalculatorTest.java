package com.smarttraffic;

import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.services.PriorityCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriorityCalculatorTest {

    @Test
    @DisplayName("Ambulance priority formula: 50% ETA + 30% Queue + 20% Criticality")
    void testAmbulanceFormula() {
        // (95 * 0.50) + (85 * 0.30) + (90 * 0.20) = 47.5 + 25.5 + 18.0 = 91.00
        double score = PriorityCalculator.calculateScore(VehicleCategory.AMBULANCE, 95.0, 85.0, 90.0);
        assertEquals(91.00, score, 0.01);
    }

    @Test
    @DisplayName("Fire Engine priority formula: 40% Queue + 30% Weight + 30% ETA")
    void testFireEngineFormula() {
        // (75 * 0.40) + (92 * 0.30) + (88 * 0.30) = 30.0 + 27.6 + 26.4 = 84.00
        double score = PriorityCalculator.calculateScore(VehicleCategory.FIRE_ENGINE, 75.0, 92.0, 88.0);
        assertEquals(84.00, score, 0.01);
    }

    @Test
    @DisplayName("Police VIP Convoy formula: 50% Sync + 30% Security + 20% Speed")
    void testPoliceVipFormula() {
        // (90 * 0.50) + (95 * 0.30) + (85 * 0.20) = 45.0 + 28.5 + 17.0 = 90.50
        double score = PriorityCalculator.calculateScore(VehicleCategory.POLICE_VIP, 90.0, 95.0, 85.0);
        assertEquals(90.50, score, 0.01);
    }

    @Test
    @DisplayName("Priority Tiers classification boundaries")
    void testPriorityTiers() {
        assertEquals(PriorityTier.GOLD_CORRIDOR, PriorityCalculator.evaluateTier(90.0, false));
        assertEquals(PriorityTier.GOLD_CORRIDOR, PriorityCalculator.evaluateTier(100.0, false));
        assertEquals(PriorityTier.STANDARD_CLEARANCE, PriorityCalculator.evaluateTier(84.0, false));
        assertEquals(PriorityTier.MINOR_TRAFFIC_DELAY, PriorityCalculator.evaluateTier(67.5, false));
        assertEquals(PriorityTier.ROUTE_BLOCKED, PriorityCalculator.evaluateTier(55.0, false));
        assertEquals(PriorityTier.ROUTE_BLOCKED, PriorityCalculator.evaluateTier(95.0, true)); // Override failed forces blocked
    }

    @Test
    @DisplayName("ETE Mathematical Formula calculation")
    void testEteFormula() {
        // ETE = (Uncongested / Actual) * 100
        // (120 / 150) * 100 = 80.0
        double ete = PriorityCalculator.calculateETE(120.0, 150.0);
        assertEquals(80.00, ete, 0.01);
    }

    @Test
    @DisplayName("CMI Mathematical Formula calculation")
    void testCmiFormula() {
        // CMI = 100 - (Delay / StandardCycle) * 100
        // 100 - (18 / 90) * 100 = 100 - 20 = 80.0
        double cmi = PriorityCalculator.calculateCMI(18.0, 90.0);
        assertEquals(80.00, cmi, 0.01);
    }
}

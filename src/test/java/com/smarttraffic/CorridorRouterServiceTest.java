package com.smarttraffic;

import com.smarttraffic.models.*;
import com.smarttraffic.services.CorridorRouterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorridorRouterServiceTest {

    private CorridorRouterService routerService;

    @BeforeEach
    void setUp() {
        routerService = new CorridorRouterService();
    }

    @AfterEach
    void tearDown() {
        routerService.shutdown();
    }

    @Test
    @DisplayName("Green corridor override triggers when ETA < 2.0 min at blocked intersection")
    void testGreenWavePreemption() {
        EmergencyVehicle ev = new EmergencyVehicle(
                "EV-101", "EMG-AMB-1001", VehicleCategory.AMBULANCE,
                "CRITICAL_CODE_RED", "HOSP-01", 12.97, 77.59, 68.5,
                "INT-01", 84, 1.4, 95.0, 85.0, 90.0
        );

        List<Intersection> route = new ArrayList<>();
        route.add(new Intersection("INT-01", "Junction 1", 12.97, 77.59, SignalState.RED, 80, true, 45, 5));
        route.add(new Intersection("INT-02", "Junction 2", 12.98, 77.60, SignalState.RED, 40, false, 40, 4));

        CorridorRouterService.CorridorExecutionResult result = routerService.dispatchCorridor(ev, route);

        assertNotNull(result);
        assertNotNull(result.getReport());
        assertEquals(2, result.getLogs().size());
        assertEquals(1, result.getReport().getOverriddenIntersections()); // INT-01 was blocked, INT-02 was not
        assertEquals(PriorityTier.GOLD_CORRIDOR, result.getReport().getPriorityTier());
    }

    @Test
    @DisplayName("Green corridor override NOT triggered when ETA >= 2.0 min")
    void testNoOverrideWhenEtaExceedsBoundary() {
        EmergencyVehicle ev = new EmergencyVehicle(
                "EV-102", "EMG-AMB-1002", VehicleCategory.AMBULANCE,
                "URGENT_YELLOW", "HOSP-02", 12.97, 77.59, 50.0,
                "INT-02", 45, 2.0, 80.0, 45.0, 70.0
        );

        List<Intersection> route = new ArrayList<>();
        route.add(new Intersection("INT-02", "Junction 2", 12.97, 77.59, SignalState.RED, 45, true, 45, 5));

        CorridorRouterService.CorridorExecutionResult result = routerService.dispatchCorridor(ev, route);

        assertEquals(0, result.getReport().getOverriddenIntersections());
        assertFalse(result.getLogs().get(0).isOverrideTriggered());
    }
}

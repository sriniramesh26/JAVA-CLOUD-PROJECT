package com.smarttraffic;

import com.smarttraffic.models.*;
import com.smarttraffic.workers.CorridorWorker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class CorridorWorkerMultithreadingTest {

    @Test
    @DisplayName("Parallel execution of CorridorWorker across multiple intersections")
    void testParallelWorkers() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        EmergencyVehicle ev = new EmergencyVehicle(
                "EV-101", "EMG-AMB-1001", VehicleCategory.AMBULANCE,
                "CODE_RED", "HOSP-01", 12.97, 77.59, 70.0,
                "INT-01", 80, 1.2, 90.0, 90.0, 90.0
        );

        List<Intersection> intersections = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            intersections.add(new Intersection("INT-0" + i, "Junction " + i, 12.97 + (i * 0.01), 77.59 + (i * 0.01),
                    SignalState.RED, 50, true, 45, 5));
        }

        List<Future<PreemptionLog>> futures = new ArrayList<>();
        for (Intersection in : intersections) {
            futures.add(executor.submit(new CorridorWorker(ev, in, ev.getEtaMinutes())));
        }

        for (Future<PreemptionLog> f : futures) {
            PreemptionLog log = f.get(3, TimeUnit.SECONDS);
            assertNotNull(log);
            assertTrue(log.isOverrideTriggered());
            assertEquals(SignalState.GREEN, log.getNewSignal());
            assertNotNull(log.getThreadName());
        }

        executor.shutdown();
    }
}

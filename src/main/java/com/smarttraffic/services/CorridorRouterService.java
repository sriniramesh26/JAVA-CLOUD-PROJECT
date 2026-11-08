package com.smarttraffic.services;

import com.smarttraffic.models.*;
import com.smarttraffic.workers.CorridorWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Service managing multithreaded dynamic green corridor routing and preemption coordination.
 */
public class CorridorRouterService {

    private final ExecutorService executor;

    public CorridorRouterService() {
        this.executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "CorridorWorker-" + String.format("%02d", counter++));
                t.setDaemon(true);
                return t;
            }
        });
    }

    public CorridorRouterService(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Executes parallel signal preemption across the given corridor intersections for a dispatched emergency vehicle.
     */
    public CorridorExecutionResult dispatchCorridor(EmergencyVehicle vehicle, List<Intersection> corridorRoute) {
        List<Future<PreemptionLog>> futures = new ArrayList<>();
        List<PreemptionLog> logs = new ArrayList<>();

        double eta = vehicle.getEtaMinutes();

        // 1. Submit parallel preemption workers for all intersections along the corridor
        for (Intersection intersection : corridorRoute) {
            CorridorWorker worker = new CorridorWorker(vehicle, intersection, eta);
            futures.add(executor.submit(worker));
        }

        // 2. Gather future results
        int totalTransitSec = 0;
        int overriddenCount = 0;
        boolean anyFailure = false;

        for (Future<PreemptionLog> f : futures) {
            try {
                PreemptionLog log = f.get(5, TimeUnit.SECONDS);
                logs.add(log);
                totalTransitSec += log.getTransitDurationSec();
                if (log.isOverrideTriggered()) {
                    overriddenCount++;
                }
                if (log.getOverrideStatus() == OverrideStatus.FAILED) {
                    anyFailure = true;
                }
            } catch (Exception e) {
                anyFailure = true;
                System.err.println("[CorridorRouterService] Thread execution failed: " + e.getMessage());
            }
        }

        // 3. Compute priority score and tier
        double score = PriorityCalculator.calculateScore(vehicle);
        PriorityTier tier = PriorityCalculator.evaluateTier(score, anyFailure);

        // 4. Determine status based on max delay limit
        double maxDelaySeconds = vehicle.getCategory().getMaxDelayLimitMinutes() * 60.0;
        String status = (totalTransitSec > maxDelaySeconds || tier == PriorityTier.ROUTE_BLOCKED)
                ? "ABORTED" : "COMPLETED";

        // 5. Generate CorridorReport
        String reportId = "REP-" + vehicle.getVehicleId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String summary = String.format("Vehicle %s (%s) transit finished with score %.2f (%s) over %d intersections (%d overridden).",
                vehicle.getVehicleId(), vehicle.getCategory().getDisplayName(), score, tier.getTierName(),
                corridorRoute.size(), overriddenCount);

        CorridorReport report = new CorridorReport(
                reportId,
                vehicle.getVehicleId(),
                vehicle.getCategory(),
                score,
                tier,
                corridorRoute.size(),
                overriddenCount,
                totalTransitSec,
                status,
                summary
        );

        // 6. Calculate ETE and CMI
        double uncongestedTime = 120.0; // Baseline uncongested reference seconds
        double ete = PriorityCalculator.calculateETE(uncongestedTime, totalTransitSec);
        double totalDelay = logs.stream().mapToInt(PreemptionLog::getClearanceDelaySec).sum();
        double cmi = PriorityCalculator.calculateCMI(totalDelay, PriorityCalculator.STANDARD_SIGNAL_CYCLE_SEC);

        return new CorridorExecutionResult(report, logs, ete, cmi);
    }

    public void shutdown() {
        executor.shutdown();
    }

    /**
     * DTO containing all output artifacts of a corridor run.
     */
    public static class CorridorExecutionResult {
        private final CorridorReport report;
        private final List<PreemptionLog> logs;
        private final double eteScore;
        private final double cmiScore;

        public CorridorExecutionResult(CorridorReport report, List<PreemptionLog> logs, double eteScore, double cmiScore) {
            this.report = report;
            this.logs = logs;
            this.eteScore = eteScore;
            this.cmiScore = cmiScore;
        }

        public CorridorReport getReport() { return report; }
        public List<PreemptionLog> getLogs() { return logs; }
        public double getEteScore() { return eteScore; }
        public double getCmiScore() { return cmiScore; }
    }
}

package com.smarttraffic.workers;

import com.smarttraffic.models.*;
import com.smarttraffic.utils.ValidationUtils;

import java.util.concurrent.Callable;

/**
 * Worker thread simulating real-time dynamic traffic signal preemption at a single intersection.
 * Implements Callable<PreemptionLog> for parallel multithreaded execution across city corridors.
 */
public class CorridorWorker implements Callable<PreemptionLog> {

    private final EmergencyVehicle vehicle;
    private final Intersection intersection;
    private final double etaMinutes;

    public CorridorWorker(EmergencyVehicle vehicle, Intersection intersection, double etaMinutes) {
        this.vehicle = vehicle;
        this.intersection = intersection;
        this.etaMinutes = etaMinutes;
    }

    @Override
    public PreemptionLog call() throws Exception {
        String threadName = Thread.currentThread().getName();
        SignalState prevSignal = intersection.getCurrentSignalState();
        SignalState newSignal;
        boolean overrideTriggered;
        OverrideStatus status;
        int clearanceDelaySec;
        int transitDurationSec;

        // Core Rule: ETA < 2.0 min AND Intersection is BLOCKED forces GREEN_CORRIDOR_OVERRIDE
        if (etaMinutes < 2.0 && intersection.isBlocked()) {
            overrideTriggered = true;
            newSignal = SignalState.GREEN;
            clearanceDelaySec = intersection.getClearanceDelaySec();
            status = OverrideStatus.SUCCESS;

            // Update in-memory intersection state
            intersection.setCurrentSignalState(SignalState.GREEN);
            intersection.setBlocked(false);

            // Simulation sleep to mimic hardware relay activation (50ms)
            Thread.sleep(50);

            transitDurationSec = intersection.getGreenDurationSec() + clearanceDelaySec;
        } else if (etaMinutes < 2.0 && !intersection.isBlocked()) {
            overrideTriggered = false;
            newSignal = SignalState.GREEN;
            clearanceDelaySec = 2;
            status = OverrideStatus.BYPASSED;
            transitDurationSec = 30;
        } else {
            // Standard clearance / delay
            overrideTriggered = false;
            newSignal = prevSignal;
            clearanceDelaySec = intersection.getClearanceDelaySec() * 2;
            status = OverrideStatus.BYPASSED;
            transitDurationSec = 60;
        }

        String logMsg = ValidationUtils.formatOverrideLog(
                vehicle.getVehicleId(),
                intersection.getIntersectionId(),
                prevSignal.name(),
                newSignal.name(),
                overrideTriggered,
                etaMinutes,
                clearanceDelaySec
        );

        return new PreemptionLog(
                vehicle.getVehicleId(),
                intersection.getIntersectionId(),
                etaMinutes,
                overrideTriggered,
                status,
                prevSignal,
                newSignal,
                clearanceDelaySec,
                transitDurationSec,
                threadName,
                logMsg
        );
    }
}

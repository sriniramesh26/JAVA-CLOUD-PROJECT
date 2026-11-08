package com.smarttraffic.utils;

import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.PreemptionLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * File I/O and String Formatting Utility.
 * Exports signal preemption logs and corridor performance summaries to structured .txt files.
 */
public final class PreemptionWriter {

    private static final String OUTPUT_DIR = "output";
    private static final DateTimeFormatter FILE_TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private PreemptionWriter() {
        // Utility class
    }

    /**
     * Ensures output directory exists.
     */
    public static File getOutputDir() {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Exports execution logs to output/execution_logs_YYYYMMDD_HHmmss.txt.
     */
    public static String exportExecutionLogs(List<PreemptionLog> logs) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_TS_FORMAT);
        String filename = String.format("execution_logs_%s.txt", timestamp);
        File targetFile = new File(getOutputDir(), filename);

        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("==========================================================================================");
            pw.println("       SMART CITY TRAFFIC ENGINE - SIGNAL PREEMPTION EXECUTION TRACE LOGS                 ");
            pw.printf("       Generated: %s%n", LocalDateTime.now());
            pw.println("==========================================================================================");
            pw.println();
            pw.printf("%-6s | %-8s | %-8s | %-8s | %-6s -> %-6s | %-9s | %-6s | %-16s | %s%n",
                    "LOG ID", "VEHICLE", "INTERSEC", "ETA(MIN)", "FROM", "TO", "OVERRIDE", "STATUS", "THREAD", "DELAY(S)");
            pw.println("--------------------------------------------------------------------------------------------------");

            for (PreemptionLog l : logs) {
                pw.printf("%-6d | %-8s | %-8s | %8.2f | %-6s -> %-6s | %-9s | %-6s | %-16s | %ds%n",
                        l.getLogId(),
                        ValidationUtils.cleanTag(l.getVehicleId()),
                        ValidationUtils.cleanTag(l.getIntersectionId()),
                        l.getEtaMinutes(),
                        l.getPreviousSignal(),
                        l.getNewSignal(),
                        l.isOverrideTriggered() ? "TRIGGERED" : "NO_ACTION",
                        l.getOverrideStatus(),
                        ValidationUtils.cleanTag(l.getThreadName()),
                        l.getClearanceDelaySec()
                );
            }

            pw.println("--------------------------------------------------------------------------------------------------");
            pw.printf("Total logged events: %d%n", logs.size());
            pw.println("==========================================================================================");
        }

        return targetFile.getAbsolutePath();
    }

    /**
     * Exports Corridor Performance Summary to output/corridor_performance_<vehicleId>.txt.
     */
    public static String exportCorridorSummary(EmergencyVehicle vehicle, CorridorReport report,
                                               double eteScore, double cmiScore) throws IOException {
        String filename = String.format("corridor_performance_%s.txt", vehicle.getVehicleId());
        File targetFile = new File(getOutputDir(), filename);

        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("==========================================================================================");
            pw.println("       SMART CITY EMERGENCY VEHICLE GREEN CORRIDOR - TRANSIT AUDIT REPORT                ");
            pw.printf("       Vehicle ID: %-10s | Beacon: %-15s | Category: %s%n",
                    vehicle.getVehicleId(), vehicle.getBeaconId(), vehicle.getCategory().getDisplayName());
            pw.println("==========================================================================================");
            pw.println();
            pw.println("--- [1] DISPATCH & TELEMETRY PROFILE ---");
            pw.printf("  Destination Hospital/Site Code : %s%n", vehicle.getDestinationCode());
            pw.printf("  Emergency Severity Level       : %s%n", vehicle.getSeverityCode());
            pw.printf("  Telemetry Coordinates         : Lat %.6f, Lon %.6f%n", vehicle.getLatitude(), vehicle.getLongitude());
            pw.printf("  Cruising Speed                 : %.1f km/h%n", vehicle.getSpeedKmh());
            pw.printf("  Estimated Time to Arrival      : %.2f Minutes%n", vehicle.getEtaMinutes());
            pw.println();
            pw.println("--- [2] MATHEMATICAL EVALUATION & EFFICIENCY METRICS ---");
            pw.printf("  Category Priority Score        : %6.2f / 100.00%n", report.getPriorityScore());
            pw.printf("  Assigned Efficiency Tier       : %s (%s)%n",
                    report.getPriorityTier().getTierName(), report.getPriorityTier().getActionDescription());
            pw.printf("  Emergency Transit Efficiency   : %6.2f %% (ETE)%n", eteScore);
            pw.printf("  Congestion Mitigation Index    : %6.2f %% (CMI)%n", cmiScore);
            pw.println();
            pw.println("--- [3] CORRIDOR TRANSIT SUMMARY ---");
            pw.printf("  Total Intersections En Route   : %d%n", report.getTotalIntersections());
            pw.printf("  Preemption Overrides Triggered : %d%n", report.getOverriddenIntersections());
            pw.printf("  Total Elapsed Transit Time     : %d Seconds%n", report.getTotalTransitSeconds());
            pw.printf("  Mission Clearance Status       : %s%n", report.getCorridorStatus());
            pw.println();
            pw.println("==========================================================================================");
            pw.println(" End of Official College Audit Deliverable - Smart Traffic Network System");
            pw.println("==========================================================================================");
        }

        return targetFile.getAbsolutePath();
    }
}

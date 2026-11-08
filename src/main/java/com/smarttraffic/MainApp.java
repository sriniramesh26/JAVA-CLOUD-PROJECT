package com.smarttraffic;

import com.smarttraffic.database.DBConnection;
import com.smarttraffic.database.TrafficDAO;
import com.smarttraffic.models.*;
import com.smarttraffic.services.CorridorRouterService;
import com.smarttraffic.services.CsvGpsParser;
import com.smarttraffic.services.PriorityCalculator;
import com.smarttraffic.utils.PreemptionWriter;
import com.smarttraffic.utils.ValidationUtils;

import java.io.File;
import java.util.*;

/**
 * Main Application for Smart City Traffic Signal & Emergency Vehicle Green Corridor Router.
 * Supports interactive menu-driven console operations as well as automated (--auto) demonstration.
 */
public class MainApp {

    private static final String CSV_FILE = "emergency_gps.csv";
    private final TrafficDAO trafficDAO = new TrafficDAO();
    private final CsvGpsParser gpsParser = new CsvGpsParser();
    private final CorridorRouterService routerService = new CorridorRouterService();

    private List<EmergencyVehicle> loadedVehicles = new ArrayList<>();
    private final List<Intersection> cityIntersections = new ArrayList<>();
    private final List<PreemptionLog> sessionLogs = new ArrayList<>();
    private final List<CorridorReport> sessionReports = new ArrayList<>();

    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.initializeIntersections();

        if (args.length > 0 && "--auto".equalsIgnoreCase(args[0])) {
            app.runAutomatedPipeline();
        } else {
            app.runInteractiveMenu();
        }
    }

    private void initializeIntersections() {
        cityIntersections.clear();
        cityIntersections.add(new Intersection("INT-01", "MG Road - Brigade Junction", 12.9716, 77.5946, SignalState.RED, 84, true, 45, 5));
        cityIntersections.add(new Intersection("INT-02", "Residency Road - Richmond Circle", 12.9735, 77.5982, SignalState.RED, 45, false, 40, 4));
        cityIntersections.add(new Intersection("INT-03", "Hosur Road - Silk Board Junction", 12.9760, 77.6015, SignalState.RED, 72, true, 50, 6));
        cityIntersections.add(new Intersection("INT-04", "Old Airport Road - Domlur Flyover", 12.9810, 77.6090, SignalState.RED, 30, false, 35, 3));
        cityIntersections.add(new Intersection("INT-05", "Outer Ring Road - Marathahalli", 12.9845, 77.6150, SignalState.RED, 60, true, 45, 5));
        cityIntersections.add(new Intersection("INT-06", "Whitefield Main Road - Hope Farm", 12.9880, 77.6210, SignalState.RED, 20, false, 30, 3));
    }

    public void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=========================================================================");
            System.out.println("   SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER   ");
            System.out.println("=========================================================================");
            System.out.println(" 1. Test Database Connectivity (Local / OCI Remote VM 2)");
            System.out.println(" 2. Ingest GPS Telemetry from emergency_gps.csv");
            System.out.println(" 3. Validate Vehicle Beacon IDs (Regex: EMG-[A-Z]{3}-\\d{4})");
            System.out.println(" 4. Calculate Priority Scores & Efficiency Tiers");
            System.out.println(" 5. Run Multithreaded Green Corridor Simulation");
            System.out.println(" 6. Persist Simulation Results to MySQL Database");
            System.out.println(" 7. Query & Display Database Records (All 4 Tables)");
            System.out.println(" 8. Export Preemption Logs & Performance Summaries to TXT");
            System.out.println(" 9. Exit Application");
            System.out.println("=========================================================================");
            System.out.print(" Select an option (1-9): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> testDatabaseConnection();
                case "2" -> loadCsvTelemetry();
                case "3" -> validateBeaconIds();
                case "4" -> displayPriorityCalculations();
                case "5" -> runCorridorSimulation();
                case "6" -> persistToDatabase();
                case "7" -> displayDatabaseRecords();
                case "8" -> exportFiles();
                case "9" -> {
                    System.out.println("Exiting Smart City Traffic Router. Goodbye!");
                    routerService.shutdown();
                    running = false;
                }
                default -> System.out.println("[!] Invalid option. Please select 1 through 9.");
            }
        }
    }

    public void runAutomatedPipeline() {
        System.out.println("\n[SYSTEM] Running Automated End-to-End Green Corridor Demonstration...");
        System.out.println("=========================================================================");
        System.out.println("          STARTING END-TO-END CORRIDOR SIMULATION PIPELINE               ");
        System.out.println("=========================================================================");

        testDatabaseConnection();
        loadCsvTelemetry();
        validateBeaconIds();
        displayPriorityCalculations();
        runCorridorSimulation();
        persistToDatabase();
        exportFiles();

        System.out.println("\n=========================================================================");
        System.out.println(" [SUCCESS] Automated Green Corridor Pipeline Completed Successfully!     ");
        System.out.println("=========================================================================");
    }

    private void testDatabaseConnection() {
        System.out.println("\n--- [1] DATABASE CONNECTIVITY INSPECTION ---");
        System.out.printf("  Configured Details: %s%n", DBConnection.getConnectionInfo());
        System.out.println("  Testing connection...");

        if (DBConnection.testConnection()) {
            System.out.println("  [+] SUCCESS: Connected to MySQL database successfully!");
        } else {
            System.out.println("  [-] NOTICE: Database currently offline or unreachable.");
            System.out.println("      Standalone Simulation Mode is ACTIVE. All Java core simulation,");
            System.out.println("      multithreading, priority calculation, and TXT reporting work offline!");
        }
    }

    private void loadCsvTelemetry() {
        System.out.println("\n--- [2] LOADING TELEMETRY FROM CSV: " + CSV_FILE + " ---");
        try {
            File f = new File(CSV_FILE);
            if (!f.exists()) {
                System.err.println("  [!] Error: " + CSV_FILE + " not found in current directory!");
                return;
            }
            loadedVehicles = gpsParser.parseCsvFile(CSV_FILE);
            System.out.printf("  [+] Successfully loaded and validated %d emergency vehicles:%n%n", loadedVehicles.size());

            System.out.printf("  %-10s | %-15s | %-14s | %-18s | %11s | %9s%n",
                    "VEHICLE", "BEACON ID", "CATEGORY", "SEVERITY", "SPEED(km/h)", "ETA(min)");
            System.out.println("  -----------------------------------------------------------------------------------");
            for (EmergencyVehicle v : loadedVehicles) {
                System.out.printf("  %-10s | %-15s | %-14s | %-18s | %11.1f | %9.2f%n",
                        v.getVehicleId(), v.getBeaconId(), v.getCategory(), v.getSeverityCode(),
                        v.getSpeedKmh(), v.getEtaMinutes());
            }
        } catch (Exception e) {
            System.err.println("  [!] Error parsing CSV: " + e.getMessage());
        }
    }

    private void validateBeaconIds() {
        System.out.println("\n--- [3] BEACON CODE REGEX VALIDATION (EMG-[A-Z]{3}-\\d{4}) ---");
        if (loadedVehicles.isEmpty()) {
            loadCsvTelemetry();
        }
        for (EmergencyVehicle v : loadedVehicles) {
            boolean valid = ValidationUtils.isValidBeaconCode(v.getBeaconId());
            System.out.printf("  Vehicle: %-8s | Beacon: %-15s | Status: %s%n",
                    v.getVehicleId(), v.getBeaconId(), valid ? "[VALID]" : "[INVALID]");
        }
    }

    private void displayPriorityCalculations() {
        System.out.println("\n--- [4] EMERGENCY VEHICLE PRIORITY SCORING & TIER EVALUATION ---");
        if (loadedVehicles.isEmpty()) {
            loadCsvTelemetry();
        }
        System.out.printf("  %-10s | %-16s | %-15s | %-8s | %-20s%n",
                "VEHICLE", "CATEGORY", "FORMULA INPUTS", "SCORE", "ASSIGNED TIER");
        System.out.println("  ----------------------------------------------------------------------------------");
        for (EmergencyVehicle v : loadedVehicles) {
            double score = PriorityCalculator.calculateScore(v);
            PriorityTier tier = PriorityCalculator.evaluateTier(score, false);
            System.out.printf("  %-10s | %-16s | %4.0f, %4.0f, %4.0f   | %7.2f | %-20s%n",
                    v.getVehicleId(), v.getCategory().getDisplayName(),
                    v.getParam1(), v.getParam2(), v.getParam3(),
                    score, tier.getTierName());
        }
    }

    private void runCorridorSimulation() {
        System.out.println("\n--- [5] MULTITHREADED GREEN CORRIDOR SIMULATION ---");
        if (loadedVehicles.isEmpty()) {
            loadCsvTelemetry();
        }

        sessionLogs.clear();
        sessionReports.clear();

        for (EmergencyVehicle v : loadedVehicles) {
            System.out.printf("%n  >>> Dispatching %s (%s, ETA: %.2f min) <<<%n",
                    v.getVehicleId(), v.getCategory().getDisplayName(), v.getEtaMinutes());

            // Build corridor subset of intersections
            List<Intersection> corridor = new ArrayList<>(cityIntersections);
            CorridorRouterService.CorridorExecutionResult result = routerService.dispatchCorridor(v, corridor);

            sessionLogs.addAll(result.getLogs());
            sessionReports.add(result.getReport());

            System.out.printf("      [Result] Score: %.2f | Tier: %s | Transit: %ds | Overrides: %d/%d | Status: %s%n",
                    result.getReport().getPriorityScore(),
                    result.getReport().getPriorityTier().getTierName(),
                    result.getReport().getTotalTransitSeconds(),
                    result.getReport().getOverriddenIntersections(),
                    corridor.size(),
                    result.getReport().getCorridorStatus());
        }
    }

    private void persistToDatabase() {
        System.out.println("\n--- [6] PERSISTING SIMULATION RESULTS TO MYSQL DATABASE ---");
        if (!DBConnection.testConnection()) {
            System.out.println("  [-] Database offline. Skipping MySQL persistence (Standalone Mode).");
            return;
        }

        System.out.println("  [+] Persisting Intersections...");
        for (Intersection in : cityIntersections) {
            trafficDAO.insertOrUpdateIntersection(in);
        }

        System.out.println("  [+] Persisting Emergency Vehicles (Phase 1 Ingestion)...");
        for (EmergencyVehicle ev : loadedVehicles) {
            trafficDAO.insertOrUpdateVehicle(ev);
        }

        System.out.println("  [+] Persisting Preemption Logs (Phase 3 Clearance Audit)...");
        for (PreemptionLog log : sessionLogs) {
            trafficDAO.insertPreemptionLog(log);
        }

        System.out.println("  [+] Persisting Corridor Performance Reports...");
        for (CorridorReport rep : sessionReports) {
            trafficDAO.insertCorridorReport(rep);
        }

        System.out.println("  [SUCCESS] All records persisted into smart_traffic database successfully!");
    }

    private void displayDatabaseRecords() {
        System.out.println("\n--- [7] QUERYING MYSQL DATABASE RECORDS ---");
        if (!DBConnection.testConnection()) {
            System.out.println("  [-] Database is offline. Cannot query MySQL.");
            return;
        }

        System.out.println("\n[TABLE 1: emergency_vehicles]");
        List<EmergencyVehicle> vList = trafficDAO.getAllVehicles();
        vList.forEach(v -> System.out.printf("  %s | %s | %s | %s%n", v.getVehicleId(), v.getBeaconId(), v.getCategory(), v.getDestinationCode()));

        System.out.println("\n[TABLE 2: intersections]");
        List<Intersection> iList = trafficDAO.getAllIntersections();
        iList.forEach(i -> System.out.printf("  %s | %-32s | %s | Blocked=%s%n", i.getIntersectionId(), i.getName(), i.getCurrentSignalState(), i.isBlocked()));

        System.out.println("\n[TABLE 3: preemption_logs (Recent 5)]");
        List<PreemptionLog> lList = trafficDAO.getRecentLogs(5);
        lList.forEach(l -> System.out.printf("  #%d | %s @ %s | %s -> %s | Delay: %ds%n", l.getLogId(), l.getVehicleId(), l.getIntersectionId(), l.getPreviousSignal(), l.getNewSignal(), l.getClearanceDelaySec()));

        System.out.println("\n[TABLE 4: corridor_reports]");
        List<CorridorReport> rList = trafficDAO.getAllReports();
        rList.forEach(r -> System.out.printf("  %s | %s | Score: %.2f | Tier: %s | Transit: %ds%n", r.getReportId(), r.getVehicleId(), r.getPriorityScore(), r.getPriorityTier().getTierName(), r.getTotalTransitSeconds()));
    }

    private void exportFiles() {
        System.out.println("\n--- [8] EXPORTING EXECUTION LOGS & SUMMARIES (TXT) ---");
        try {
            if (!sessionLogs.isEmpty()) {
                String logPath = PreemptionWriter.exportExecutionLogs(sessionLogs);
                System.out.printf("  [+] Execution logs exported to : %s%n", logPath);
            }

            if (!sessionReports.isEmpty() && !loadedVehicles.isEmpty()) {
                EmergencyVehicle v = loadedVehicles.get(0);
                CorridorReport r = sessionReports.get(0);
                double ete = PriorityCalculator.calculateETE(120.0, r.getTotalTransitSeconds());
                double cmi = PriorityCalculator.calculateCMI(15.0, PriorityCalculator.STANDARD_SIGNAL_CYCLE_SEC);
                String repPath = PreemptionWriter.exportCorridorSummary(v, r, ete, cmi);
                System.out.printf("  [+] Summary report exported to : %s%n", repPath);
            }
        } catch (Exception e) {
            System.err.println("  [!] Error writing files: " + e.getMessage());
        }
    }
}

package com.smarttraffic;

import com.smarttraffic.models.*;
import com.smarttraffic.utils.PreemptionWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreemptionWriterTest {

    @Test
    @DisplayName("PreemptionWriter exports log file and corridor performance summary")
    void testFileExport() throws IOException {
        List<PreemptionLog> logs = new ArrayList<>();
        logs.add(new PreemptionLog("EV-101", "INT-01", 1.4, true, OverrideStatus.SUCCESS,
                SignalState.RED, SignalState.GREEN, 5, 50, "Worker-01", "Override applied"));

        String logPath = PreemptionWriter.exportExecutionLogs(logs);
        assertNotNull(logPath);
        File logFile = new File(logPath);
        assertTrue(logFile.exists());
        assertTrue(logFile.length() > 0);

        EmergencyVehicle ev = new EmergencyVehicle("EV-101", "EMG-AMB-1001", VehicleCategory.AMBULANCE,
                "CODE_RED", "HOSP-01", 12.97, 77.59, 68.5, "INT-01", 84, 1.4, 95.0, 85.0, 90.0);
        CorridorReport report = new CorridorReport("REP-01", "EV-101", VehicleCategory.AMBULANCE,
                91.0, PriorityTier.GOLD_CORRIDOR, 4, 3, 178, "COMPLETED", "Summary test");

        String sumPath = PreemptionWriter.exportCorridorSummary(ev, report, 85.0, 90.0);
        assertNotNull(sumPath);
        File sumFile = new File(sumPath);
        assertTrue(sumFile.exists());
        assertTrue(sumFile.length() > 0);
    }
}

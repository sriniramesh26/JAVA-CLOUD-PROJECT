package com.smarttraffic;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.services.CsvGpsParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvGpsParserTest {

    @Test
    @DisplayName("Parse valid CSV lines into EmergencyVehicle list")
    void testParseValidCsv() throws IOException {
        File tempFile = File.createTempFile("test_gps", ".csv");
        tempFile.deleteOnExit();

        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("vehicle_id,beacon_id,category,severity_code,destination_code,latitude,longitude,speed_kmh,intersection_id,camera_density,eta_minutes,param1,param2,param3\n");
            fw.write("EV-101,EMG-AMB-1001,AMBULANCE,CRITICAL_CODE_RED,HOSP-01,12.971600,77.594600,68.5,INT-01,84,1.4,95.0,85.0,90.0\n");
            fw.write("EV-201,EMG-FIR-2042,FIRE_ENGINE,INDUSTRIAL_FIRE,FIRE-03,12.976000,77.601500,58.0,INT-03,72,1.8,75.0,92.0,88.0\n");
        }

        CsvGpsParser parser = new CsvGpsParser();
        List<EmergencyVehicle> list = parser.parseCsvFile(tempFile.getAbsolutePath());

        assertEquals(2, list.size());
        assertEquals("EV-101", list.get(0).getVehicleId());
        assertEquals("EMG-AMB-1001", list.get(0).getBeaconId());
        assertEquals(68.5, list.get(0).getSpeedKmh());
        assertEquals(1.4, list.get(0).getEtaMinutes());
    }

    @Test
    @DisplayName("Handles non-existent file gracefully")
    void testFileNotFound() {
        CsvGpsParser parser = new CsvGpsParser();
        assertThrows(IOException.class, () -> parser.parseCsvFile("non_existent_file.csv"));
    }
}

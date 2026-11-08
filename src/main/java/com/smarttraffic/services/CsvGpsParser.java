package com.smarttraffic.services;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.utils.ValidationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses GPS telemetry and vehicle dispatch data from emergency_gps.csv.
 */
public class CsvGpsParser {

    /**
     * Reads and parses emergency_gps.csv into a list of EmergencyVehicle objects.
     */
    public List<EmergencyVehicle> parseCsvFile(String filePath) throws IOException {
        List<EmergencyVehicle> vehicles = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (isHeader) {
                    isHeader = false;
                    // Skip CSV header
                    continue;
                }

                String[] tokens = ValidationUtils.parseCsvLine(line);
                if (tokens.length < 14) {
                    System.err.printf("[CsvGpsParser] Skipping malformed row (< 14 columns): %s%n", line);
                    continue;
                }

                try {
                    String vehicleId = tokens[0];
                    String beaconId = tokens[1];
                    String categoryStr = tokens[2];
                    String severityCode = tokens[3];
                    String destinationCode = tokens[4];
                    double latitude = Double.parseDouble(tokens[5]);
                    double longitude = Double.parseDouble(tokens[6]);
                    double speedKmh = Double.parseDouble(tokens[7]);
                    String intersectionId = tokens[8];
                    int cameraDensity = Integer.parseInt(tokens[9]);
                    double etaMinutes = Double.parseDouble(tokens[10]);
                    double param1 = Double.parseDouble(tokens[11]);
                    double param2 = Double.parseDouble(tokens[12]);
                    double param3 = Double.parseDouble(tokens[13]);

                    if (!ValidationUtils.isValidBeaconCode(beaconId)) {
                        System.err.printf("[CsvGpsParser] Invalid Beacon ID '%s' for vehicle %s (Format must match EMG-[A-Z]{3}-\\d{4})%n",
                                beaconId, vehicleId);
                    }

                    VehicleCategory category = VehicleCategory.fromString(categoryStr);

                    EmergencyVehicle ev = new EmergencyVehicle(
                            vehicleId, beaconId, category, severityCode, destinationCode,
                            latitude, longitude, speedKmh, intersectionId, cameraDensity,
                            etaMinutes, param1, param2, param3
                    );

                    vehicles.add(ev);

                } catch (NumberFormatException e) {
                    System.err.printf("[CsvGpsParser] Number format error in row: %s (%s)%n", line, e.getMessage());
                }
            }
        }

        return vehicles;
    }
}

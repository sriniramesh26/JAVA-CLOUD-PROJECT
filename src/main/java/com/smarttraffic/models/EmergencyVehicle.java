package com.smarttraffic.models;

/**
 * Emergency Vehicle Data Model representing Phase 1 Dispatch & Phase 2 Telemetry.
 */
public class EmergencyVehicle {
    private String vehicleId;
    private String beaconId;
    private VehicleCategory category;
    private String severityCode;
    private String destinationCode;
    private double latitude;
    private double longitude;
    private double speedKmh;
    private String intersectionId;
    private int cameraDensity;
    private double etaMinutes;
    private double param1;
    private double param2;
    private double param3;

    public EmergencyVehicle() {}

    public EmergencyVehicle(String vehicleId, String beaconId, VehicleCategory category,
                            String severityCode, String destinationCode, double latitude,
                            double longitude, double speedKmh, String intersectionId,
                            int cameraDensity, double etaMinutes, double param1,
                            double param2, double param3) {
        this.vehicleId = vehicleId;
        this.beaconId = beaconId;
        this.category = category;
        this.severityCode = severityCode;
        this.destinationCode = destinationCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.intersectionId = intersectionId;
        this.cameraDensity = cameraDensity;
        this.etaMinutes = etaMinutes;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getBeaconId() { return beaconId; }
    public void setBeaconId(String beaconId) { this.beaconId = beaconId; }

    public VehicleCategory getCategory() { return category; }
    public void setCategory(VehicleCategory category) { this.category = category; }

    public String getSeverityCode() { return severityCode; }
    public void setSeverityCode(String severityCode) { this.severityCode = severityCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(double speedKmh) { this.speedKmh = speedKmh; }

    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }

    public int getCameraDensity() { return cameraDensity; }
    public void setCameraDensity(int cameraDensity) { this.cameraDensity = cameraDensity; }

    public double getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(double etaMinutes) { this.etaMinutes = etaMinutes; }

    public double getParam1() { return param1; }
    public void setParam1(double param1) { this.param1 = param1; }

    public double getParam2() { return param2; }
    public void setParam2(double param2) { this.param2 = param2; }

    public double getParam3() { return param3; }
    public void setParam3(double param3) { this.param3 = param3; }

    @Override
    public String toString() {
        return String.format("Vehicle[%s, Beacon=%s, %s, Speed=%.1f km/h, ETA=%.2f min]",
                vehicleId, beaconId, category, speedKmh, etaMinutes);
    }
}

package com.smarttraffic.models;

/**
 * Physical intersection model with signal telemetry.
 */
public class Intersection {
    private String intersectionId;
    private String name;
    private double latitude;
    private double longitude;
    private SignalState currentSignalState;
    private int cameraDensityCount;
    private boolean isBlocked;
    private int greenDurationSec;
    private int clearanceDelaySec;

    public Intersection() {
        this.currentSignalState = SignalState.RED;
        this.greenDurationSec = 45;
        this.clearanceDelaySec = 5;
    }

    public Intersection(String intersectionId, String name, double latitude, double longitude,
                        SignalState currentSignalState, int cameraDensityCount, boolean isBlocked,
                        int greenDurationSec, int clearanceDelaySec) {
        this.intersectionId = intersectionId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentSignalState = currentSignalState;
        this.cameraDensityCount = cameraDensityCount;
        this.isBlocked = isBlocked;
        this.greenDurationSec = greenDurationSec;
        this.clearanceDelaySec = clearanceDelaySec;
    }

    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public SignalState getCurrentSignalState() { return currentSignalState; }
    public void setCurrentSignalState(SignalState currentSignalState) { this.currentSignalState = currentSignalState; }

    public int getCameraDensityCount() { return cameraDensityCount; }
    public void setCameraDensityCount(int cameraDensityCount) { this.cameraDensityCount = cameraDensityCount; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public int getGreenDurationSec() { return greenDurationSec; }
    public void setGreenDurationSec(int greenDurationSec) { this.greenDurationSec = greenDurationSec; }

    public int getClearanceDelaySec() { return clearanceDelaySec; }
    public void setClearanceDelaySec(int clearanceDelaySec) { this.clearanceDelaySec = clearanceDelaySec; }

    @Override
    public String toString() {
        return String.format("Intersection[%s - %s, Signal=%s, Blocked=%s, Density=%d]",
                intersectionId, name, currentSignalState, isBlocked, cameraDensityCount);
    }
}

package com.smarttraffic.database;

import com.smarttraffic.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Smart City Traffic Database.
 * Implements CRUD operations for all 4 schema tables:
 * - emergency_vehicles
 * - intersections
 * - preemption_logs
 * - corridor_reports
 */
public class TrafficDAO {

    // -------------------------------------------------------------------------
    // 1. Table: emergency_vehicles
    // -------------------------------------------------------------------------
    public boolean insertOrUpdateVehicle(EmergencyVehicle ev) {
        String sql = """
            INSERT INTO emergency_vehicles 
            (vehicle_id, beacon_id, category, severity_code, destination_code, speed_kmh, latitude, longitude)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                speed_kmh = VALUES(speed_kmh),
                latitude = VALUES(latitude),
                longitude = VALUES(longitude),
                destination_code = VALUES(destination_code);
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ev.getVehicleId());
            ps.setString(2, ev.getBeaconId());
            ps.setString(3, ev.getCategory().name());
            ps.setString(4, ev.getSeverityCode());
            ps.setString(5, ev.getDestinationCode());
            ps.setDouble(6, ev.getSpeedKmh());
            ps.setDouble(7, ev.getLatitude());
            ps.setDouble(8, ev.getLongitude());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("[TrafficDAO] Error inserting vehicle %s: %s%n", ev.getVehicleId(), e.getMessage());
            return false;
        }
    }

    public List<EmergencyVehicle> getAllVehicles() {
        List<EmergencyVehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM emergency_vehicles ORDER BY vehicle_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmergencyVehicle ev = new EmergencyVehicle();
                ev.setVehicleId(rs.getString("vehicle_id"));
                ev.setBeaconId(rs.getString("beacon_id"));
                ev.setCategory(VehicleCategory.fromString(rs.getString("category")));
                ev.setSeverityCode(rs.getString("severity_code"));
                ev.setDestinationCode(rs.getString("destination_code"));
                ev.setSpeedKmh(rs.getDouble("speed_kmh"));
                ev.setLatitude(rs.getDouble("latitude"));
                ev.setLongitude(rs.getDouble("longitude"));
                list.add(ev);
            }
        } catch (SQLException e) {
            System.err.println("[TrafficDAO] Error fetching vehicles: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // 2. Table: intersections
    // -------------------------------------------------------------------------
    public boolean insertOrUpdateIntersection(Intersection in) {
        String sql = """
            INSERT INTO intersections
            (intersection_id, name, latitude, longitude, current_signal_state, camera_density_count, is_blocked, green_duration_sec, clearance_delay_sec)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                current_signal_state = VALUES(current_signal_state),
                camera_density_count = VALUES(camera_density_count),
                is_blocked = VALUES(is_blocked),
                green_duration_sec = VALUES(green_duration_sec),
                clearance_delay_sec = VALUES(clearance_delay_sec);
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, in.getIntersectionId());
            ps.setString(2, in.getName());
            ps.setDouble(3, in.getLatitude());
            ps.setDouble(4, in.getLongitude());
            ps.setString(5, in.getCurrentSignalState().name());
            ps.setInt(6, in.getCameraDensityCount());
            ps.setBoolean(7, in.isBlocked());
            ps.setInt(8, in.getGreenDurationSec());
            ps.setInt(9, in.getClearanceDelaySec());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("[TrafficDAO] Error inserting intersection %s: %s%n", in.getIntersectionId(), e.getMessage());
            return false;
        }
    }

    public List<Intersection> getAllIntersections() {
        List<Intersection> list = new ArrayList<>();
        String sql = "SELECT * FROM intersections ORDER BY intersection_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Intersection in = new Intersection(
                        rs.getString("intersection_id"),
                        rs.getString("name"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        SignalState.valueOf(rs.getString("current_signal_state")),
                        rs.getInt("camera_density_count"),
                        rs.getBoolean("is_blocked"),
                        rs.getInt("green_duration_sec"),
                        rs.getInt("clearance_delay_sec")
                );
                list.add(in);
            }
        } catch (SQLException e) {
            System.err.println("[TrafficDAO] Error fetching intersections: " + e.getMessage());
        }
        return list;
    }

    public boolean updateSignalState(String intersectionId, SignalState newState, boolean isBlocked) {
        String sql = "UPDATE intersections SET current_signal_state = ?, is_blocked = ? WHERE intersection_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newState.name());
            ps.setBoolean(2, isBlocked);
            ps.setString(3, intersectionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("[TrafficDAO] Error updating intersection %s: %s%n", intersectionId, e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // 3. Table: preemption_logs
    // -------------------------------------------------------------------------
    public boolean insertPreemptionLog(PreemptionLog log) {
        String sql = """
            INSERT INTO preemption_logs
            (vehicle_id, intersection_id, eta_minutes, override_triggered, override_status, 
             previous_signal, new_signal, clearance_delay_sec, transit_duration_sec, thread_name, log_message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, log.getVehicleId());
            ps.setString(2, log.getIntersectionId());
            ps.setDouble(3, log.getEtaMinutes());
            ps.setBoolean(4, log.isOverrideTriggered());
            ps.setString(5, log.getOverrideStatus().name());
            ps.setString(6, log.getPreviousSignal().name());
            ps.setString(7, log.getNewSignal().name());
            ps.setInt(8, log.getClearanceDelaySec());
            ps.setInt(9, log.getTransitDurationSec());
            ps.setString(10, log.getThreadName());
            ps.setString(11, log.getLogMessage());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        log.setLogId(keys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.printf("[TrafficDAO] Error inserting preemption log: %s%n", e.getMessage());
            return false;
        }
    }

    public List<PreemptionLog> getRecentLogs(int limit) {
        List<PreemptionLog> list = new ArrayList<>();
        String sql = "SELECT * FROM preemption_logs ORDER BY log_id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PreemptionLog l = new PreemptionLog();
                    l.setLogId(rs.getLong("log_id"));
                    l.setVehicleId(rs.getString("vehicle_id"));
                    l.setIntersectionId(rs.getString("intersection_id"));
                    l.setEtaMinutes(rs.getDouble("eta_minutes"));
                    l.setOverrideTriggered(rs.getBoolean("override_triggered"));
                    l.setOverrideStatus(OverrideStatus.valueOf(rs.getString("override_status")));
                    l.setPreviousSignal(SignalState.valueOf(rs.getString("previous_signal")));
                    l.setNewSignal(SignalState.valueOf(rs.getString("new_signal")));
                    l.setClearanceDelaySec(rs.getInt("clearance_delay_sec"));
                    l.setTransitDurationSec(rs.getInt("transit_duration_sec"));
                    l.setThreadName(rs.getString("thread_name"));
                    l.setLogMessage(rs.getString("log_message"));
                    Timestamp ts = rs.getTimestamp("logged_at");
                    if (ts != null) {
                        l.setLoggedAt(ts.toLocalDateTime());
                    }
                    list.add(l);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TrafficDAO] Error fetching preemption logs: " + e.getMessage());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // 4. Table: corridor_reports
    // -------------------------------------------------------------------------
    public boolean insertCorridorReport(CorridorReport report) {
        String sql = """
            INSERT INTO corridor_reports
            (report_id, vehicle_id, category, priority_score, priority_tier, 
             total_intersections, overridden_intersections, total_transit_seconds, corridor_status, summary_text)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                priority_score = VALUES(priority_score),
                priority_tier = VALUES(priority_tier),
                overridden_intersections = VALUES(overridden_intersections),
                total_transit_seconds = VALUES(total_transit_seconds),
                corridor_status = VALUES(corridor_status),
                summary_text = VALUES(summary_text);
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, report.getReportId());
            ps.setString(2, report.getVehicleId());
            ps.setString(3, report.getCategory().name());
            ps.setDouble(4, report.getPriorityScore());
            ps.setString(5, report.getPriorityTier().name());
            ps.setInt(6, report.getTotalIntersections());
            ps.setInt(7, report.getOverriddenIntersections());
            ps.setInt(8, report.getTotalTransitSeconds());
            ps.setString(9, report.getCorridorStatus());
            ps.setString(10, report.getSummaryText());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.printf("[TrafficDAO] Error inserting corridor report: %s%n", e.getMessage());
            return false;
        }
    }

    public List<CorridorReport> getAllReports() {
        List<CorridorReport> list = new ArrayList<>();
        String sql = "SELECT * FROM corridor_reports ORDER BY generated_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CorridorReport r = new CorridorReport();
                r.setReportId(rs.getString("report_id"));
                r.setVehicleId(rs.getString("vehicle_id"));
                r.setCategory(VehicleCategory.fromString(rs.getString("category")));
                r.setPriorityScore(rs.getDouble("priority_score"));
                r.setPriorityTier(PriorityTier.valueOf(rs.getString("priority_tier")));
                r.setTotalIntersections(rs.getInt("total_intersections"));
                r.setOverriddenIntersections(rs.getInt("overridden_intersections"));
                r.setTotalTransitSeconds(rs.getInt("total_transit_seconds"));
                r.setCorridorStatus(rs.getString("corridor_status"));
                r.setSummaryText(rs.getString("summary_text"));
                Timestamp ts = rs.getTimestamp("generated_at");
                if (ts != null) {
                    r.setGeneratedAt(ts.toLocalDateTime());
                }
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("[TrafficDAO] Error fetching corridor reports: " + e.getMessage());
        }
        return list;
    }
}

-- ============================================================================
-- SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
-- Verification Script: verify_database.sql
-- Run by Student 2 (Database Host) to verify DB deployment & data integrity
-- ============================================================================

USE smart_traffic;

-- 1. Verify Tables in smart_traffic Database
SELECT '--- 1. VERIFYING REGISTERED TABLES ---' AS check_step;
SHOW FULL TABLES IN smart_traffic;

-- 2. Verify Table Row Counts
SELECT '--- 2. VERIFYING TABLE ROW COUNTS ---' AS check_step;
SELECT 
    (SELECT COUNT(*) FROM emergency_vehicles) AS emergency_vehicles_count,
    (SELECT COUNT(*) FROM intersections)      AS intersections_count,
    (SELECT COUNT(*) FROM preemption_logs)    AS preemption_logs_count,
    (SELECT COUNT(*) FROM corridor_reports)   AS corridor_reports_count;

-- 3. Verify Foreign Keys and Constraints
SELECT '--- 3. VERIFYING TABLE FOREIGN KEY CONSTRAINTS ---' AS check_step;
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    CONSTRAINT_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE 
    TABLE_SCHEMA = 'smart_traffic' 
    AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 4. Verify Remote User Access Privileges
SELECT '--- 4. VERIFYING REMOTE USER ACCESS PRIVILEGES ---' AS check_step;
SHOW GRANTS;

-- 5. Inspect Latest Emergency Vehicles (Phase 1 Ingestion)
SELECT '--- 5. INSPECTING EMERGENCY VEHICLES (PHASE 1) ---' AS check_step;
SELECT vehicle_id, beacon_id, category, severity_code, destination_code, speed_kmh FROM emergency_vehicles;

-- 6. Inspect Preemption Logs (Phase 3 Clearance Audit)
SELECT '--- 6. INSPECTING RECENT PREEMPTION LOGS (PHASE 3) ---' AS check_step;
SELECT log_id, vehicle_id, intersection_id, eta_minutes, override_status, previous_signal, new_signal, clearance_delay_sec, thread_name, logged_at 
FROM preemption_logs 
ORDER BY log_id DESC 
LIMIT 10;

-- 7. Inspect Corridor Performance Reports & Rating Tiers
SELECT '--- 7. INSPECTING CORRIDOR SUMMARY REPORTS & TIERS ---' AS check_step;
SELECT report_id, vehicle_id, category, priority_score, priority_tier, overridden_intersections, total_transit_seconds, corridor_status 
FROM corridor_reports;

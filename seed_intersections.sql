-- ============================================================================
-- SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
-- Seed Script: seed_intersections.sql
-- Pre-populates city intersection nodes required for foreign key integrity
-- ============================================================================

USE smart_traffic;

INSERT INTO intersections (intersection_id, name, latitude, longitude, current_signal_state, camera_density_count, is_blocked, green_duration_sec, clearance_delay_sec)
VALUES 
    ('INT-01', 'MG Road - Brigade Junction', 12.9716000, 77.5946000, 'RED', 84, TRUE, 45, 5),
    ('INT-02', 'Residency Road - Richmond Circle', 12.9735000, 77.5982000, 'RED', 45, FALSE, 40, 4),
    ('INT-03', 'Hosur Road - Silk Board Junction', 12.9760000, 77.6015000, 'RED', 72, TRUE, 50, 6),
    ('INT-04', 'Old Airport Road - Domlur Flyover', 12.9810000, 77.6090000, 'RED', 30, FALSE, 35, 3),
    ('INT-05', 'Outer Ring Road - Marathahalli', 12.9845000, 77.6150000, 'RED', 60, TRUE, 45, 5),
    ('INT-06', 'Whitefield Main Road - Hope Farm', 12.9880000, 77.6210000, 'RED', 20, FALSE, 30, 3)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    latitude = VALUES(latitude),
    longitude = VALUES(longitude),
    camera_density_count = VALUES(camera_density_count),
    is_blocked = VALUES(is_blocked);

SELECT 'Intersections seeded successfully:' AS status;
SELECT intersection_id, name, current_signal_state, is_blocked, camera_density_count FROM intersections;

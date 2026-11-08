package com.smarttraffic;

import com.smarttraffic.database.DBConnection;
import com.smarttraffic.database.TrafficDAO;
import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionAndDAOTest {

    @Test
    @DisplayName("DBConnection returns valid connection string and user")
    void testConnectionParameters() {
        String info = DBConnection.getConnectionInfo();
        assertNotNull(info);
        assertTrue(info.contains("URL:"));
        assertTrue(info.contains("User:"));
        assertNotNull(DBConnection.getDbUrl());
        assertNotNull(DBConnection.getDbUser());
    }

    @Test
    @DisplayName("TrafficDAO handles offline database gracefully")
    void testDaoOfflineGracefulHandling() {
        TrafficDAO dao = new TrafficDAO();
        // If DB is offline, methods return empty lists or false without crashing
        List<EmergencyVehicle> vehicles = dao.getAllVehicles();
        assertNotNull(vehicles);

        List<Intersection> intersections = dao.getAllIntersections();
        assertNotNull(intersections);
    }
}

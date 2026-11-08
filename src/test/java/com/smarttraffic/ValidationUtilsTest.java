package com.smarttraffic;

import com.smarttraffic.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    @DisplayName("Valid beacon codes matching EMG-[A-Z]{3}-\\d{4}")
    void testValidBeaconCodes() {
        assertTrue(ValidationUtils.isValidBeaconCode("EMG-AMB-1001"));
        assertTrue(ValidationUtils.isValidBeaconCode("EMG-FIR-2042"));
        assertTrue(ValidationUtils.isValidBeaconCode("EMG-POL-3099"));
        assertTrue(ValidationUtils.isValidBeaconCode("EMG-XYZ-0001"));
    }

    @Test
    @DisplayName("Invalid beacon codes failing regex")
    void testInvalidBeaconCodes() {
        assertFalse(ValidationUtils.isValidBeaconCode("EMG-AMB-100"));    // 3 digits
        assertFalse(ValidationUtils.isValidBeaconCode("EMG-amb-1001"));   // lowercase
        assertFalse(ValidationUtils.isValidBeaconCode("AMB-1001"));       // missing prefix
        assertFalse(ValidationUtils.isValidBeaconCode("EMG-AMBU-1001"));  // 4 letters
        assertFalse(ValidationUtils.isValidBeaconCode(null));             // null
        assertFalse(ValidationUtils.isValidBeaconCode(""));               // empty
    }

    @Test
    @DisplayName("CSV line parser trims whitespace")
    void testParseCsvLine() {
        String line = " EV-101 , EMG-AMB-1001 , AMBULANCE , CRITICAL ";
        String[] tokens = ValidationUtils.parseCsvLine(line);
        assertEquals(4, tokens.length);
        assertEquals("EV-101", tokens[0]);
        assertEquals("EMG-AMB-1001", tokens[1]);
        assertEquals("AMBULANCE", tokens[2]);
        assertEquals("CRITICAL", tokens[3]);
    }

    @Test
    @DisplayName("Format override log outputs structured message")
    void testFormatOverrideLog() {
        String log = ValidationUtils.formatOverrideLog("EV-101", "INT-01", "RED", "GREEN", true, 1.4, 5);
        assertNotNull(log);
        assertTrue(log.contains("EV-101"));
        assertTrue(log.contains("INT-01"));
        assertTrue(log.contains("RED"));
        assertTrue(log.contains("GREEN"));
        assertTrue(log.contains("TRUE"));
    }
}

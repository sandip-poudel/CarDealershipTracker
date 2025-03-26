package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Vehicle class and its subclasses using Arrange-Act-Assert pattern.
 * Tests focus on the rental functionality and vehicle type behaviors.
 */
public class VehicleTest {

    // Test objects
    private SUV suv;
    private Sedan sedan;
    private SportsCar sportsCar;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    void setUp() {
        // Create fresh instances for each test
        suv = new SUV();
        sedan = new Sedan();
        sportsCar = new SportsCar();

        // Set up rental dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MARCH, 25);
        startDate = calendar.getTime();

        calendar.set(2025, Calendar.MARCH, 30);
        endDate = calendar.getTime();
    }

    @Test
    @DisplayName("Regular vehicles can be rented")
    void testRegularVehicleRental() {
        // Arrange
        suv.setVehicleId("SUV123");
        suv.setManufacturer("Toyota");
        suv.setModel("RAV4");
        assertFalse(suv.isRented());

        // Act
        boolean rentalResult = suv.rent(startDate, endDate);

        // Assert
        assertTrue(rentalResult, "Rental should succeed for regular vehicles");
        assertTrue(suv.isRented(), "Vehicle should be marked as rented");
        assertEquals(startDate, suv.getRentalStartDate(), "Start date should match");
        assertEquals(endDate, suv.getRentalEndDate(), "End date should match");
    }

    @Test
    @DisplayName("Sports cars cannot be rented")
    void testSportsCarRental() {
        // Arrange
        sportsCar.setVehicleId("SPORTS123");
        sportsCar.setManufacturer("Mazda");
        sportsCar.setModel("Miata");

        // Act
        boolean rentalResult = sportsCar.rent(startDate, endDate);

        // Assert
        assertFalse(rentalResult, "Rental should fail for sports cars");
        assertFalse(sportsCar.isRented(), "Sports car should not be marked as rented");
        assertNull(sportsCar.getRentalStartDate(), "Start date should remain null");
        assertNull(sportsCar.getRentalEndDate(), "End date should remain null");
    }

    @Test
    @DisplayName("Already rented vehicles cannot be rented again")
    void testDoubleRental() {
        // Arrange
        sedan.setVehicleId("SEDAN123");
        sedan.setManufacturer("Honda");
        sedan.setModel("Accord");
        assertTrue(sedan.rent(startDate, endDate), "First rental should succeed");

        // Act
        boolean secondRentalResult = sedan.rent(startDate, endDate);

        // Assert
        assertFalse(secondRentalResult, "Second rental should fail");
    }

    @Test
    @DisplayName("Rented vehicles can be returned")
    void testVehicleReturn() {
        // Arrange
        sedan.setVehicleId("SEDAN123");
        sedan.rent(startDate, endDate);
        assertTrue(sedan.isRented(), "Vehicle should be rented before return");

        // Act
        boolean returnResult = sedan.returnVehicle();

        // Assert
        assertTrue(returnResult, "Return should succeed");
        assertFalse(sedan.isRented(), "Vehicle should no longer be rented");
    }

    @Test
    @DisplayName("Cannot return vehicles that aren't rented")
    void testReturnUnrentedVehicle() {
        // Arrange
        sedan.setVehicleId("SEDAN123");
        assertFalse(sedan.isRented(), "Vehicle should not be rented initially");

        // Act
        boolean returnResult = sedan.returnVehicle();

        // Assert
        assertFalse(returnResult, "Return should fail for unrented vehicle");
    }

    @Test
    @DisplayName("Vehicle metadata can store additional information")
    void testVehicleMetadata() {
        // Arrange
        suv.setVehicleId("SUV123");

        // Act
        suv.getMetadata().put("dealer_name", "Luxury Motors");
        suv.getMetadata().put("color", "Midnight Blue");

        // Assert
        assertEquals("Luxury Motors", suv.getMetadata().get("dealer_name"));
        assertEquals("Midnight Blue", suv.getMetadata().get("color"));
    }
}

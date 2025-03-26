package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Dealership class using Arrange-Act-Assert pattern.
 * Tests focus on inventory management, vehicle rental, and vehicle transfers.
 */
public class DealershipTest {

    // Test objects
    private Dealership dealership;
    private Dealership targetDealership;
    private Vehicle suv;
    private Vehicle sedan;
    private Vehicle sportsCar;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    void setUp() {
        // Create fresh dealerships for each test
        dealership = new Dealership("D001", "Test Motors");
        targetDealership = new Dealership("D002", "Target Motors");

        // Create test vehicles
        suv = new SUV();
        suv.setVehicleId("SUV001");
        suv.setManufacturer("Toyota");
        suv.setModel("RAV4");
        suv.setPrice(28000.0);
        suv.setDealerId("D001");
        suv.setAcquisitionDate(new Date());

        sedan = new Sedan();
        sedan.setVehicleId("SEDAN001");
        sedan.setManufacturer("Honda");
        sedan.setModel("Accord");
        sedan.setPrice(26000.0);
        sedan.setDealerId("D001");
        sedan.setAcquisitionDate(new Date());

        sportsCar = new SportsCar();
        sportsCar.setVehicleId("SPORTS001");
        sportsCar.setManufacturer("Mazda");
        sportsCar.setModel("Miata");
        sportsCar.setPrice(30000.0);
        sportsCar.setDealerId("D001");
        sportsCar.setAcquisitionDate(new Date());

        // Set up rental dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MARCH, 25);
        startDate = calendar.getTime();

        calendar.set(2025, Calendar.MARCH, 30);
        endDate = calendar.getTime();
    }

    @Test
    @DisplayName("Adding vehicles to dealership inventory")
    void testAddVehicle() {
        // Arrange - setUp already created dealership and vehicles

        // Act - Add vehicles to dealership
        boolean addResult1 = dealership.addVehicle(suv);
        boolean addResult2 = dealership.addVehicle(sedan);

        // Assert - Verify vehicles were added
        assertTrue(addResult1, "First vehicle should be added successfully");
        assertTrue(addResult2, "Second vehicle should be added successfully");
        assertEquals(2, dealership.getVehicles().size(), "Dealership should have 2 vehicles");
        assertTrue(dealership.getVehicles().contains(suv), "Dealership should contain the SUV");
        assertTrue(dealership.getVehicles().contains(sedan), "Dealership should contain the sedan");
    }

    @Test
    @DisplayName("Duplicate vehicles are not added to inventory")
    void testAddDuplicateVehicle() {
        // Arrange - Add a vehicle first
        dealership.addVehicle(suv);
        assertEquals(1, dealership.getVehicles().size(), "Should have 1 vehicle initially");

        // Create duplicate with same ID
        Vehicle duplicateSuv = new SUV();
        duplicateSuv.setVehicleId("SUV001"); // Same ID
        duplicateSuv.setManufacturer("Ford");
        duplicateSuv.setModel("Explorer");

        // Act - Try to add duplicate
        boolean result = dealership.addVehicle(duplicateSuv);

        // Assert - Verify duplicate was rejected
        assertFalse(result, "Adding duplicate vehicle should fail");
        assertEquals(1, dealership.getVehicles().size(), "Inventory size should remain unchanged");
    }

    @Test
    @DisplayName("Enabling and disabling vehicle acquisition")
    void testAcquisitionToggle() {
        // Arrange - setUp created dealership with acquisition enabled by default
        assertTrue(dealership.isAcquisitionEnabled(), "Acquisition should be enabled by default");

        // Act - Disable acquisition
        dealership.disableAcquisition();

        // Assert - Verify acquisition is disabled
        assertFalse(dealership.isAcquisitionEnabled(), "Acquisition should be disabled");

        // Act again - Re-enable acquisition
        dealership.enableAcquisition();

        // Assert again - Verify acquisition is re-enabled
        assertTrue(dealership.isAcquisitionEnabled(), "Acquisition should be enabled again");
    }

    @Test
    @DisplayName("Renting vehicles updates their status correctly")
    void testRentVehicle() {
        // Arrange - Add vehicle to dealership
        dealership.addVehicle(sedan);

        // Act - Rent the vehicle
        boolean rentResult = dealership.rentVehicle(sedan.getVehicleId(), startDate, endDate);

        // Assert - Verify rental status
        assertTrue(rentResult, "Rental should succeed");
        assertTrue(sedan.isRented(), "Vehicle should be marked as rented");
        assertEquals(startDate, sedan.getRentalStartDate(), "Start date should match");
        assertEquals(endDate, sedan.getRentalEndDate(), "End date should match");
    }

    @Test
    @DisplayName("Sports cars cannot be rented")
    void testRentSportsCar() {
        // Arrange - Add sports car to dealership
        dealership.addVehicle(sportsCar);

        // Act - Attempt to rent the sports car
        boolean rentResult = dealership.rentVehicle(sportsCar.getVehicleId(), startDate, endDate);

        // Assert - Verify rental failed
        assertFalse(rentResult, "Sports car rental should fail");
        assertFalse(sportsCar.isRented(), "Sports car should not be marked as rented");
    }

    @Test
    @DisplayName("Cannot rent non-existent vehicles")
    void testRentNonExistentVehicle() {
        // Arrange - Dealership with no vehicles

        // Act - Try to rent a non-existent vehicle
        boolean rentResult = dealership.rentVehicle("NONEXISTENT", startDate, endDate);

        // Assert - Verify rental failed
        assertFalse(rentResult, "Renting non-existent vehicle should fail");
    }

    @Test
    @DisplayName("Returning rented vehicles")
    void testReturnVehicle() {
        // Arrange - Add and rent a vehicle
        dealership.addVehicle(sedan);
        dealership.rentVehicle(sedan.getVehicleId(), startDate, endDate);
        assertTrue(sedan.isRented(), "Vehicle should be rented for test setup");

        // Act - Return the vehicle
        boolean returnResult = dealership.returnVehicle(sedan.getVehicleId());

        // Assert - Verify vehicle was returned
        assertTrue(returnResult, "Return should succeed");
        assertFalse(sedan.isRented(), "Vehicle should no longer be rented");
    }

    @Test
    @DisplayName("Cannot return vehicles that aren't rented")
    void testReturnUnrentedVehicle() {
        // Arrange - Add vehicle without renting it
        dealership.addVehicle(suv);
        assertFalse(suv.isRented(), "Vehicle should not be rented initially");

        // Act - Try to return unrented vehicle
        boolean returnResult = dealership.returnVehicle(suv.getVehicleId());

        // Assert - Verify return failed
        assertFalse(returnResult, "Returning unrented vehicle should fail");
    }

    @Test
    @DisplayName("Transferring vehicles between dealerships")
    void testTransferVehicle() {
        // Arrange - Add vehicle to source dealership
        dealership.addVehicle(suv);
        assertEquals(1, dealership.getVehicles().size(), "Source should have 1 vehicle");
        assertEquals(0, targetDealership.getVehicles().size(), "Target should have 0 vehicles");

        // Act - Transfer vehicle
        boolean transferResult = dealership.transferVehicle(suv.getVehicleId(), targetDealership);

        // Assert - Verify transfer
        assertTrue(transferResult, "Transfer should succeed");
        assertEquals(0, dealership.getVehicles().size(), "Source should have 0 vehicles after transfer");
        assertEquals(1, targetDealership.getVehicles().size(), "Target should have 1 vehicle after transfer");
        assertEquals("D002", suv.getDealerId(), "Vehicle dealer ID should be updated to target ID");
    }

    @Test
    @DisplayName("Cannot transfer rented vehicles")
    void testTransferRentedVehicle() {
        // Arrange - Add and rent a vehicle
        dealership.addVehicle(sedan);
        dealership.rentVehicle(sedan.getVehicleId(), startDate, endDate);
        assertTrue(sedan.isRented(), "Vehicle should be rented for test setup");

        // Act - Try to transfer rented vehicle
        boolean transferResult = dealership.transferVehicle(sedan.getVehicleId(), targetDealership);

        // Assert - Verify transfer failed
        assertFalse(transferResult, "Transferring rented vehicle should fail");
        assertEquals(1, dealership.getVehicles().size(), "Source should still have 1 vehicle");
        assertEquals(0, targetDealership.getVehicles().size(), "Target should have 0 vehicles");
    }

    @Test
    @DisplayName("Finding vehicles by ID")
    void testFindVehicleById() {
        // Arrange - Add vehicles to dealership
        dealership.addVehicle(suv);
        dealership.addVehicle(sedan);

        // Act - Find a vehicle by ID
        Vehicle foundVehicle = dealership.findVehicleById(sedan.getVehicleId());

        // Assert - Verify correct vehicle found
        assertNotNull(foundVehicle, "Vehicle should be found");
        assertEquals(sedan.getVehicleId(), foundVehicle.getVehicleId(), "Found vehicle should have correct ID");
        assertEquals(sedan.getManufacturer(), foundVehicle.getManufacturer(), "Found vehicle should have correct manufacturer");
        assertEquals(sedan.getModel(), foundVehicle.getModel(), "Found vehicle should have correct model");
    }

    @Test
    @DisplayName("Finding non-existent vehicles returns null")
    void testFindNonExistentVehicle() {
        // Arrange - Add one vehicle to dealership
        dealership.addVehicle(suv);

        // Act - Try to find non-existent vehicle
        Vehicle foundVehicle = dealership.findVehicleById("NONEXISTENT");

        // Assert - Verify null is returned
        assertNull(foundVehicle, "Finding non-existent vehicle should return null");
    }
}
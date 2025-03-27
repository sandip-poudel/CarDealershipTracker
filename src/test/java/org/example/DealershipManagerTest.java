package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the DealershipManager class using Arrange-Act-Assert pattern.
 * Tests focus on inventory management, rentals, and persistence operations.
 */
public class DealershipManagerTest {

    // Test objects
    private DealershipManager manager;
    private Vehicle suv;
    private Vehicle sedan;
    private Vehicle sportsCar;
    private Date startDate;
    private Date endDate;

    // Temporary directory for test files
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Create fresh manager for each test
        manager = new DealershipManager();

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

    // Helper method to create a test inventory file
    private File createTestInventoryFile() throws IOException {
        File inventoryFile = tempDir.resolve("test_inventory.json").toFile();

        // Create a minimal valid JSON structure
        String json = "{\n" +
                "  \"car_inventory\": [\n" +
                "    {\n" +
                "      \"price\": 28000.0,\n" +
                "      \"vehicle_model\": \"RAV4\",\n" +
                "      \"vehicle_type\": \"suv\",\n" +
                "      \"is_rented\": false,\n" +
                "      \"dealership_id\": \"D001\",\n" +
                "      \"vehicle_id\": \"SUV001\",\n" +
                "      \"vehicle_manufacturer\": \"Toyota\",\n" +
                "      \"acquisition_date\": " + System.currentTimeMillis() + "\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try (FileWriter writer = new FileWriter(inventoryFile)) {
            writer.write(json);
        }

        return inventoryFile;
    }

    @Test
    @DisplayName("Adding vehicles to inventory with acquisition enabled")
    void testAddVehicleWithAcquisitionEnabled() {
        // Arrange - setUp already created manager and vehicles
        File inventoryFile = tempDir.resolve("inventory.json").toFile();

        // Act - Add vehicle to inventory
        boolean result = manager.addVehicleToInventory(suv, inventoryFile);

        // Assert - Verify addition was successful
        assertTrue(result, "Adding vehicle should succeed when acquisition is enabled");
        assertEquals(1, manager.getVehiclesForDisplay().size(), "Manager should have 1 vehicle");
        assertTrue(inventoryFile.exists(), "Inventory file should be created");
    }

    @Test
    @DisplayName("Disabling acquisition prevents adding vehicles")
    void testAddVehicleWithAcquisitionDisabled() {
        // Arrange - Disable acquisition for a dealership
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.disableAcquisition(suv.getDealerId());

        // Act - Try to add vehicle to inventory
        boolean result = manager.addVehicleToInventory(suv, inventoryFile);

        // Assert - Verify addition was blocked
        assertFalse(result, "Adding vehicle should fail when acquisition is disabled");
        assertEquals(0, manager.getVehiclesForDisplay().size(), "Manager should have 0 vehicles");
    }

    @Test
    @DisplayName("Reading inventory from file")
    void testReadInventoryFile() throws IOException {
        // Arrange - Create test inventory file
        File inventoryFile = createTestInventoryFile();

        // Act - Read inventory
        manager.readInventoryFile(inventoryFile);

        // Assert - Verify inventory was loaded
        List<Vehicle> loadedVehicles = manager.getVehiclesForDisplay();
        assertEquals(1, loadedVehicles.size(), "Should load 1 vehicle from file");
        assertEquals("SUV001", loadedVehicles.get(0).getVehicleId(), "Loaded vehicle should have correct ID");
        assertEquals("Toyota", loadedVehicles.get(0).getManufacturer(), "Loaded vehicle should have correct manufacturer");
        assertEquals("RAV4", loadedVehicles.get(0).getModel(), "Loaded vehicle should have correct model");
    }

    @Test
    @DisplayName("Transferring vehicles between dealerships")
    void testTransferVehicle() {
        // Arrange - Add vehicle to source dealership
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", suv);
        manager.enableAcquisition("D002"); // Enable acquisition for target

        // Act - Transfer vehicle
        boolean transferResult = manager.transferVehicle("D001", "D002", suv.getVehicleId(), inventoryFile);

        // Assert - Verify transfer was successful
        assertTrue(transferResult, "Transfer should succeed");
        List<Vehicle> vehicles = manager.getVehiclesForDisplay();
        assertEquals(1, vehicles.size(), "Should still have 1 vehicle total");
        assertEquals("D002", vehicles.get(0).getDealerId(), "Vehicle should now belong to D002");
    }

    @Test
    @DisplayName("Cannot transfer vehicles to dealerships with acquisition disabled")
    void testTransferVehicleToDisabledDealer() {
        // Arrange - Add vehicle and disable target acquisition
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", suv);
        manager.disableAcquisition("D002");

        // Act - Attempt transfer
        boolean transferResult = manager.transferVehicle("D001", "D002", suv.getVehicleId(), inventoryFile);

        // Assert - Verify transfer failed
        assertFalse(transferResult, "Transfer should fail when target acquisition is disabled");
        List<Vehicle> vehicles = manager.getVehiclesForDisplay();
        assertEquals(1, vehicles.size(), "Should still have 1 vehicle total");
        assertEquals("D001", vehicles.get(0).getDealerId(), "Vehicle should still belong to D001");
    }

    @Test
    @DisplayName("Renting a vehicle")
    void testRentVehicle() {
        // Arrange - Add vehicle to inventory
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", sedan);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Act - Rent vehicle
        boolean rentResult = manager.rentVehicle("D001", sedan.getVehicleId(), startDateStr, endDateStr, inventoryFile);

        // Assert - Verify rental was successful
        assertTrue(rentResult, "Rental should succeed");
        Vehicle rentedVehicle = manager.getVehiclesForDisplay().get(0);
        assertTrue(rentedVehicle.isRented(), "Vehicle should be marked as rented");
    }

    @Test
    @DisplayName("Returning a rented vehicle")
    void testReturnVehicle() {
        // Arrange - Add and rent a vehicle
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", sedan);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        manager.rentVehicle("D001", sedan.getVehicleId(), startDateStr, endDateStr, inventoryFile);

        // Act - Return vehicle
        boolean returnResult = manager.returnVehicle("D001", sedan.getVehicleId(), inventoryFile);

        // Assert - Verify return was successful
        assertTrue(returnResult, "Return should succeed");
        Vehicle returnedVehicle = manager.getVehiclesForDisplay().get(0);
        assertFalse(returnedVehicle.isRented(), "Vehicle should no longer be rented");
    }

    @Test
    @DisplayName("Cannot rent a sports car")
    void testRentSportsCar() {
        // Arrange - Add sports car to inventory
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", sportsCar);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Act - Try to rent sports car
        boolean rentResult = manager.rentVehicle("D001", sportsCar.getVehicleId(), startDateStr, endDateStr, inventoryFile);

        // Assert - Verify rental failed
        assertFalse(rentResult, "Renting a sports car should fail");
        Vehicle sportsCar = manager.getVehiclesForDisplay().get(0);
        assertFalse(sportsCar.isRented(), "Sports car should not be marked as rented");
    }

    @Test
    @DisplayName("Exporting inventory to file")
    void testExportInventory() {
        // Arrange - Add vehicles to inventory
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        File exportFile = tempDir.resolve("export.json").toFile();

        manager.processAddVehicleCommand("D001", suv);
        manager.processAddVehicleCommand("D001", sedan);

        // Act - Export inventory
        boolean exportResult = manager.exportInventoryToExport(inventoryFile, exportFile);

        // Assert - Verify export was successful
        assertTrue(exportResult, "Export should succeed");
        assertTrue(exportFile.exists(), "Export file should be created");
        assertTrue(exportFile.length() > 0, "Export file should not be empty");
    }

    @Test
    @DisplayName("Removing vehicle from inventory")
    void testRemoveVehicle() {
        // Arrange - Add vehicle to inventory
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", suv);
        assertEquals(1, manager.getVehiclesForDisplay().size(), "Should have 1 vehicle initially");

        // Act - Remove vehicle
        boolean removeResult = manager.removeVehicleFromInventory(
                suv.getDealerId(),
                suv.getVehicleId(),
                suv.getManufacturer(),
                suv.getModel(),
                suv.getPrice(),
                inventoryFile);

        // Assert - Verify removal was successful
        assertTrue(removeResult, "Vehicle removal should succeed");
        assertEquals(0, manager.getVehiclesForDisplay().size(), "Should have 0 vehicles after removal");
    }

    @Test
    @DisplayName("Cannot remove rented vehicles")
    void testRemoveRentedVehicle() {
        // Arrange - Add and rent a vehicle
        File inventoryFile = tempDir.resolve("inventory.json").toFile();
        manager.processAddVehicleCommand("D001", sedan);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        manager.rentVehicle("D001", sedan.getVehicleId(), startDateStr, endDateStr, inventoryFile);

        // Act - Try to remove rented vehicle
        boolean removeResult = manager.removeVehicleFromInventory(
                sedan.getDealerId(),
                sedan.getVehicleId(),
                sedan.getManufacturer(),
                sedan.getModel(),
                sedan.getPrice(),
                inventoryFile);

        // Assert - Verify removal failed
        assertFalse(removeResult, "Removing rented vehicle should fail");
        assertEquals(1, manager.getVehiclesForDisplay().size(), "Should still have 1 vehicle");
        assertTrue(manager.getVehiclesForDisplay().get(0).isRented(), "Vehicle should still be rented");
    }
}

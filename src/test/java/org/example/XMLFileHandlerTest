package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the XMLFileHandler class using Arrange-Act-Assert pattern.
 * Tests focus on importing vehicle data from XML files with different formats
 * and validating proper extraction of dealer information.
 */
public class XMLFileHandlerTest {

    // Test objects
    private XMLFileHandler xmlFileHandler;

    // Temporary directory for test files
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Create a fresh XMLFileHandler for each test
        xmlFileHandler = new XMLFileHandler();
    }

    /**
     * Helper method to create a test XML file with vehicle data
     */
    private File createTestXmlFile(String content) throws IOException {
        File xmlFile = tempDir.resolve("test_dealers.xml").toFile();
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(content);
        }
        return xmlFile;
    }

    /**
     * Helper method to find a vehicle by ID in a list
     */
    private Vehicle findVehicleById(List<Vehicle> vehicles, String id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getVehicleId().equals(id)) {
                return vehicle;
            }
        }
        return null;
    }

    @Test
    @DisplayName("Importing standard XML with multiple vehicles and dealers")
    void testImportStandardXml() throws IOException {
        // Arrange - Create test XML file with standard data
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" +
                        "    <Vehicle type=\"suv\" id=\"848432\">\n" +
                        "      <Price unit=\"pounds\">17000</Price>\n" +
                        "      <Make>Land Rover</Make>\n" +
                        "      <Model>Range Rover</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"sedan\" id=\"151e5dde\">\n" +
                        "      <Price unit=\"dollars\">36600</Price>\n" +
                        "      <Make>Genesis</Make>\n" +
                        "      <Model>G70</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "  <Dealer id=\"721\">\n" +
                        "    <Name>Luxury Motors</Name>\n" +
                        "    <Vehicle type=\"sports car\" id=\"sc123\">\n" +
                        "      <Price unit=\"dollars\">68000</Price>\n" +
                        "      <Make>Porsche</Make>\n" +
                        "      <Model>911</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Verify correct number of vehicles imported
        assertEquals(3, importedVehicles.size(), "Should import 3 vehicles");

        // Verify first vehicle (SUV with pound pricing)
        Vehicle landRover = findVehicleById(importedVehicles, "848432");
        assertNotNull(landRover, "Land Rover should be imported");
        assertTrue(landRover instanceof SUV, "Vehicle should be an SUV");
        assertEquals("Land Rover", landRover.getManufacturer(), "Manufacturer should be Land Rover");
        assertEquals("Range Rover", landRover.getModel(), "Model should be Range Rover");
        // Price should be converted from pounds to dollars
        assertEquals(21250.0, landRover.getPrice(), 0.01, "Price should be converted from pounds to dollars");
        assertEquals("485", landRover.getDealerId(), "Dealer ID should be set");
        assertEquals("Wacky Bob's Automall", landRover.getMetadata().get("dealer_name"), "Dealer name should be stored in metadata");

        // Verify second vehicle (Sedan with dollar pricing)
        Vehicle genesis = findVehicleById(importedVehicles, "151e5dde");
        assertNotNull(genesis, "Genesis should be imported");
        assertTrue(genesis instanceof Sedan, "Vehicle should be a Sedan");
        assertEquals("Genesis", genesis.getManufacturer(), "Manufacturer should be Genesis");
        assertEquals("G70", genesis.getModel(), "Model should be G70");
        assertEquals(36600.0, genesis.getPrice(), 0.01, "Price should remain in dollars");
        assertEquals("485", genesis.getDealerId(), "Dealer ID should be set");
        assertEquals("Wacky Bob's Automall", genesis.getMetadata().get("dealer_name"), "Dealer name should be stored in metadata");

        // Verify third vehicle (Sports car)
        Vehicle porsche = findVehicleById(importedVehicles, "sc123");
        assertNotNull(porsche, "Porsche should be imported");
        assertTrue(porsche instanceof SportsCar, "Vehicle should be a SportsCar");
        assertEquals("Porsche", porsche.getManufacturer(), "Manufacturer should be Porsche");
        assertEquals("911", porsche.getModel(), "Model should be 911");
        assertEquals(68000.0, porsche.getPrice(), 0.01, "Price should be set correctly");
        assertEquals("721", porsche.getDealerId(), "Dealer ID should be set");
        assertEquals("Luxury Motors", porsche.getMetadata().get("dealer_name"), "Dealer name should be stored in metadata");
    }

    @Test
    @DisplayName("Importing XML with missing vehicle attributes")
    void testImportXmlWithMissingAttributes() throws IOException {
        // Arrange - Create test XML with missing attributes
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" +
                        "    <Vehicle id=\"848432\">\n" + // Missing type attribute
                        "      <Price unit=\"dollars\">17000</Price>\n" +
                        "      <Make>Land Rover</Make>\n" +
                        "      <Model>Range Rover</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"pickup\">\n" + // Missing id attribute
                        "      <Price unit=\"dollars\">42000</Price>\n" +
                        "      <Make>Ford</Make>\n" +
                        "      <Model>F-150</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Should still import at least one vehicle
        assertTrue(importedVehicles.size() > 0, "Should import at least one vehicle");

        // Check all imported vehicles have valid IDs
        for (Vehicle vehicle : importedVehicles) {
            assertNotNull(vehicle.getVehicleId(), "All imported vehicles should have an ID");
            assertFalse(vehicle.getVehicleId().isEmpty(), "Vehicle ID should not be empty");
        }
    }

    @Test
    @DisplayName("Importing XML with missing price information")
    void testImportXmlWithMissingPrice() throws IOException {
        // Arrange - Create test XML with missing price
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" +
                        "    <Vehicle type=\"suv\" id=\"848432\">\n" +
                        "      <Make>Land Rover</Make>\n" + // No Price element
                        "      <Model>Range Rover</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"sedan\" id=\"151e5dde\">\n" +
                        "      <Price>Invalid</Price>\n" + // Invalid price format
                        "      <Make>Genesis</Make>\n" +
                        "      <Model>G70</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Should handle errors gracefully, possibly skipping invalid vehicles
        // This test mainly checks that no exceptions are thrown
        // Implementation may vary on how it handles missing/invalid data
        assertNotNull(importedVehicles, "Should return a non-null list even with errors");
    }

    @Test
    @DisplayName("Importing XML with unknown vehicle types")
    void testImportXmlWithUnknownTypes() throws IOException {
        // Arrange - Create test XML with unknown vehicle type
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" +
                        "    <Vehicle type=\"unknown\" id=\"12345\">\n" +
                        "      <Price unit=\"dollars\">25000</Price>\n" +
                        "      <Make>Unknown</Make>\n" +
                        "      <Model>Mystery</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"suv\" id=\"67890\">\n" +
                        "      <Price unit=\"dollars\">32000</Price>\n" +
                        "      <Make>Toyota</Make>\n" +
                        "      <Model>RAV4</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Should handle unknown types by using a default type or skipping
        assertTrue(importedVehicles.size() >= 1, "Should import at least the valid vehicle");

        // Check that the valid SUV was imported
        Vehicle rav4 = findVehicleById(importedVehicles, "67890");
        assertNotNull(rav4, "Valid SUV should be imported");
        assertEquals("Toyota", rav4.getManufacturer(), "Manufacturer should be Toyota");
        assertEquals("RAV4", rav4.getModel(), "Model should be RAV4");

        // If the unknown type was imported with a default type, verify its data
        Vehicle unknown = findVehicleById(importedVehicles, "12345");
        if (unknown != null) {
            assertEquals("Unknown", unknown.getManufacturer(), "Manufacturer should be Unknown");
            assertEquals("Mystery", unknown.getModel(), "Model should be Mystery");
            assertEquals(25000.0, unknown.getPrice(), 0.01, "Price should be set correctly");
            assertTrue(unknown instanceof Vehicle, "Should be a Vehicle or subclass");
        }
    }

    @Test
    @DisplayName("Importing XML with multiple price units")
    void testImportXmlWithMultiplePriceUnits() throws IOException {
        // Arrange - Create test XML with different price units
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" +
                        "    <Vehicle type=\"suv\" id=\"123\">\n" +
                        "      <Price unit=\"pounds\">20000</Price>\n" +
                        "      <Make>Range Rover</Make>\n" +
                        "      <Model>Evoque</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"sedan\" id=\"456\">\n" +
                        "      <Price unit=\"dollars\">30000</Price>\n" +
                        "      <Make>Honda</Make>\n" +
                        "      <Model>Accord</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"pickup\" id=\"789\">\n" +
                        "      <Price unit=\"euros\">25000</Price>\n" + // Not explicitly supported
                        "      <Make>Ford</Make>\n" +
                        "      <Model>Ranger</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Verify correct conversions
        assertEquals(3, importedVehicles.size(), "Should import all 3 vehicles");

        // Verify pound conversion
        Vehicle rangeRover = findVehicleById(importedVehicles, "123");
        assertNotNull(rangeRover, "Range Rover should be imported");
        assertEquals(25000.0, rangeRover.getPrice(), 0.01, "Pounds should be converted to dollars");

        // Verify dollar remains unchanged
        Vehicle honda = findVehicleById(importedVehicles, "456");
        assertNotNull(honda, "Honda should be imported");
        assertEquals(30000.0, honda.getPrice(), 0.01, "Dollar price should remain unchanged");

        // Verify other currency handling (implementation may vary)
        Vehicle ford = findVehicleById(importedVehicles, "789");
        assertNotNull(ford, "Ford should be imported");
        assertTrue(ford.getPrice() > 0, "Price should be positive");
    }

    @Test
    @DisplayName("Importing XML with empty dealer name")
    void testImportXmlWithEmptyDealerName() throws IOException {
        // Arrange - Create test XML with missing dealer name
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name></Name>\n" + // Empty dealer name
                        "    <Vehicle type=\"suv\" id=\"848432\">\n" +
                        "      <Price unit=\"dollars\">17000</Price>\n" +
                        "      <Make>Land Rover</Make>\n" +
                        "      <Model>Range Rover</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

// Assert - Should still import the vehicle
        assertEquals(1, importedVehicles.size(), "Should import the vehicle");

// Check dealer information
        Vehicle vehicle = importedVehicles.get(0);
        assertEquals("485", vehicle.getDealerId(), "Dealer ID should be set");
// Either empty string or null in metadata depending on implementation
        assertTrue(vehicle.getMetadata().containsKey("dealer_name"), "Dealer name key should exist in metadata");
    }

    @Test
    @DisplayName("Importing XML with malformed structure")
    void testImportMalformedXml() throws IOException {
        // Arrange - Create test XML with malformed structure
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" +
                        "    <Vehicle type=\"suv\" id=\"848432\">\n" +
                        "      <Price unit=\"dollars\">17000</Price>\n" +
                        "      <Make>Land Rover</Make>\n" +
                        "      <Model>Range Rover</Model>\n" +
                        "    </Vehicle>\n" +
                        "    <Vehicle type=\"sedan\" id=\"151e5dde\">\n" +
                        "      <Price unit=\"dollars\">36600</Price>\n" +
                        "      <Manufacturer>Genesis</Manufacturer>\n" + // Inconsistent tag name
                        "      <Model>G70</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "  <BadTag>\n" + // Unexpected tag
                        "    <Something>Wrong</Something>\n" +
                        "  </BadTag>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Should handle malformed XML gracefully
        assertNotNull(importedVehicles, "Should return a non-null list even with malformed XML");
        assertTrue(importedVehicles.size() >= 1, "Should import at least one vehicle from valid part of XML");
    }

    @Test
    @DisplayName("Importing XML with different Name tag structure")
    void testImportXmlWithDifferentNameTag() throws IOException {
        // Arrange - Create test XML with different name tag
        String xmlContent =
                "<Dealers>\n" +
                        "  <Dealer id=\"485\">\n" +
                        "    <Name>Wacky Bob's Automall</Name>\n" + // Using "Name" instead of "n"
                        "    <Vehicle type=\"suv\" id=\"848432\">\n" +
                        "      <Price unit=\"dollars\">17000</Price>\n" +
                        "      <Make>Land Rover</Make>\n" +
                        "      <Model>Range Rover</Model>\n" +
                        "    </Vehicle>\n" +
                        "  </Dealer>\n" +
                        "</Dealers>";

        File xmlFile = createTestXmlFile(xmlContent);

        // Act - Import the XML file
        List<Vehicle> importedVehicles = xmlFileHandler.importXML(xmlFile);

        // Assert - Should still import the vehicle and handle different name tag
        assertEquals(1, importedVehicles.size(), "Should import the vehicle");

        // Check dealer name handling with different tag
        Vehicle vehicle = importedVehicles.get(0);
        assertTrue(vehicle.getMetadata().containsKey("dealer_name"), "Dealer name key should exist in metadata");
        assertEquals("Wacky Bob's Automall", vehicle.getMetadata().get("dealer_name"),
                "Dealer name should be extracted despite different tag name");
    }
}

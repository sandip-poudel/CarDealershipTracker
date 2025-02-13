package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionListener;

/**
 * DealershipGUI class provides a graphical user interface for managing vehicle dealership operations.
 * This class handles all user interactions, input validation, and visual display of dealership data.
 * The GUI allows users to add/remove vehicles, enable/disable dealer acquisition, and manage inventory files.
 */
public class DealershipGUI extends JFrame {
    // Constants for file paths and colors - Added for better maintainability
    private static final String INVENTORY_PATH = "src/main/resources/inventory.json";
    private static final String EXPORT_PATH = "src/main/resources/export.json";
    private static final Color HEADER_COLOR = new Color(0, 100, 200);

    // Core business logic manager
    private final DealershipManager manager;

    // Input fields stored in a list for easier management - Improvement for maintainability
    private final ArrayList<JTextField> inputFields = new ArrayList<>();

    // Main GUI components
    private JTextArea displayArea;      // Area for showing inventory and messages
    private Choice vehicleTypeChoice;   // Dropdown for vehicle types

    /**
     * Constructor initializes the GUI and sets up the dealership management system
     */
    public DealershipGUI() {
        this.manager = new DealershipManager();
        initializeGUI();
    }

    /**
     * Sets up the main GUI layout and initializes all components
     * Creates a three-part layout:
     * 1. Header panel (top)
     * 2. Input and button panels (center)
     * 3. Display area (bottom)
     */
    private void initializeGUI() {
        setupFrame();
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createInputPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.EAST);
        mainPanel.add(createDisplayArea(), BorderLayout.SOUTH);

        add(mainPanel);
        loadInitialInventory();
    }

    /**
     * Sets up the main frame properties
     * Added as separate method for better organization
     */
    private void setupFrame() {
        setTitle("Dealership Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
    }

    /**
     * Creates the header panel with gradient background and title
     * Maintains original visual styling while improving code organization
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(HEADER_COLOR);
                g.fillRect(0, 0, getWidth(), 60);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Vehicle Management System", 20, 40);
            }
        };
        headerPanel.setPreferredSize(new Dimension(800, 60));
        return headerPanel;
    }

    /**
     * Creates and sets up the display area with scroll capability
     * Extracted to separate method for better organization
     */
    private JScrollPane createDisplayArea() {
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        return new JScrollPane(displayArea);
    }

    /**
     * Creates and organizes the input panel with labels and text fields
     * Uses GridLayout for uniform spacing and alignment
     * Improved to use arrays and loops for cleaner code
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        // Create and store input fields
        JTextField dealerIdField = new JTextField();
        JTextField vehicleIdField = new JTextField();
        JTextField manufacturerField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField priceField = new JTextField();

        inputFields.addAll(Arrays.asList(
                dealerIdField, vehicleIdField, manufacturerField, modelField, priceField
        ));

        // Create vehicle type dropdown
        vehicleTypeChoice = new Choice();
        Arrays.asList("SUV", "Sedan", "Pickup", "Sports Car")
                .forEach(vehicleTypeChoice::add);

        // Add components to panel with labels
        String[][] labelPairs = {
                {"Dealer ID:", null},
                {"Vehicle Type:", null},
                {"Vehicle ID:", null},
                {"Manufacturer:", null},
                {"Model:", null},
                {"Price:", null}
        };

        Component[] components = {
                dealerIdField, vehicleTypeChoice, vehicleIdField,
                manufacturerField, modelField, priceField
        };

        for (int i = 0; i < labelPairs.length; i++) {
            panel.add(new JLabel(labelPairs[i][0]));
            panel.add(components[i]);
        }

        return panel;
    }

    /**
     * Helper method to create buttons with consistent styling
     * Added for code reusability and maintainability
     */
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

    /**
     * Creates the button panel with all action buttons
     * Adds action listeners to handle button clicks
     * Improved to use helper method for button creation
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));

        Arrays.asList(
                createButton("Add Vehicle to Inventory", e -> handleAddVehicle()),
                createButton("Remove Vehicle from Inventory", e -> handleRemoveVehicle()),
                createButton("Enable Dealer Acquisition", e -> handleEnableAcquisition()),
                createButton("Disable Dealer Acquisition", e -> handleDisableAcquisition()),
                createButton("Export to export.json", e -> handleExportInventory()),
                createButton("Clear export.json", e -> handleClearExport())
        ).forEach(panel::add);

        return panel;
    }

    /**
     * Loads initial inventory data from file if it exists
     * Reads from inventory.json and updates the display
     */
    private void loadInitialInventory() {
        File initialFile = new File(INVENTORY_PATH);
        if (initialFile.exists()) {
            manager.readInventoryFile(initialFile);
            refreshDisplay();
        }
    }

    /**
     * Updates the display area with current inventory information
     * Formats the output for easy reading
     * Improved to use StringBuilder and forEach for cleaner code
     */
    private void refreshDisplay() {
        StringBuilder sb = new StringBuilder("Current Inventory:\n\n");

        manager.getVehiclesForDisplay().forEach(vehicle ->
                sb.append(String.format("Type: %s, ID: %s, Manufacturer: %s, Model: %s, Price: $%.2f, Dealer: %s\n",
                        vehicle.getClass().getSimpleName(),
                        vehicle.getVehicleId(),
                        vehicle.getManufacturer(),
                        vehicle.getModel(),
                        vehicle.getPrice(),
                        vehicle.getDealerId()))
        );

        displayArea.setText(sb.toString());
    }

    /**
     * Validates all input fields before processing
     * Checks for empty fields and valid price format
     * Improved to use stream operations for cleaner validation
     * @return boolean indicating if all fields are valid
     */
    private boolean validateFields() {
        if (inputFields.stream().anyMatch(field -> field.getText().trim().isEmpty())) {
            showMessage("Error: All fields must be filled out!");
            return false;
        }

        try {
            double price = Double.parseDouble(inputFields.get(4).getText().trim());
            if (price <= 0) {
                showMessage("Error: Price must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("Error: Invalid price format!");
            return false;
        }

        return true;
    }

    /**
     * Helper method to handle common operations with error handling
     * Added for better error handling and code reuse
     */
    private void handleDealershipOperation(String operationName, Runnable operation) {
        if (!validateFields()) return;
        try {
            operation.run();
        } catch (Exception ex) {
            showMessage("Error " + operationName + ": " + ex.getMessage());
        }
    }

    /**
     * Handles the addition of a new vehicle to the inventory
     * Validates input, creates vehicle object, and updates inventory
     * Improved with common error handling
     */
    private void handleAddVehicle() {
        handleDealershipOperation("adding vehicle", () -> {
            String dealerId = inputFields.get(0).getText().trim();
            Vehicle vehicle = createVehicleFromType();
            if (vehicle == null) return;

            vehicle.setVehicleId(inputFields.get(1).getText().trim());
            vehicle.setManufacturer(inputFields.get(2).getText().trim());
            vehicle.setModel(inputFields.get(3).getText().trim());
            vehicle.setPrice(Double.parseDouble(inputFields.get(4).getText().trim()));
            vehicle.setAcquisitionDate(new Date());
            vehicle.setDealerId(dealerId);

            File inventoryFile = new File(INVENTORY_PATH);
            if (manager.addVehicleToInventory(vehicle, inventoryFile)) {
                refreshDisplay();
                clearInputFields();
                showMessage("Vehicle added to inventory.json successfully!");
            } else {
                showMessage("Error: Cannot add vehicle - Acquisition is disabled for dealer " + dealerId);
            }
        });
    }

    /**
     * Handles the removal of a vehicle from the inventory
     * Validates input and updates inventory file
     * Improved with common error handling
     */
    private void handleRemoveVehicle() {
        handleDealershipOperation("removing vehicle", () -> {
            String dealerId = inputFields.get(0).getText().trim();
            String vehicleId = inputFields.get(1).getText().trim();
            String manufacturer = inputFields.get(2).getText().trim();
            String model = inputFields.get(3).getText().trim();
            double price = Double.parseDouble(inputFields.get(4).getText().trim());

            File inventoryFile = new File(INVENTORY_PATH);
            if (manager.removeVehicleFromInventory(dealerId, vehicleId, manufacturer, model, price, inventoryFile)) {
                refreshDisplay();
                clearInputFields();
                showMessage("Vehicle removed from inventory successfully!");
            } else {
                showMessage("Error: Vehicle not found or could not be removed!");
            }
        });
    }

    /**
     * Creates appropriate vehicle object based on selected type
     * Improved to use switch expression for cleaner code
     * @return Vehicle object of the selected type
     */
    private Vehicle createVehicleFromType() {
        return switch (vehicleTypeChoice.getSelectedItem()) {
            case "SUV" -> new SUV();
            case "Sedan" -> new Sedan();
            case "Pickup" -> new Pickup();
            case "Sports Car" -> new SportsCar();
            default -> null;
        };
    }

    /**
     * Handles enabling acquisition for a dealer
     * Requires valid dealer ID
     */
    private void handleEnableAcquisition() {
        String dealerId = inputFields.get(0).getText().trim();
        if (dealerId.isEmpty()) {
            showMessage("Error: Dealer ID is required to enable acquisition!");
            return;
        }
        if (manager.enableAcquisition(dealerId)) {
            showMessage("Acquisition enabled for dealer: " + dealerId);
        }
    }

    /**
     * Handles disabling acquisition for a dealer
     * Requires valid dealer ID
     */
    private void handleDisableAcquisition() {
        String dealerId = inputFields.get(0).getText().trim();
        if (dealerId.isEmpty()) {
            showMessage("Error: Dealer ID is required to disable acquisition!");
            return;
        }
        if (manager.disableAcquisition(dealerId)) {
            showMessage("Acquisition disabled for dealer: " + dealerId);
        }
    }

    /**
     * Handles exporting current inventory to export.json
     * Validates file existence and handles errors
     */
    private void handleExportInventory() {
        File inventoryFile = new File(INVENTORY_PATH);
        File exportFile = new File(EXPORT_PATH);

        if (!inventoryFile.exists()) {
            showMessage("Error: inventory.json not found!");
            return;
        }

        try {
            if (manager.exportInventoryToExport(inventoryFile, exportFile)) {
                showMessage("Successfully exported to export.json");
            } else {
                showMessage("Failed to export: No vehicles found in inventory");
            }
        } catch (Exception e) {
            showMessage("Error during export: " + e.getMessage());
        }
    }

    /**
     * Handles clearing the export.json file
     * Creates empty inventory in export file
     */
    private void handleClearExport() {
        try {
            manager.clearExportFile(new File(EXPORT_PATH));
            showMessage("export.json has been cleared");
        } catch (Exception e) {
            showMessage("Error clearing export.json: " + e.getMessage());
        }
    }

    /**
     * Clears all input fields after successful operations
     * Improved to use forEach for cleaner code
     */
    private void clearInputFields() {
        inputFields.forEach(field -> field.setText(""));
        vehicleTypeChoice.select(0);
    }

    /**
     * Displays messages in the display area
     * @param message The message to display
     */
    private void showMessage(String message) {
        displayArea.append("\n>>> " + message + "\n");
    }
}

/*
 * Original Authors: Kenan & Nala
 * This code represents a collaborative effort between human developers with partial AI assistance
 * The core functionality and implementation was created by Kenan & Nala
 */

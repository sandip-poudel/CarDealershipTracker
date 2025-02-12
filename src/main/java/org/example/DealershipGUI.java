package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;

/**
 * DealershipGUI class provides a graphical user interface for managing vehicle dealership operations.
 * This class handles all user interactions, input validation, and visual display of dealership data.
 * The GUI allows users to add/remove vehicles, enable/disable dealer acquisition, and manage inventory files.
 */
public class DealershipGUI extends JFrame {
    // Core business logic manager
    private final DealershipManager manager;

    // Main GUI components
    private JPanel mainPanel;           // Main container panel
    private JTextArea displayArea;      // Area for showing inventory and messages
    private JButton addButton, removeButton, enableButton, disableButton, exportButton, clearExportButton;

    // Input field labels
    private JLabel dealerIdLabel, vehicleTypeLabel, vehicleIdLabel, manufacturerLabel, modelLabel, priceLabel;

    // Input fields for vehicle data
    private JTextField dealerIdField, vehicleIdField, manufacturerField, modelField, priceField;
    private Choice vehicleTypeChoice;   // Dropdown for vehicle types

    // Custom header panel for visual enhancement
    private JPanel headerPanel;

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
        setTitle("Dealership Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        mainPanel = new JPanel(new BorderLayout(10, 10));

        // Create custom header with gradient background
        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Create blue header bar
                g.setColor(new Color(0, 100, 200));
                g.fillRect(0, 0, getWidth(), 60);
                // Add white text
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Vehicle Management System", 20, 40);
            }
        };
        headerPanel.setPreferredSize(new Dimension(800, 60));

        // Create and organize input and button panels
        JPanel inputPanel = createInputPanel();
        JPanel buttonPanel = createButtonPanel();

        // Set up display area with scroll capability
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        // Assemble main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);
        loadInitialInventory();
    }

    /**
     * Creates and organizes the input panel with labels and text fields
     * Uses GridLayout for uniform spacing and alignment
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        // Initialize labels
        dealerIdLabel = new JLabel("Dealer ID:");
        vehicleTypeLabel = new JLabel("Vehicle Type:");
        vehicleIdLabel = new JLabel("Vehicle ID:");
        manufacturerLabel = new JLabel("Manufacturer:");
        modelLabel = new JLabel("Model:");
        priceLabel = new JLabel("Price:");

        // Initialize input fields
        dealerIdField = new JTextField();
        vehicleTypeChoice = new Choice();
        // Add vehicle types to dropdown
        vehicleTypeChoice.add("SUV");
        vehicleTypeChoice.add("Sedan");
        vehicleTypeChoice.add("Pickup");
        vehicleTypeChoice.add("Sports Car");
        vehicleIdField = new JTextField();
        manufacturerField = new JTextField();
        modelField = new JTextField();
        priceField = new JTextField();

        // Add components to panel in order
        panel.add(dealerIdLabel);
        panel.add(dealerIdField);
        panel.add(vehicleTypeLabel);
        panel.add(vehicleTypeChoice);
        panel.add(vehicleIdLabel);
        panel.add(vehicleIdField);
        panel.add(manufacturerLabel);
        panel.add(manufacturerField);
        panel.add(modelLabel);
        panel.add(modelField);
        panel.add(priceLabel);
        panel.add(priceField);

        return panel;
    }

    /**
     * Creates the button panel with all action buttons
     * Adds action listeners to handle button clicks
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));

        // Initialize buttons
        addButton = new JButton("Add Vehicle to Inventory");
        removeButton = new JButton("Remove Vehicle from Inventory");
        enableButton = new JButton("Enable Dealer Acquisition");
        disableButton = new JButton("Disable Dealer Acquisition");
        exportButton = new JButton("Export to export.json");
        clearExportButton = new JButton("Clear export.json");

        // Add action listeners
        addButton.addActionListener(e -> handleAddVehicle());
        removeButton.addActionListener(e -> handleRemoveVehicle());
        enableButton.addActionListener(e -> handleEnableAcquisition());
        disableButton.addActionListener(e -> handleDisableAcquisition());
        exportButton.addActionListener(e -> handleExportInventory());
        clearExportButton.addActionListener(e -> handleClearExport());

        // Add buttons to panel
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(enableButton);
        panel.add(disableButton);
        panel.add(exportButton);
        panel.add(clearExportButton);

        return panel;
    }

    /**
     * Loads initial inventory data from file if it exists
     * Reads from inventory.json and updates the display
     */
    private void loadInitialInventory() {
        File initialFile = new File("src/main/resources/inventory.json");
        if (initialFile.exists()) {
            manager.readInventoryFile(initialFile);
            refreshDisplay();
        }
    }

    /**
     * Updates the display area with current inventory information
     * Formats the output for easy reading
     */
    private void refreshDisplay() {
        displayArea.setText("");
        StringBuilder sb = new StringBuilder();
        sb.append("Current Inventory:\n\n");

        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            sb.append(String.format("Type: %s, ID: %s, Manufacturer: %s, Model: %s, Price: $%.2f, Dealer: %s\n",
                    vehicle.getClass().getSimpleName(),
                    vehicle.getVehicleId(),
                    vehicle.getManufacturer(),
                    vehicle.getModel(),
                    vehicle.getPrice(),
                    vehicle.getDealerId()));
        }

        displayArea.setText(sb.toString());
    }

    /**
     * Validates all input fields before processing
     * Checks for empty fields and valid price format
     * @return boolean indicating if all fields are valid
     */
    private boolean validateFields() {
        if (dealerIdField.getText().trim().isEmpty() ||
                vehicleIdField.getText().trim().isEmpty() ||
                manufacturerField.getText().trim().isEmpty() ||
                modelField.getText().trim().isEmpty() ||
                priceField.getText().trim().isEmpty()) {
            showMessage("Error: All fields must be filled out!");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
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
     * Handles the addition of a new vehicle to the inventory
     * Validates input, creates vehicle object, and updates inventory
     */
    private void handleAddVehicle() {
        if (!validateFields()) {
            return;
        }

        try {
            String dealerId = dealerIdField.getText().trim();
            Vehicle vehicle = createVehicleFromType();
            if (vehicle == null) return;

            // Set vehicle properties from input fields
            vehicle.setVehicleId(vehicleIdField.getText().trim());
            vehicle.setManufacturer(manufacturerField.getText().trim());
            vehicle.setModel(modelField.getText().trim());
            vehicle.setPrice(Double.parseDouble(priceField.getText().trim()));
            vehicle.setAcquisitionDate(new Date());
            vehicle.setDealerId(dealerId);

            File inventoryFile = new File("src/main/resources/inventory.json");
            if (manager.addVehicleToInventory(vehicle, inventoryFile)) {
                refreshDisplay();
                clearInputFields();
                showMessage("Vehicle added to inventory.json successfully!");
            } else {
                showMessage("Error: Cannot add vehicle - Acquisition is disabled for dealer " + dealerId);
            }

        } catch (Exception ex) {
            showMessage("Error adding vehicle: " + ex.getMessage());
        }
    }

    /**
     * Handles the removal of a vehicle from the inventory
     * Validates input and updates inventory file
     */
    private void handleRemoveVehicle() {
        if (!validateFields()) {
            return;
        }

        try {
            String dealerId = dealerIdField.getText().trim();
            String vehicleId = vehicleIdField.getText().trim();
            String manufacturer = manufacturerField.getText().trim();
            String model = modelField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            File inventoryFile = new File("src/main/resources/inventory.json");
            if (manager.removeVehicleFromInventory(dealerId, vehicleId, manufacturer, model, price, inventoryFile)) {
                refreshDisplay();
                clearInputFields();
                showMessage("Vehicle removed from inventory successfully!");
            } else {
                showMessage("Error: Vehicle not found or could not be removed!");
            }
        } catch (Exception ex) {
            showMessage("Error removing vehicle: " + ex.getMessage());
        }
    }

    /**
     * Creates appropriate vehicle object based on selected type
     * @return Vehicle object of the selected type
     */
    private Vehicle createVehicleFromType() {
        String type = vehicleTypeChoice.getSelectedItem();
        switch (type) {
            case "SUV": return new SUV();
            case "Sedan": return new Sedan();
            case "Pickup": return new Pickup();
            case "Sports Car": return new SportsCar();
            default: return null;
        }
    }

    /**
     * Handles enabling acquisition for a dealer
     * Requires valid dealer ID
     */
    private void handleEnableAcquisition() {
        String dealerId = dealerIdField.getText().trim();
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
        String dealerId = dealerIdField.getText().trim();
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
        File inventoryFile = new File("src/main/resources/inventory.json");
        File exportFile = new File("src/main/resources/export.json");

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
            File exportFile = new File("src/main/resources/export.json");
            manager.clearExportFile(exportFile);
            showMessage("export.json has been cleared");
        } catch (Exception e) {
            showMessage("Error clearing export.json: " + e.getMessage());
        }
    }

    /**
     * Clears all input fields after successful operations
     */
    private void clearInputFields() {
        dealerIdField.setText("");
        vehicleIdField.setText("");
        manufacturerField.setText("");
        modelField.setText("");
        priceField.setText("");
        vehicleTypeChoice.select(0);
    }

    /**
     * Displays messages in the display area
     * @param message The message to display
     */
    private void showMessage(String message) {
        displayArea.append("\n>>> " + message + "\n");
    }

    /**
     * Main method to launch the application
     * Uses EventQueue to ensure thread safety
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            DealershipGUI gui = new DealershipGUI();
            gui.setVisible(true);
        });
    }
}

/*
 * Original Authors: Kenan & Nala
 * This code represents a collaborative effort between human developers with partial AI assistance
 * The core functionality and implementation was created by Kenan & Nala
 * Comments and documentation structure were enhanced with AI assistance
 */

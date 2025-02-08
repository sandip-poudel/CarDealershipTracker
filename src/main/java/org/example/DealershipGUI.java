package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;

public class DealershipGUI extends JFrame {
    private final DealershipManager manager;

    // Basic Swing Components
    private JPanel mainPanel;
    private JTextArea displayArea;
    private JButton addButton, removeButton, enableButton, disableButton, exportButton, clearExportButton;
    private JLabel dealerIdLabel, vehicleTypeLabel, vehicleIdLabel, manufacturerLabel, modelLabel, priceLabel;
    private JTextField dealerIdField, vehicleIdField, manufacturerField, modelField, priceField;
    private Choice vehicleTypeChoice;

    // Custom drawing panel
    private JPanel headerPanel;

    public DealershipGUI() {
        this.manager = new DealershipManager();
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Dealership Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        mainPanel = new JPanel(new BorderLayout(10, 10));

        headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 100, 200));
                g.fillRect(0, 0, getWidth(), 60);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Vehicle Management System", 20, 40);
            }
        };
        headerPanel.setPreferredSize(new Dimension(800, 60));

        JPanel inputPanel = createInputPanel();
        JPanel buttonPanel = createButtonPanel();

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);
        loadInitialInventory();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        dealerIdLabel = new JLabel("Dealer ID:");
        vehicleTypeLabel = new JLabel("Vehicle Type:");
        vehicleIdLabel = new JLabel("Vehicle ID:");
        manufacturerLabel = new JLabel("Manufacturer:");
        modelLabel = new JLabel("Model:");
        priceLabel = new JLabel("Price:");

        dealerIdField = new JTextField();
        vehicleTypeChoice = new Choice();
        vehicleTypeChoice.add("SUV");
        vehicleTypeChoice.add("Sedan");
        vehicleTypeChoice.add("Pickup");
        vehicleTypeChoice.add("Sports Car");
        vehicleIdField = new JTextField();
        manufacturerField = new JTextField();
        modelField = new JTextField();
        priceField = new JTextField();

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

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));

        addButton = new JButton("Add Vehicle to Inventory");
        removeButton = new JButton("Remove Vehicle from Inventory");
        enableButton = new JButton("Enable Dealer Acquisition");
        disableButton = new JButton("Disable Dealer Acquisition");
        exportButton = new JButton("Export to export.json");
        clearExportButton = new JButton("Clear export.json");

        addButton.addActionListener(e -> handleAddVehicle());
        removeButton.addActionListener(e -> handleRemoveVehicle());
        enableButton.addActionListener(e -> handleEnableAcquisition());
        disableButton.addActionListener(e -> handleDisableAcquisition());
        exportButton.addActionListener(e -> handleExportInventory());
        clearExportButton.addActionListener(e -> handleClearExport());

        panel.add(addButton);
        panel.add(removeButton);
        panel.add(enableButton);
        panel.add(disableButton);
        panel.add(exportButton);
        panel.add(clearExportButton);

        return panel;
    }

    private void loadInitialInventory() {
        File initialFile = new File("src/main/resources/inventory.json");
        if (initialFile.exists()) {
            manager.readInventoryFile(initialFile);
            refreshDisplay();
        }
    }

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

    private void handleAddVehicle() {
        if (!validateFields()) {
            return;
        }

        try {
            String dealerId = dealerIdField.getText().trim();
            Vehicle vehicle = createVehicleFromType();
            if (vehicle == null) return;

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

    private void handleClearExport() {
        try {
            File exportFile = new File("src/main/resources/export.json");
            manager.clearExportFile(exportFile);
            showMessage("export.json has been cleared");
        } catch (Exception e) {
            showMessage("Error clearing export.json: " + e.getMessage());
        }
    }

    private void clearInputFields() {
        dealerIdField.setText("");
        vehicleIdField.setText("");
        manufacturerField.setText("");
        modelField.setText("");
        priceField.setText("");
        vehicleTypeChoice.select(0);
    }

    private void showMessage(String message) {
        displayArea.append("\n>>> " + message + "\n");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            DealershipGUI gui = new DealershipGUI();
            gui.setVisible(true);
        });
    }
}
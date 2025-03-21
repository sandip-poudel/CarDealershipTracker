package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * DealershipJavaFXGUI class provides a JavaFX-based graphical user interface for managing
 * vehicle dealership operations. This class handles all user interactions, input validation,
 * and visual display of dealership data.
 */
public class DealershipJavaFXGUI extends Application {
    // Constants for file paths and colors
    private static final String INVENTORY_PATH = "src/main/resources/inventory.json";
    private static final String EXPORT_PATH = "src/main/resources/export.json";
    private static final String APP_TITLE = "Dealership Management System";
    private static final Color THEME_COLOR = Color.DODGERBLUE; // Main theme color

    // Core business logic manager
    private DealershipManager manager;

    // Input fields
    private TextField dealerIdField;
    private ComboBox<String> dealerIdComboBox;
    private ComboBox<String> vehicleTypeComboBox;
    private TextField vehicleIdField;
    private TextField manufacturerField;
    private TextField modelField;
    private TextField priceField;

    // Main GUI components
    private TextArea displayArea;
    private FileChooser fileChooser;

    @Override
    public void start(Stage primaryStage) {
        manager = new DealershipManager();

        // Set up the main layout
        BorderPane mainLayout = new BorderPane();

        // Create header
        VBox headerPane = createHeaderPanel();
        mainLayout.setTop(headerPane);

        // Create center section with input fields and buttons
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(createInputPanel(), createButtonPanel());
        centerPane.setDividerPositions(0.6);

        // Create display area with resizable capability
        displayArea = createDisplayArea();
        ScrollPane scrollPane = new ScrollPane(displayArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Make it resizable
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        mainSplitPane.getItems().addAll(centerPane, scrollPane);
        mainSplitPane.setDividerPositions(0.6);
        mainLayout.setCenter(mainSplitPane);

        // Set up the scene
        Scene scene = new Scene(mainLayout, 1000, 700);

        // Configure the stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load initial data
        loadInitialInventory();
    }

    /**
     * Creates the header panel with title
     */
    private VBox createHeaderPanel() {
        VBox header = new VBox();
        header.setPadding(new Insets(15, 15, 15, 15));
        String colorHex = String.format("#%02X%02X%02X",
                (int)(THEME_COLOR.getRed() * 255),
                (int)(THEME_COLOR.getGreen() * 255),
                (int)(THEME_COLOR.getBlue() * 255));
        header.setStyle("-fx-background-color: " + colorHex + ";");

        Label titleLabel = new Label(APP_TITLE);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        header.getChildren().add(titleLabel);
        return header;
    }

    /**
     * Creates the input panel with form fields
     */
    private GridPane createInputPanel() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20, 20, 20, 20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        // Initialize input fields
        dealerIdField = new TextField();

        // Initialize dealership dropdown
        dealerIdComboBox = new ComboBox<>();
        dealerIdComboBox.setPromptText("Select existing dealer");
        dealerIdComboBox.setEditable(false);

        // Create a HBox to hold both dealer ID options
        HBox dealerSelectionBox = new HBox(10);
        dealerSelectionBox.getChildren().addAll(dealerIdComboBox, dealerIdField);

        // Add null/clear option to the dropdown
        dealerIdComboBox.getItems().add("-- New Dealer --");

        // Set up dealership dropdown listener
        dealerIdComboBox.setOnAction(e -> {
            if (dealerIdComboBox.getValue() != null) {
                if (dealerIdComboBox.getValue().equals("-- New Dealer --")) {
                    // Selected the "New Dealer" option
                    dealerIdField.clear();
                    dealerIdField.setDisable(false);
                } else {
                    // Selected an existing dealer
                    dealerIdField.clear();
                    dealerIdField.setDisable(true);
                }
            } else {
                dealerIdField.setDisable(false);
            }
        });

        // Set up dealerIdField listener to clear dropdown when text is entered
        dealerIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                dealerIdComboBox.setValue(null);
            }
        });

        vehicleTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "SUV", "Sedan", "Pickup", "Sports Car"));
        vehicleTypeComboBox.setValue("SUV");
        vehicleIdField = new TextField();
        manufacturerField = new TextField();
        modelField = new TextField();
        priceField = new TextField();

        // Create a large rent vehicle button
        Button rentVehicleBtn = new Button("Rent Vehicle");
        rentVehicleBtn.setPrefWidth(250);
        rentVehicleBtn.setPrefHeight(45);
        rentVehicleBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: skyblue;");
        rentVehicleBtn.setOnAction(e -> handleRentVehicle());

        // Style fields to be consistent size
        dealerIdField.setPrefWidth(120);
        dealerIdComboBox.setPrefWidth(120);
        vehicleTypeComboBox.setPrefWidth(250);
        vehicleIdField.setPrefWidth(250);
        manufacturerField.setPrefWidth(250);
        modelField.setPrefWidth(250);
        priceField.setPrefWidth(250);

        // Add field labels
        gridPane.add(new Label("Dealer ID:"), 0, 0);
        gridPane.add(dealerSelectionBox, 1, 0);

        gridPane.add(new Label("Vehicle Type:"), 0, 1);
        gridPane.add(vehicleTypeComboBox, 1, 1);

        gridPane.add(new Label("Vehicle ID:"), 0, 2);
        gridPane.add(vehicleIdField, 1, 2);

        gridPane.add(new Label("Manufacturer:"), 0, 3);
        gridPane.add(manufacturerField, 1, 3);

        gridPane.add(new Label("Model:"), 0, 4);
        gridPane.add(modelField, 1, 4);

        gridPane.add(new Label("Price:"), 0, 5);
        gridPane.add(priceField, 1, 5);

        // Add rental button in row 6
        gridPane.add(new Label(""), 0, 6);
        gridPane.add(rentVehicleBtn, 1, 6);

        return gridPane;
    }

    /**
     * Creates the button panel with all action buttons
     */
    private VBox createButtonPanel() {
        VBox buttonPanel = new VBox(10);
        buttonPanel.setPadding(new Insets(20, 20, 20, 20));

        fileChooser = new FileChooser();

        // Create buttons with consistent styling
        Button addVehicleBtn = createStyledButton("Add Vehicle");
        addVehicleBtn.setOnAction(e -> handleAddVehicle());

        Button removeVehicleBtn = createStyledButton("Remove Vehicle");
        removeVehicleBtn.setOnAction(e -> handleRemoveVehicle());

        Button returnVehicleBtn = createStyledButton("Return Vehicle");
        returnVehicleBtn.setOnAction(e -> handleReturnVehicle());

        Button transferVehicleBtn = createStyledButton("Transfer Vehicle");
        transferVehicleBtn.setOnAction(e -> handleTransferVehicle());

        Button importXmlBtn = createStyledButton("Import XML");
        importXmlBtn.setOnAction(e -> handleImportXML());

        Button enableAcquisitionBtn = createStyledButton("Enable Acquisition");
        enableAcquisitionBtn.setOnAction(e -> handleEnableAcquisition());

        Button disableAcquisitionBtn = createStyledButton("Disable Acquisition");
        disableAcquisitionBtn.setOnAction(e -> handleDisableAcquisition());

        Button exportInventoryBtn = createStyledButton("Export Inventory");
        exportInventoryBtn.setOnAction(e -> handleExportInventory());

        Button clearExportBtn = createStyledButton("Clear Export");
        clearExportBtn.setOnAction(e -> handleClearExport());

        buttonPanel.getChildren().addAll(
                addVehicleBtn, removeVehicleBtn, returnVehicleBtn,
                transferVehicleBtn, importXmlBtn, enableAcquisitionBtn,
                disableAcquisitionBtn, exportInventoryBtn, clearExportBtn
        );

        return buttonPanel;
    }

    /**
     * Creates a styled button with consistent look and feel
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(35);
        button.setStyle("-fx-font-size: 14px;");
        return button;
    }

    /**
     * Creates and sets up the display area
     */
    private TextArea createDisplayArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefHeight(300);
        area.setFont(Font.font("Monospaced", 12));
        return area;
    }

    /**
     * Loads initial inventory data from file if it exists
     */
    private void loadInitialInventory() {
        File initialFile = new File(INVENTORY_PATH);
        if (initialFile.exists()) {
            manager.readInventoryFile(initialFile);
            refreshDisplay();
            updateDealerDropdown();
        }
    }

    /**
     * Updates the dealer dropdown with current dealerships in the system
     */
    private void updateDealerDropdown() {
        // Get all unique dealer IDs from current inventory
        java.util.Set<String> dealerIds = new java.util.HashSet<>();

        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            dealerIds.add(vehicle.getDealerId());
        }

        // Update the combo box
        dealerIdComboBox.getItems().clear();
        dealerIdComboBox.getItems().add("-- New Dealer --");
        dealerIdComboBox.getItems().addAll(dealerIds);
    }

    /**
     * Updates the display area with current inventory information
     */
    private void refreshDisplay() {
        StringBuilder sb = new StringBuilder("Current Inventory:\n\n");

        manager.getVehiclesForDisplay().forEach(vehicle -> {
            String rentalStatus = vehicle.isRented() ? "RENTED" : "AVAILABLE";
            if (vehicle instanceof SportsCar) {
                rentalStatus = "NOT RENTABLE";
            }

            String dealerInfo = vehicle.getDealerId();
            if (vehicle.getMetadata().containsKey("dealer_name")) {
                dealerInfo += " (" + vehicle.getMetadata().get("dealer_name") + ")";
            }

            sb.append(String.format("Type: %s, ID: %s, Manufacturer: %s, Model: %s, Price: $%.2f, Dealer: %s, Status: %s\n",
                    vehicle.getClass().getSimpleName(),
                    vehicle.getVehicleId(),
                    vehicle.getManufacturer(),
                    vehicle.getModel(),
                    vehicle.getPrice(),
                    dealerInfo,
                    rentalStatus));
        });

        displayArea.setText(sb.toString());
    }

    /**
     * Validates all input fields before processing
     */
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        // Check for dealer ID (either from dropdown or text field)
        String dealerId = getDealerId();
        if (dealerId.isEmpty()) {
            errorMessage.append("Dealer ID is required. Either select from dropdown or enter a new one.\n");
        }

        if (vehicleIdField.getText().trim().isEmpty()) {
            errorMessage.append("Vehicle ID is required.\n");
        }
        if (manufacturerField.getText().trim().isEmpty()) {
            errorMessage.append("Manufacturer is required.\n");
        }
        if (modelField.getText().trim().isEmpty()) {
            errorMessage.append("Model is required.\n");
        }
        if (priceField.getText().trim().isEmpty()) {
            errorMessage.append("Price is required.\n");
        }

        // Validate price format
        if (!priceField.getText().trim().isEmpty()) {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    errorMessage.append("Price must be greater than 0!\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Invalid price format! Enter a valid number.\n");
            }
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Validates just dealer ID and vehicle ID fields
     */
    private boolean validateDealerAndVehicleIds() {
        StringBuilder errorMessage = new StringBuilder();

        String dealerId = getDealerId();
        if (dealerId.isEmpty()) {
            errorMessage.append("Dealer ID is required. Either select from dropdown or enter a new one.\n");
        }
        if (vehicleIdField.getText().trim().isEmpty()) {
            errorMessage.append("Vehicle ID is required.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Displays an alert dialog with the given information
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays a success message
     */
    private void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", null, message);
    }

    /**
     * Displays an error message
     */
    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", null, message);
    }

    /**
     * Adds a message to the display area
     */
    private void showMessage(String message) {
        displayArea.appendText("\n>>> " + message + "\n");
    }

    /**
     * Returns the current dealer ID from either the dropdown or text field
     */
    private String getDealerId() {
        if (dealerIdComboBox.getValue() != null &&
                !dealerIdComboBox.getValue().isEmpty() &&
                !dealerIdComboBox.getValue().equals("-- New Dealer --")) {
            return dealerIdComboBox.getValue();
        } else {
            return dealerIdField.getText().trim();
        }
    }

    /**
     * Creates appropriate vehicle object based on selected type
     */
    private Vehicle createVehicleFromType() {
        return switch (vehicleTypeComboBox.getValue()) {
            case "SUV" -> new SUV();
            case "Sedan" -> new Sedan();
            case "Pickup" -> new Pickup();
            case "Sports Car" -> new SportsCar();
            default -> null;
        };
    }

    /**
     * Clears all input fields after successful operations
     */
    private void clearInputFields() {
        dealerIdField.clear();
        dealerIdField.setDisable(false);
        dealerIdComboBox.setValue(null);
        vehicleTypeComboBox.setValue("SUV");
        vehicleIdField.clear();
        manufacturerField.clear();
        modelField.clear();
        priceField.clear();
    }

    /**
     * Gets available vehicles for a specific dealer
     */
    private java.util.List<String> getAvailableVehiclesForDealer(String dealerId) {
        java.util.List<String> availableVehicles = new java.util.ArrayList<>();

        for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
            if (vehicle.getDealerId().equals(dealerId) &&
                    !(vehicle instanceof SportsCar) &&
                    !vehicle.isRented()) {
                availableVehicles.add(vehicle.getVehicleId() + " - " +
                        vehicle.getManufacturer() + " " +
                        vehicle.getModel());
            }
        }

        return availableVehicles;
    }

    /**
     * Gets the vehicle ID from a formatted vehicle string
     */
    private String getVehicleIdFromFormatted(String formattedVehicle) {
        if (formattedVehicle == null || formattedVehicle.isEmpty()) return "";
        int index = formattedVehicle.indexOf(" - ");
        if (index > 0) {
            return formattedVehicle.substring(0, index);
        }
        return formattedVehicle;
    }

    /**
     * Handles the addition of a new vehicle to the inventory
     */
    private void handleAddVehicle() {
        try {
            if (!validateFields()) return;

            String dealerId = getDealerId();
            Vehicle vehicle = createVehicleFromType();
            if (vehicle == null) return;

            vehicle.setVehicleId(vehicleIdField.getText().trim());
            vehicle.setManufacturer(manufacturerField.getText().trim());
            vehicle.setModel(modelField.getText().trim());
            vehicle.setPrice(Double.parseDouble(priceField.getText().trim()));
            vehicle.setAcquisitionDate(new Date());
            vehicle.setDealerId(dealerId);

            File inventoryFile = new File(INVENTORY_PATH);
            if (manager.addVehicleToInventory(vehicle, inventoryFile)) {
                refreshDisplay();
                updateDealerDropdown(); // Update dealer dropdown after adding
                clearInputFields();
                showSuccess("Vehicle added to inventory successfully!");
            } else {
                showError("Cannot add vehicle - Acquisition is disabled for dealer " + dealerId);
            }
        } catch (Exception ex) {
            showError("Error adding vehicle: " + ex.getMessage());
        }
    }

    /**
     * Handles the removal of a vehicle from the inventory
     */
    private void handleRemoveVehicle() {
        try {
            if (!validateFields()) return;

            String dealerId = getDealerId();
            String vehicleId = vehicleIdField.getText().trim();
            String manufacturer = manufacturerField.getText().trim();
            String model = modelField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            File inventoryFile = new File(INVENTORY_PATH);
            if (manager.removeVehicleFromInventory(dealerId, vehicleId, manufacturer, model, price, inventoryFile)) {
                refreshDisplay();
                clearInputFields();
                showSuccess("Vehicle removed from inventory successfully!");
            } else {
                showError("Vehicle not found, is rented, or could not be removed!");
            }
        } catch (Exception ex) {
            showError("Error removing vehicle: " + ex.getMessage());
        }
    }

    /**
     * Handles renting a vehicle with a simpler data structure
     */
    private void handleRentVehicle() {
        try {
            // Create a custom dialog for rental
            Dialog<RentalInfo> dialog = new Dialog<>();
            dialog.setTitle("Rent Vehicle");
            dialog.setHeaderText("Select vehicle and rental period");

            // Set the button types
            ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

            // Create the selection fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Create dealer dropdown
            ComboBox<String> rentalDealerCombo = new ComboBox<>();
            rentalDealerCombo.setPromptText("Select a dealer");
            java.util.Set<String> dealerIds = new java.util.HashSet<>();

            for (Vehicle vehicle : manager.getVehiclesForDisplay()) {
                dealerIds.add(vehicle.getDealerId());
            }
            rentalDealerCombo.getItems().addAll(dealerIds);

            // Create vehicle dropdown (initially empty)
            ComboBox<String> vehicleCombo = new ComboBox<>();
            vehicleCombo.setPromptText("Select a vehicle");

            // Update vehicle dropdown when dealer changes
            rentalDealerCombo.setOnAction(e -> {
                String selectedDealer = rentalDealerCombo.getValue();
                if (selectedDealer != null) {
                    vehicleCombo.getItems().clear();
                    vehicleCombo.getItems().addAll(getAvailableVehiclesForDealer(selectedDealer));
                }
            });

            // Date fields
            TextField startDateField = new TextField();
            startDateField.setPromptText("MM/DD/YYYY");
            TextField endDateField = new TextField();
            endDateField.setPromptText("MM/DD/YYYY");

            grid.add(new Label("Dealer:"), 0, 0);
            grid.add(rentalDealerCombo, 1, 0);
            grid.add(new Label("Vehicle:"), 0, 1);
            grid.add(vehicleCombo, 1, 1);
            grid.add(new Label("Start Date:"), 0, 2);
            grid.add(startDateField, 1, 2);
            grid.add(new Label("End Date:"), 0, 3);
            grid.add(endDateField, 1, 3);

            dialog.getDialogPane().setContent(grid);

            // Convert the result using a custom class instead of nested Pairs
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    RentalInfo info = new RentalInfo();
                    info.dealerId = rentalDealerCombo.getValue();
                    info.vehicleId = getVehicleIdFromFormatted(vehicleCombo.getValue());
                    info.startDate = startDateField.getText();
                    info.endDate = endDateField.getText();
                    return info;
                }
                return null;
            });

            Optional<RentalInfo> result = dialog.showAndWait();

            result.ifPresent(info -> {
                if (info.dealerId == null || info.vehicleId == null ||
                        info.startDate.isEmpty() || info.endDate.isEmpty()) {
                    showError("All fields are required");
                    return;
                }

                File inventoryFile = new File(INVENTORY_PATH);
                boolean success = manager.rentVehicle(
                        info.dealerId, info.vehicleId, info.startDate, info.endDate, inventoryFile);

                if (success) {
                    refreshDisplay();
                    showSuccess("Vehicle rented successfully");
                } else {
                    showError("Failed to rent vehicle. Vehicle may be already rented or not found.");
                }
            });
        } catch (Exception ex) {
            showError("Error renting vehicle: " + ex.getMessage());
        }
    }

    /**
     * Handles returning a rented vehicle
     */
    private void handleReturnVehicle() {
        try {
            if (!validateDealerAndVehicleIds()) return;

            String dealerId = getDealerId();
            String vehicleId = vehicleIdField.getText().trim();

            File inventoryFile = new File(INVENTORY_PATH);
            boolean success = manager.returnVehicle(dealerId, vehicleId, inventoryFile);

            if (success) {
                refreshDisplay();
                showSuccess("Vehicle returned successfully");
            } else {
                showError("Failed to return vehicle. Verify the vehicle is rented.");
            }
        } catch (Exception ex) {
            showError("Error returning vehicle: " + ex.getMessage());
        }
    }

    /**
     * Handles transferring a vehicle between dealerships
     */
    private void handleTransferVehicle() {
        try {
            if (!validateDealerAndVehicleIds()) return;

            String sourceDealerId = getDealerId();
            String vehicleId = vehicleIdField.getText().trim();

            // Create input dialog for target dealer
            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("Transfer Vehicle");
            inputDialog.setHeaderText("Enter target dealer information");
            inputDialog.setContentText("Target Dealer ID:");

            Optional<String> result = inputDialog.showAndWait();

            result.ifPresent(targetDealerId -> {
                if (targetDealerId.isEmpty()) {
                    showError("Target Dealer ID is required");
                    return;
                }

                File inventoryFile = new File(INVENTORY_PATH);
                boolean success = manager.transferVehicle(sourceDealerId, targetDealerId, vehicleId, inventoryFile);

                if (success) {
                    refreshDisplay();
                    showSuccess("Vehicle transferred successfully");
                } else {
                    showError("Failed to transfer vehicle. Check all IDs and make sure the vehicle isn't rented.");
                }
            });
        } catch (Exception ex) {
            showError("Error transferring vehicle: " + ex.getMessage());
        }
    }

    /**
     * Handles importing vehicles from XML file
     */
    private void handleImportXML() {
        try {
            fileChooser.setTitle("Open XML File");
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                File inventoryFile = new File(INVENTORY_PATH);
                int importCount = manager.importXMLFile(selectedFile, inventoryFile);

                if (importCount > 0) {
                    refreshDisplay();
                    showSuccess("Successfully imported " + importCount + " vehicles from XML");
                } else {
                    showMessage("No vehicles were imported from XML");
                }
            }
        } catch (Exception ex) {
            showError("Error importing XML: " + ex.getMessage());
        }
    }

    /**
     * Handles enabling acquisition for a dealer
     */
    private void handleEnableAcquisition() {
        String dealerId = getDealerId();
        if (dealerId.isEmpty()) {
            showError("Dealer ID is required to enable acquisition!");
            return;
        }

        if (manager.enableAcquisition(dealerId)) {
            showSuccess("Acquisition enabled for dealer: " + dealerId);
        }
    }

    /**
     * Handles disabling acquisition for a dealer
     */
    private void handleDisableAcquisition() {
        String dealerId = getDealerId();
        if (dealerId.isEmpty()) {
            showError("Dealer ID is required to disable acquisition!");
            return;
        }

        if (manager.disableAcquisition(dealerId)) {
            showSuccess("Acquisition disabled for dealer: " + dealerId);
        }
    }

    /**
     * Handles exporting current inventory to export.json
     */
    private void handleExportInventory() {
        File inventoryFile = new File(INVENTORY_PATH);
        File exportFile = new File(EXPORT_PATH);

        if (!inventoryFile.exists()) {
            showError("inventory.json not found!");
            return;
        }

        try {
            if (manager.exportInventoryToExport(inventoryFile, exportFile)) {
                showSuccess("Successfully exported to export.json");
            } else {
                showError("Failed to export: No vehicles found in inventory");
            }
        } catch (Exception e) {
            showError("Error during export: " + e.getMessage());
        }
    }

    /**
     * Handles clearing the export.json file
     */
    private void handleClearExport() {
        try {
            manager.clearExportFile(new File(EXPORT_PATH));
            showSuccess("export.json has been cleared");
        } catch (Exception e) {
            showError("Error clearing export.json: " + e.getMessage());
        }
    }

    /**
     * Simple class to hold rental information
     */
    private static class RentalInfo {
        public String dealerId;
        public String vehicleId;
        public String startDate;
        public String endDate;
    }

    /**
     * Inner class to hold a pair of values
     */
    private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

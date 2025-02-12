package org.example;

import java.io.File;
import java.util.*;

/**
 * The DealershipManager class manages all the dealerships and the inventories.
 * This class provides the methods add, remove, export vehicle, enable acquisition, and lastly disable acquisition.
 */
public class DealershipManager {
    private Map<String, Dealership> dealerships = new HashMap<>();          // stores the dealership by their id
    private final JSONFileHandler jsonFileHandler = new JSONFileHandler();  // Handles all the JSON files

    /**
     * Reads the inventory and loads the vehicles into their respective dealership
     * @param file The inventory file that you want to read form
     */
    public void readInventoryFile(File file) {
        List<Vehicle> vehicles = jsonFileHandler.readInventory(file);
        for (Vehicle vehicle : vehicles) {
            String dealerId = vehicle.getDealerId();
            processAddVehicleCommand(dealerId, vehicle);
        }
    }

    /**
     * A command to add a vehicle to a dealership
     * @param dealerId  Unique id for dealership
     * @param vehicle   The vehicle you want added
     * @return  true if the vehicle was added, false otherwise
     */
    public boolean processAddVehicleCommand(String dealerId, Vehicle vehicle) {
        Dealership dealership = dealerships.computeIfAbsent(dealerId,
                k -> {
                    Dealership d = new Dealership(dealerId);
                    d.enableAcquisition();
                    return d;
                });

        if (!dealership.isAcquisitionEnabled()) {
            System.out.println("Cannot add vehicle: Acquisition disabled for dealer " + dealerId);
            return false;
        }

        if (dealership.addVehicle(vehicle)) {
            System.out.println("Vehicle added successfully to dealer " + dealerId);
            return true;
        } else {
            System.out.println("Failed to add vehicle: Duplicate vehicle ID");
            return false;
        }
    }

    /**
     * Adds a vehicle to the inventory as well as updating the dealership
     * @param vehicle The vehicle you want added
     * @param inventoryFile The file where the inventory is stored
     * @return true if the vehicle was added, false otherwise
     */
    public boolean addVehicleToInventory(Vehicle vehicle, File inventoryFile) {
        // Check if acquisition is enabled
        Dealership dealership = dealerships.get(vehicle.getDealerId());
        if (dealership != null && !dealership.isAcquisitionEnabled()) {
            return false;
        }

        List<Vehicle> currentInventory = jsonFileHandler.readInventory(inventoryFile);
        currentInventory.add(vehicle);
        jsonFileHandler.writeInventory(currentInventory, inventoryFile);
        return processAddVehicleCommand(vehicle.getDealerId(), vehicle);
    }

    /**
     * Removes a vehicle from inventory and dealership lists.
     * @param dealerId The unique id of a dealership
     * @param vehicleId The id of the vehicle you want removed
     * @param manufacturer  The manufacturer of the vehicle
     * @param model The model of the vehicle
     * @param price The price of the vehicle
     * @param inventoryFile The file where the inventory is stored
     * @return  ture if the vehicle was removed, otherwise false
     */
    public boolean removeVehicleFromInventory(String dealerId, String vehicleId, String manufacturer,
                                              String model, double price, File inventoryFile) {
        // Read current inventory
        List<Vehicle> currentInventory = jsonFileHandler.readInventory(inventoryFile);
        boolean removed = false;

        // Find and remove the vehicle that matches all criteria
        Vehicle vehicleToRemove = null;
        for (Vehicle vehicle : currentInventory) {
            if (vehicle.getDealerId().equals(dealerId) &&
                    vehicle.getVehicleId().equals(vehicleId) &&
                    vehicle.getManufacturer().equals(manufacturer) &&
                    vehicle.getModel().equals(model) &&
                    Math.abs(vehicle.getPrice() - price) < 0.01) {
                vehicleToRemove = vehicle;
                break;
            }
        }

        if (vehicleToRemove != null) {
            currentInventory.remove(vehicleToRemove);
            jsonFileHandler.writeInventory(currentInventory, inventoryFile);

            // Also remove from dealership
            Dealership dealership = dealerships.get(dealerId);
            if (dealership != null) {
                List<Vehicle> dealerVehicles = dealership.getVehicles();
                dealerVehicles.remove(vehicleToRemove);
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Exports the inventory to an external file
     * @param inventoryFile The inventory file
     * @param exportFile The destination export file
     * @return true if export is successful, otherwise false
     */
    public boolean exportInventoryToExport(File inventoryFile, File exportFile) {
        List<Vehicle> inventory = jsonFileHandler.readInventory(inventoryFile);
        if (inventory.isEmpty()) {
            System.out.println("No vehicles to export.");
            return false;
        }

        try {
            jsonFileHandler.writeInventory(inventory, exportFile);
            System.out.println("Exported " + inventory.size() + " vehicles to export.json");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clears the export file by writing an empty inventory list
     * @param exportFile The file you want cleared
     */
    public void clearExportFile(File exportFile) {
        List<Vehicle> emptyList = new ArrayList<>();
        jsonFileHandler.writeInventory(emptyList, exportFile);
    }

    /**
     * Enables vehicle acquistion for a specific dealership
     * @param dealerId The unique id of the dealership
     * @return true after acquisition was enabled
     */
    public boolean enableAcquisition(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId);
            dealerships.put(dealerId, dealership);
        }
        dealership.enableAcquisition();
        return true;
    }

    /**
     * Disables vehicle acquisition for a specfic dealership
     * @param dealerId THe unique id of the dealership
     * @return  true after disabling acquisition
     */
    public boolean disableAcquisition(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId);
            dealerships.put(dealerId, dealership);
        }
        dealership.disableAcquisition();
        return true;
    }

    /**
     * Gets a list of all vehicles for all the dealerships
     * @return A list containing all vehicles in all the dealerships
     */
    public List<Vehicle> getVehiclesForDisplay() {
        List<Vehicle> allVehicles = new ArrayList<>();
        for (Dealership dealership : dealerships.values()) {
            allVehicles.addAll(dealership.getVehicles());
        }
        return allVehicles;
    }
}
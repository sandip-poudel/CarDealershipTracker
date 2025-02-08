package org.example;

import java.io.File;
import java.util.*;

public class DealershipManager {
    private Map<String, Dealership> dealerships = new HashMap<>();
    private final JSONFileHandler jsonFileHandler = new JSONFileHandler();

    public void readInventoryFile(File file) {
        List<Vehicle> vehicles = jsonFileHandler.readInventory(file);
        for (Vehicle vehicle : vehicles) {
            String dealerId = vehicle.getDealerId();
            processAddVehicleCommand(dealerId, vehicle);
        }
    }

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

    public boolean addVehicleToInventory(Vehicle vehicle, File inventoryFile) {
        // First check if acquisition is enabled
        Dealership dealership = dealerships.get(vehicle.getDealerId());
        if (dealership != null && !dealership.isAcquisitionEnabled()) {
            return false;
        }

        List<Vehicle> currentInventory = jsonFileHandler.readInventory(inventoryFile);
        currentInventory.add(vehicle);
        jsonFileHandler.writeInventory(currentInventory, inventoryFile);
        return processAddVehicleCommand(vehicle.getDealerId(), vehicle);
    }

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

    public void clearExportFile(File exportFile) {
        List<Vehicle> emptyList = new ArrayList<>();
        jsonFileHandler.writeInventory(emptyList, exportFile);
    }

    public boolean enableAcquisition(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId);
            dealerships.put(dealerId, dealership);
        }
        dealership.enableAcquisition();
        return true;
    }

    public boolean disableAcquisition(String dealerId) {
        Dealership dealership = dealerships.get(dealerId);
        if (dealership == null) {
            dealership = new Dealership(dealerId);
            dealerships.put(dealerId, dealership);
        }
        dealership.disableAcquisition();
        return true;
    }

    public List<Vehicle> getVehiclesForDisplay() {
        List<Vehicle> allVehicles = new ArrayList<>();
        for (Dealership dealership : dealerships.values()) {
            allVehicles.addAll(dealership.getVehicles());
        }
        return allVehicles;
    }
}
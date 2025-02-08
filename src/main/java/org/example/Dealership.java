package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dealership {
    private String dealerId;
    private boolean isAcquisitionEnabled = true;
    private List<Vehicle> vehicles = new ArrayList<>();

    public Dealership(String dealerId) {
        this.dealerId = dealerId;
    }

    public void enableAcquisition() { isAcquisitionEnabled = true; }
    public void disableAcquisition() { isAcquisitionEnabled = false; }
    public boolean isAcquisitionEnabled() { return isAcquisitionEnabled; }

    public boolean addVehicle(Vehicle vehicle) {
        // Check if vehicle with same ID already exists
        if (vehicles.stream().anyMatch(v -> v.getVehicleId().equals(vehicle.getVehicleId()))) {
            return false;
        }
        // Add the new vehicle to the list
        vehicles.add(vehicle);
        return true;
    }

    public void exportToJSON(File file) {
        // Instead of creating a new handler each time, consider making this a class field
        JSONFileHandler handler = new JSONFileHandler();
        // This will now append to existing inventory rather than overwrite
        handler.writeInventory(vehicles, file);
    }

    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles); // Return a copy to prevent external modification
    }

    public String getDealerId() {
        return dealerId;
    }

    public void showVehicles() {
        System.out.println("\nDealership ID: " + dealerId);
        System.out.println("Total vehicles: " + vehicles.size());
        vehicles.forEach(v -> System.out.println(
                "Type: " + v.getClass().getSimpleName() +
                        ", ID: " + v.getVehicleId() +
                        ", Manufacturer: " + v.getManufacturer() +
                        ", Model: " + v.getModel() +
                        ", Price: $" + v.getPrice()
        ));
    }
}
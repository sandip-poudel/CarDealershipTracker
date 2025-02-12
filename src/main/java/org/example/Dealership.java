package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The Dealership class is a car dealership that manages an inventory of vehicle.
 */
public class Dealership {
    private String dealerId;                            // Unique id for dealership
    private boolean isAcquisitionEnabled = true;        // Controls whether vehicle acquisition is allowed
    private List<Vehicle> vehicles = new ArrayList<>(); // List that stores vehicle in the dealership

    /**
     * Constructor that initialize a Dealership
     * @param dealerId Unique id for dealership
     */
    public Dealership(String dealerId) {
        this.dealerId = dealerId;
    }

    /**
     * Enables vehicle acquisition
     */
    public void enableAcquisition() {
        isAcquisitionEnabled = true;
    }

    /**
     * Disables vehicle acquisition
     */
    public void disableAcquisition() {
        isAcquisitionEnabled = false;
    }

    /**
     * Checks if the acquisition is enabled or not
     * @return true if acquisition is enabled, otherwise false
     */
    public boolean isAcquisitionEnabled() {
        return isAcquisitionEnabled;
    }

    /**
     * Adds a vegucke to the inventory if it doesn't exist
     * @param vehicle The vehicle you want to add
     * @return true if vehicle was added, false otherwise
     */
    public boolean addVehicle(Vehicle vehicle) {
        // Check if vehicle with same ID already exists
        if (vehicles.stream().anyMatch(v -> v.getVehicleId().equals(vehicle.getVehicleId()))) {
            return false;
        }
        // Add the new vehicle
        vehicles.add(vehicle);
        return true;
    }

    /**
     * Exports the inventory to a JSON file
     * @param file The file that the inventory is going to be written into
     */
    public void exportToJSON(File file) {
        JSONFileHandler handler = new JSONFileHandler();
        handler.writeInventory(vehicles, file);
    }

    /**
     * Gets a copy of the vehicle inventory
     * @return A list of vehicles in the inventory
     */
    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    /**
     * Gets dealer id
     * @return dealerId
     */
    public String getDealerId() {
        return dealerId;
    }

    /**
     * Displays the details of all the vehicles in the dealership
     */
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
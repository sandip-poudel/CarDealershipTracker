package org.example; // Declares the package name

import com.fasterxml.jackson.databind.*; // Imports Jackson's ObjectMapper and related classes
import java.io.File; // Imports File class for file operations
import java.io.IOException; // Imports IOException for handling IO exceptions
import java.util.*; // Imports utility classes like List, ArrayList, Map, HashMap, etc.

public class JSONFileHandler {
    private final ObjectMapper objectMapper; // Declares an ObjectMapper instance to handle JSON processing

    public JSONFileHandler() {
        objectMapper = new ObjectMapper(); // Initializes the ObjectMapper instance
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Allows deserialization even if unknown properties exist
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true); // Enables case-insensitive property mapping
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enables pretty-printing of JSON output
    }

    // Reads vehicle inventory from a JSON file and returns a list of Vehicle objects
    public List<Vehicle> readInventory(File file) {
        try {
            if (!file.exists()) { // Checks if the file exists
                return new ArrayList<>(); // Returns an empty list if the file does not exist
            }
            JsonNode rootNode = objectMapper.readTree(file); // Parses JSON file into a JsonNode
            JsonNode inventory = rootNode.get("car_inventory"); // Extracts the "car_inventory" node
            if (inventory == null) return Collections.emptyList(); // Returns empty list if "car_inventory" is not found

            List<Vehicle> vehicles = new ArrayList<>(); // Initializes a list to store Vehicle objects
            for (JsonNode node : inventory) { // Iterates through each JSON node in "car_inventory"
                Vehicle vehicle = inferVehicleType(node); // Infers the vehicle type based on model name
                if (vehicle != null) { // Checks if a valid vehicle object was created
                    vehicles.add(vehicle); // Adds the vehicle to the list
                }
            }
            return vehicles; // Returns the list of vehicles
        } catch (IOException e) { // Catches any IO exception
            e.printStackTrace(); // Prints stack trace for debugging
            return Collections.emptyList(); // Returns an empty list in case of an error
        }
    }

    // Infers the type of vehicle based on the model name
    private Vehicle inferVehicleType(JsonNode node) {
        try {
            String model = node.get("vehicle_model").asText().toLowerCase(); // Extracts and converts model name to lowercase
            Vehicle vehicle; // Declares a Vehicle object

            // Determines the vehicle type based on model name
            if (model.contains("cr-v") || model.contains("explorer")) {
                vehicle = new SUV(); // Assigns an SUV instance if model matches
            } else if (model.contains("model 3")) {
                vehicle = new Sedan(); // Assigns a Sedan instance if model matches
            } else if (model.contains("silverado")) {
                vehicle = new Pickup(); // Assigns a Pickup instance if model matches
            } else if (model.contains("supra")) {
                vehicle = new SportsCar(); // Assigns a SportsCar instance if model matches
            } else {
                vehicle = new SUV(); // Defaults to an SUV if model type is unknown
            }

            // Sets vehicle properties from the JSON node
            vehicle.setVehicleId(node.get("vehicle_id").asText()); // Sets vehicle ID
            vehicle.setManufacturer(node.get("vehicle_manufacturer").asText()); // Sets manufacturer name
            vehicle.setModel(node.get("vehicle_model").asText()); // Sets vehicle model
            vehicle.setPrice(node.get("price").asDouble()); // Sets vehicle price
            vehicle.setDealerId(node.get("dealership_id").asText()); // Sets dealership ID
            vehicle.setAcquisitionDate(new Date(node.get("acquisition_date").asLong())); // Sets acquisition date

            return vehicle; // Returns the created vehicle object
        } catch (Exception e) { // Catches any exception
            e.printStackTrace(); // Prints stack trace for debugging
            return null; // Returns null if an error occurs
        }
    }

    // Writes the vehicle inventory to a JSON file
    public void writeInventory(List<Vehicle> vehicles, File file) {
        try {
            List<Vehicle> existingVehicles = readInventory(file); // Reads existing inventory
            Map<String, Map<String, Object>> vehicleMap = new HashMap<>(); // Initializes a map to store unique vehicles

            // Converts each vehicle object into a map and stores it in the vehicleMap
            for (Vehicle vehicle : vehicles) {
                Map<String, Object> vehicleData = new HashMap<>(); // Initializes a map for vehicle properties
                vehicleData.put("vehicle_id", vehicle.getVehicleId()); // Stores vehicle ID
                vehicleData.put("vehicle_manufacturer", vehicle.getManufacturer()); // Stores manufacturer
                vehicleData.put("vehicle_model", vehicle.getModel()); // Stores model name
                vehicleData.put("acquisition_date", vehicle.getAcquisitionDate().getTime()); // Stores acquisition date as timestamp
                vehicleData.put("price", vehicle.getPrice()); // Stores vehicle price
                vehicleData.put("dealership_id", vehicle.getDealerId()); // Stores dealership ID

                vehicleMap.put(vehicle.getVehicleId(), vehicleData); // Stores the vehicle data in the map
            }

            // Creates a wrapper map with "car_inventory" key
            Map<String, List<Map<String, Object>>> wrapper = new HashMap<>();
            wrapper.put("car_inventory", new ArrayList<>(vehicleMap.values())); // Adds all unique vehicle entries to the wrapper
            objectMapper.writeValue(file, wrapper); // Writes the inventory to the JSON file
        } catch (IOException e) { // Catches any IO exception
            e.printStackTrace(); // Prints stack trace for debugging
        }
    }
}

// This code is partially generated by claude.ai Sonnet 3.5 version
// Original Author: Sandip Poudel

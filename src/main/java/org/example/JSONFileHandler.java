package org.example;

import com.fasterxml.jackson.databind.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JSONFileHandler {
    private final ObjectMapper objectMapper;

    public JSONFileHandler() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<Vehicle> readInventory(File file) {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode inventory = rootNode.get("car_inventory");
            if (inventory == null) return Collections.emptyList();

            List<Vehicle> vehicles = new ArrayList<>();
            for (JsonNode node : inventory) {
                // Infer vehicle type from the model
                Vehicle vehicle = inferVehicleType(node);
                if (vehicle != null) {
                    vehicles.add(vehicle);
                }
            }
            return vehicles;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Vehicle inferVehicleType(JsonNode node) {
        try {
            String model = node.get("vehicle_model").asText().toLowerCase();
            Vehicle vehicle;

            // Infer type based on model
            if (model.contains("cr-v") || model.contains("explorer")) {
                vehicle = new SUV();
            } else if (model.contains("model 3")) {
                vehicle = new Sedan();
            } else if (model.contains("silverado")) {
                vehicle = new Pickup();
            } else if (model.contains("supra")) {
                vehicle = new SportsCar();
            } else {
                // Default to SUV if can't determine
                vehicle = new SUV();
            }

            // Set all the properties
            vehicle.setVehicleId(node.get("vehicle_id").asText());
            vehicle.setManufacturer(node.get("vehicle_manufacturer").asText());
            vehicle.setModel(node.get("vehicle_model").asText());
            vehicle.setPrice(node.get("price").asDouble());
            vehicle.setDealerId(node.get("dealership_id").asText());
            vehicle.setAcquisitionDate(new Date(node.get("acquisition_date").asLong()));

            return vehicle;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeInventory(List<Vehicle> vehicles, File file) {
        try {
            // Read existing inventory
            List<Vehicle> existingVehicles = readInventory(file);

            // Create a map to track unique vehicle IDs
            Map<String, Map<String, Object>> vehicleMap = new HashMap<>();

            // Convert vehicles to maps and store them
            for (Vehicle vehicle : vehicles) {
                Map<String, Object> vehicleData = new HashMap<>();
                vehicleData.put("vehicle_id", vehicle.getVehicleId());
                vehicleData.put("vehicle_manufacturer", vehicle.getManufacturer());
                vehicleData.put("vehicle_model", vehicle.getModel());
                vehicleData.put("acquisition_date", vehicle.getAcquisitionDate().getTime());
                vehicleData.put("price", vehicle.getPrice());
                vehicleData.put("dealership_id", vehicle.getDealerId());

                vehicleMap.put(vehicle.getVehicleId(), vehicleData);
            }

            // Create wrapper map and write to file
            Map<String, List<Map<String, Object>>> wrapper = new HashMap<>();
            wrapper.put("car_inventory", new ArrayList<>(vehicleMap.values()));
            objectMapper.writeValue(file, wrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package com.team3n1.smarthome.application;

import com.team3n1.smarthome.core.model.SmartDevice;
import java.util.HashMap;
import java.util.Map;

/**
 * DeviceRegistry maintains a central registry of all smart devices in the system.
 * 
 * Responsibilities:
 * - Store devices by their ID
 * - Retrieve devices for rule execution (RulesEngine needs to find the target device)
 * - RuleFactory uses this to validate that referenced devices exist (RQ_03)
 * - Supports adding/removing devices during runtime
 * 
 * Design Notes:
 * - Use internal Map<String, SmartDevice> to store devices by ID
 * - MVP: In-memory only (no persistence)
 * - No thread safety for MVP (single-threaded simulator is acceptable)
 * 
 * @author Team 3n1
 * @version MVP
 */
public class DeviceRegistry {
    
    // TODO: Add field - IMPLEMENT
    // Type: Map<String, SmartDevice>
    // Purpose: Store devices with their ID as key
    // Example: devices.put("light_1", new LightDevice("light_1"))
    private Map<String, SmartDevice> devices;
    
    // TODO: Constructor - IMPLEMENT
    // Initialize the devices Map (use HashMap)
    // Log: System.out.println("[REGISTRY] DeviceRegistry initialized");
    public DeviceRegistry() {
        // TODO: IMPLEMENT
    }
    
    // TODO: registerDevice() - IMPLEMENT
    // Parameters: SmartDevice device
    // Purpose: Add a device to the registry
    // Validation:
    //   - Device cannot be null
    //   - Device ID cannot be null or empty (get via device.getDeviceId())
    //   - Device ID should not already exist (overwriting is ok for MVP, but log a warning)
    // Logging: "[REGISTRY] Registered device: " + device.getDeviceId() + " type: " + device.getType()
    // No return value needed
    public void registerDevice(SmartDevice device) {
        // TODO: IMPLEMENT
    }
    
    // TODO: getDevice() - IMPLEMENT
    // Parameters: String deviceId
    // Return: SmartDevice or null if not found
    // Used by: RuleFactory to validate devices exist, RulesEngine to execute actions on devices
    // Purpose: Retrieve a device by ID for execution
    public SmartDevice getDevice(String deviceId) {
        // TODO: IMPLEMENT
        return null;
    }
    
    // TODO: deviceExists() - IMPLEMENT
    // Parameters: String deviceId
    // Return: boolean (true if device registered, false otherwise)
    // Used by: RuleFactory validation (check device exists before accepting rule)
    // This is a convenience method that prevents null-checking elsewhere
    public boolean deviceExists(String deviceId) {
        // TODO: IMPLEMENT
        return false;
    }
    
    // TODO: getAllDevices() - IMPLEMENT
    // Return: Collection<SmartDevice> or Map<String, SmartDevice> with all devices
    // Used by: Simulator, testing, diagnostics
    // Note: For MVP, returning the map values is fine
    public Map<String, SmartDevice> getAllDevices() {
        // TODO: IMPLEMENT
        return null;
    }
    
}

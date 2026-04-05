package com.team3n1.smarthome.application;

import com.team3n1.smarthome.core.model.SmartDevice;
import com.team3n1.smarthome.core.exceptions.DomainException;
import com.team3n1.smarthome.infrastructure.logging.AuditLogger;
import com.team3n1.smarthome.infrastructure.logging.ConsoleAuditLogger;
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
    
    // Type: Map<String, SmartDevice>
    // Purpose: Store devices with their ID as key
    // Example: devices.put("light_1", new LightDevice("light_1"))
    private Map<String, SmartDevice> devices;
    private AuditLogger auditLogger;
    
    // Initialize the devices Map (use HashMap)
    // Log: System.out.println("[REGISTRY] DeviceRegistry initialized");
    public DeviceRegistry() {
        this(new ConsoleAuditLogger());
    }

    public DeviceRegistry(AuditLogger auditLogger) {
        this.devices = new HashMap<>();
        this.auditLogger = auditLogger;
        this.auditLogger.logSystemEvent("[REGISTRY] DeviceRegistry initialized");
    }
    
    // Parameters: SmartDevice device
    // Purpose: Add a device to the registry
    // Validation:
    //   - Device cannot be null
    //   - Device ID cannot be null or empty (get via device.getDeviceId())
    //   - Device ID should not already exist (overwriting is ok for MVP, but log a warning)
    //  Throw DomainException if validation fails
    // Logging: "[REGISTRY] Registered device: " + device.getDeviceId() + " type: " + device.getType()
    // No return value needed
    public void registerDevice(SmartDevice device) {
        if (device == null) {
            throw new DomainException("INVALID_RULE", "Device cannot be null");
        }
        String deviceId = device.getDeviceId();
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new DomainException("INVALID_RULE", "Device ID cannot be null or empty");
        }
        if (devices.containsKey(deviceId)) {
            auditLogger.logSystemEvent("[REGISTRY] Warning: Device ID " + deviceId + " already exists. Overwriting.");
        }
        devices.put(deviceId, device);
        auditLogger.logSystemEvent("[REGISTRY] Registered device: " + deviceId + " type: " + device.getType());
    }
    
    // Parameters: String deviceId
    // Return: SmartDevice or null if not found
    // Used by: RuleFactory to validate devices exist, RulesEngine to execute actions on devices
    // Purpose: Retrieve a device by ID for execution
    public SmartDevice getDevice(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return null;
        }
        return devices.get(deviceId);
    }
    
    // Parameters: String deviceId
    // Return: boolean (true if device registered, false otherwise)
    // Used by: RuleFactory validation (check device exists before accepting rule)
    // This is a convenience method that prevents null-checking elsewhere
    public boolean deviceExists(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return false;
        }
        return devices.containsKey(deviceId);
    }
    
    // Return: Collection<SmartDevice> or Map<String, SmartDevice> with all devices
    // Used by: Simulator, testing, diagnostics
    // Note: For MVP, returning the map values is fine
    public Map<String, SmartDevice> getAllDevices() {
        return devices;
    }
    
}

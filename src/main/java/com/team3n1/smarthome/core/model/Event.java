package com.team3n1.smarthome.core.model;

/**
 * Event class represents something that happened in the smart home.
 * Examples: motion_detected, door_opened, light_turned_on
 * 
 * Requirements Supported:
 * - RQ_01: System accepts device events (e.g., motion_detected, door_open)
 * - RQ_04: Events are used for rule evaluation
 * 
 * Design Notes:
 * - Immutable (once created, don't change)
 * - Carries minimal data for MVP: type, source device, timestamp
 * - No constraint validation here (Rules do constraint checking)
 * 
 * @author Team 3n1
 * @version MVP
 */
public class Event {
    
    private final String eventId;
    private final String eventType;      // e.g., "motion_detected"
    private final String sourceDeviceId; // Which device emitted this event (e.g., "sensor_1")
    private final long timestamp;        // When it happened (System.currentTimeMillis())
    

    // Parameters: eventId, eventType, sourceDeviceId, timestamp
    // Hint: Validate that eventType and sourceDeviceId are not null/empty
    //       Validation: timestamp should not be in future (SDD Domain Model constraint)
    //       Throw IllegalArgumentException if validation fails
    // Example logging: System.out.println("[EVENT] " + eventType + " from " + sourceDeviceId);
    public Event(String eventId, String eventType, String sourceDeviceId, long timestamp) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be null or empty");
        }
        if (sourceDeviceId == null || sourceDeviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Source device ID cannot be null or empty");
        }
        long now = System.currentTimeMillis();
        if (timestamp > now) {
            throw new IllegalArgumentException("Timestamp cannot be in the future");
        }
        this.eventId = eventId;
        this.eventType = eventType;
        this.sourceDeviceId = sourceDeviceId;
        this.timestamp = timestamp;

        System.out.println("[EVENT] " + eventType + " from " + sourceDeviceId);
    }
    
    // Return: eventId, eventType, sourceDeviceId, timestamp
    // Use pattern: public String getEventType() { return eventType; }
    public String getEventId() {
        return eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getSourceDeviceId() {
        return sourceDeviceId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    
    // Format: "[EVENT] eventType=motion_detected sourceDevice=sensor_1 timestamp=1234567890"
    // Used for logging and debugging
    @Override
    public String toString() {
        return String.format("[EVENT] eventType=%s sourceDevice=%s timestamp=%d", 
                    eventType, sourceDeviceId, timestamp);
    }
    
}

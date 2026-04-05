package com.team3n1.smarthome.core.exceptions;

/**
 * Custom exceptions for domain validation errors.
 * These are unchecked exceptions (extend RuntimeException) because they represent
 * invalid domain state that should cause immediate failure.
 * 
 * Used by: RuleFactory, RulesEngine, Event validation
 * Requirements Supported: RQ_03 (reject rule creation on validation failure)
 * 
 * @author Team 3n1
 * @version MVP
 */

// TODO: Create InvalidRuleException class - IMPLEMENT
// Purpose: Thrown when rule validation fails (missing fields, invalid config, etc.)
// Extend: RuntimeException
// Constructor: InvalidRuleException(String message)
// Used by: RuleFactory when RQ_03 validations fail
// Example message: "Rule ID is required and cannot be null or empty"
public class InvalidRuleException extends RuntimeException {
    public InvalidRuleException(String message) {
        super(message);
    }
}

// TODO: Create UnknownDeviceException class - IMPLEMENT
// Purpose: Thrown when a rule references a device that doesn't exist
// Extend: RuntimeException
// Constructor: UnknownDeviceException(String message)
// Used by: RuleFactory when deviceRegistry.deviceExists(deviceId) returns false
// Example message: "Device 'light_5' referenced in rule but not registered in DeviceRegistry"
public class UnknownDeviceException extends RuntimeException {
    public UnknownDeviceException(String message) {
        super(message);
    }
}

// TODO: Create UnknownEventTypeException class - IMPLEMENT
// Purpose: Thrown when a rule triggers on an event type that is not recognized
// Extend: RuntimeException
// Constructor: UnknownEventTypeException(String message)
// Used by: RuleFactory to validate trigger event type (RQ_03)
// Example message: "Event type 'unknown_event' is not recognized by the system"
// Note: For MVP, you could maintain a Set<String> of valid event types or check on first use
public class UnknownEventTypeException extends RuntimeException {
    public UnknownEventTypeException(String message) {
        super(message);
    }
}

// TODO: Create RuleExecutionException class - IMPLEMENT
// Purpose: Thrown when action execution fails
// Extend: RuntimeException
// Constructor: RuleExecutionException(String message) and RuleExecutionException(String message, Throwable cause)
// Used by: RulesEngine when action.execute() throws an exception
// Logged to: AuditLogger
// Example message: "Action execution failed on device light_1: Device is offline"
public class RuleExecutionException extends RuntimeException {
    public RuleExecutionException(String message) {
        super(message);
    }
    
    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

// TODO: OPTIONAL: Create additional exceptions as needed
// Examples:
// - InvalidEventException: Event creation fails (future timestamp, etc.)
// - DeviceOfflineException: Action targets offline device
// - CircularDependencyException: Rule depends on itself (deferred to Phase 2)


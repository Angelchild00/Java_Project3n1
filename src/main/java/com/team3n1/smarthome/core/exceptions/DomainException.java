package com.team3n1.smarthome.core.exceptions;

/**
 * Single DomainException for all domain validation and runtime errors.
 * Provides error codes to distinguish different failure types without multiple exception classes.
 * 
 * This is an unchecked exception (extends RuntimeException) because it represents
 * invalid domain state that should cause immediate failure.
 * 
 * Used by: RuleFactory, RulesEngine, Event validation
 * Requirements Supported: RQ_03 (reject rule creation on validation failure), RQ_06 (action failures)
 * 
 * Error Codes:
 * - INVALID_RULE: Rule validation failed (missing fields, invalid config)
 * - UNKNOWN_DEVICE: Rule references device that doesn't exist
 * - UNKNOWN_EVENT_TYPE: Rule triggers on unrecognized event type
 * - RULE_EXECUTION_FAILED: Action execution failed at runtime
 * 
 * @author Team 3n1
 * @version MVP
 */
public class DomainException extends RuntimeException {
    
    private final String errorCode;
    
    // Constructor with error code and message
    // Parameters:
    //   errorCode: Machine-readable error type (INVALID_RULE, UNKNOWN_DEVICE, etc.)
    //   message: Human-readable error description
    // Usage: throw new DomainException("UNKNOWN_DEVICE", "Device 'light_1' not registered");
    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    // Constructor with error code, message, and cause
    // Used when wrapping another exception as root cause
    // Usage: throw new DomainException("RULE_EXECUTION_FAILED", "Action failed", cause);
    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    // Getter for error code
    // Allows callers to distinguish error types without string parsing
    // Usage: if (exception.getErrorCode().equals("UNKNOWN_DEVICE")) { ... }
    public String getErrorCode() {
        return errorCode;
    }
    
}


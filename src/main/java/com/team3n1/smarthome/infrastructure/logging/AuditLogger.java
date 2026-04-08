package com.team3n1.smarthome.infrastructure.logging;

import com.team3n1.smarthome.core.model.Event;
import com.team3n1.smarthome.core.model.Rule;
import com.team3n1.smarthome.core.actions.Action;

/**
 * AuditLogger interface defines contract for logging system events.
 * Responsible for recording all rule evaluations, actions, and outcomes.
 * 
 * Requirements Supported:
 * - RQ_10: Write audit entry for every processed event including eventID, matched ruleID, actions attempted, outcome per action
 * 
 * Design Notes:
 * - Interface allows different implementations (console, file, database)
 * - MVP: ConsoleAuditLogger implementation for console output
 * - Logs must be immutable (per SDD)
 * 
 * @author Team 3n1
 * @version MVP
 */
public interface AuditLogger {
    
    // Parameters: Event event
    // Purpose: Log that an event was received and will be processed
    // Format example: "[AUDIT] EVENT RECEIVED: eventType=motion_detected sourceDevice=sensor_1 timestamp=1234567890"
    void logEventReceived(Event event);

    // Parameters: Event event, Rule rule
    // Purpose: Log that an event matched a rule and actions will be executed
    // Format example: "[AUDIT] RULE MATCHED: ruleID=rule_1 eventType=motion_detected action_count=2"
    void logRuleMatched(Event event, Rule rule);
    
  
    // Parameters: Rule rule, Action action, String targetDeviceId
    // Purpose: Log that an action is about to be executed
    // Format example: "[AUDIT] ACTION ATTEMPT: ruleID=rule_1 actionType=TurnOnAction device=light_1"
    void logActionAttempted(Rule rule, Action action, String targetDeviceId);
    

    // Parameters: Rule rule, Action action, String targetDeviceId
    // Purpose: Log successful action execution
    // Format example: "[AUDIT] ACTION SUCCESS: ruleID=rule_1 actionType=TurnOnAction device=light_1"
    void logActionSuccess(Rule rule, Action action, String targetDeviceId);
    

    // Parameters: Rule rule, Action action, String targetDeviceId, String failureReason
    // Purpose: Log failed action execution (RQ_06, RQ_10)
    // Format example: "[AUDIT] ACTION FAILURE: ruleID=rule_1 actionType=TurnOnAction device=light_1 reason=DeviceOffline"
    void logActionFailure(Rule rule, Action action, String targetDeviceId, String failureReason);
    
  
    // Parameters: Event event, Rule rule, String reason
    // Purpose: Log when a rule matched but was not executed (e.g., rule is DISABLED or in COOLDOWN)
    // Format example: "[AUDIT] RULE SKIPPED: ruleID=rule_1 reason=RuleInDraftState"
    void logRuleSkipped(Event event, Rule rule, String reason);
    

    // Parameters: String message
    // Purpose: Log general system events (rule creation, registration, etc.)
    // Format example: "[AUDIT] SYSTEM: Registered device light_1 of type Light"
    void logSystemEvent(String message);
    
}

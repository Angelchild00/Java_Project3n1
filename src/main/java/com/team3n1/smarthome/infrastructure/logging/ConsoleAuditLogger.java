package com.team3n1.smarthome.infrastructure.logging;

import com.team3n1.smarthome.core.model.Event;
import com.team3n1.smarthome.core.model.Rule;
import com.team3n1.smarthome.core.actions.Action;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ConsoleAuditLogger implements AuditLogger by printing to System.out.
 * 
 * MVP implementation suitable for:
 * - Development and testing
 * - Simulator UI console output
 * - Debugging and traceability
 * 
 * Design Notes:
 * - Logs are printed to console in real-time
 * - Each log message starts with timestamp and [AUDIT] tag for easy filtering
 * - Format is human-readable for Phase 1
 * - No file persistence (Phase 2 feature)
 * 
 * @author Team 3n1
 * @version MVP
 */
public class ConsoleAuditLogger implements AuditLogger {
    
    // TODO: Add field - IMPLEMENT
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    // Purpose: Format timestamps for log messages
    // Used in: All log methods to add consistent timestamps
    private DateTimeFormatter formatter;
    
    // Purpose: Initialize the logger
    // Implementation:
    //   - Initialize formatter with pattern "yyyy-MM-dd HH:mm:ss" or similar
    //   - Print startup message: "[AUDIT] ConsoleAuditLogger initialized"
    public ConsoleAuditLogger() {
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("[AUDIT] ConsoleAuditLogger initialized");
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 EVENT_RECEIVED: eventType=motion_detected sourceDevice=sensor_1"
    @Override
    public void logEventReceived(Event event) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " EVENT_RECEIVED: eventType=" + event.getEventType() + " sourceDevice=" + event.getSourceDeviceId());
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 RULE_MATCHED: ruleID=rule_motion_light eventType=motion_detected actions=2"
    @Override
    public void logRuleMatched(Event event, Rule rule) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " RULE_MATCHED: ruleID=" + rule.getRuleID() + " eventType=" + event.getEventType() + " actions=" + rule.getActions().size());
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 ACTION_ATTEMPT: ruleID=rule_1 action=TurnOnAction targetDevice=light_1"
    @Override
    public void logActionAttempted(Rule rule, Action action, String targetDeviceId) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " ACTION_ATTEMPT: ruleID=" + rule.getRuleID() + " action=" + action.getDescription() + " targetDevice=" + targetDeviceId);
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 ACTION_SUCCESS: ruleID=rule_1 action=TurnOnAction device=light_1"
    @Override
    public void logActionSuccess(Rule rule, Action action, String targetDeviceId) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " ACTION_SUCCESS: ruleID=" + rule.getRuleID() + " action=" + action.getDescription() + " device=" + targetDeviceId);
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 ACTION_FAILURE: ruleID=rule_1 action=TurnOnAction device=light_1 reason=DeviceNotFound"
    @Override
    public void logActionFailure(Rule rule, Action action, String targetDeviceId, String failureReason) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " ACTION_FAILURE: ruleID=" + rule.getRuleID() + " action=" + action.getDescription() + " device=" + targetDeviceId + " reason=" + failureReason);
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 RULE_SKIPPED: ruleID=rule_1 reason=RuleStateIsDraft"
    @Override
    public void logRuleSkipped(Event event, Rule rule, String reason) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " RULE_SKIPPED: ruleID=" + rule.getRuleID() + " reason=" + reason);
    }
    
    // Example output:
    // "[AUDIT] 2026-04-05 14:32:10 SYSTEM: Registered device light_1 type=Light"
    @Override
    public void logSystemEvent(String message) {
        System.out.println("[AUDIT] " + getCurrentTimestamp() + " SYSTEM: " + message);
    }
    
    // Helper method: getCurrentTimestamp() - OPTIONAL
    // Purpose: Get formatted current timestamp for all log methods
    // Implementation: return LocalDateTime.now().format(formatter)
    // Used by: All log methods
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
    
}


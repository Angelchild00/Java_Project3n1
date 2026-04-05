package com.team3n1.smarthome.application;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import com.team3n1.smarthome.core.model.Rule;
import com.team3n1.smarthome.core.actions.Action;
import com.team3n1.smarthome.core.exceptions.*;

/**
 * RuleFactory is responsible for **validating** and **creating** rules.
 * It acts as a guard that rejects invalid configurations early.
 * 
 * Responsibilities:
 * - Validate rule configuration against business rules (RQ_03)
 * - Create Rule instances only if validation passes
 * - Throw DomainException if validation fails
 * - Does NOT store rules (RulesEngine does that)
 * 
 * Requirements Supported:
 * - RQ_02: Allow admin to create rules with required parameters
 * - RQ_03: Reject invalid rule creation (missing fields, unknown devices/events, zero actions)
 * 
 * Design Pattern: Factory (Creational)
 * - Provides interface for creating objects (rules)
 * - Encapsulates validation logic so clients don't need to validate manually
 * 
 * @author Team 3n1
 * @version MVP
 */
public class RuleFactory {
    // 1. deviceRegistry: DeviceRegistry
    //    Purpose: Validate that target devices exist (RQ_03)
    //    Used in: createRule() validation step
    //
    // 2. validEventTypes: Set<String>
    //    Purpose: Track which event types are recognized by the system
    //    Examples: "motion_detected", "door_opened", "light_turned_on"
    //    Used in: createRule() validation - reject rules with unknown event types
    //    MVP approach: Initialize with known types, or populate dynamically
    //
    private DeviceRegistry deviceRegistry;
    private Set<String> validEventTypes;
    
    // Parameters: DeviceRegistry deviceRegistry
    // Purpose: Accept dependency injection
    // Implementation:
    //   - Store deviceRegistry reference
    //   - Initialize validEventTypes Set<String> with known event types
    //   - For MVP, hardcode supported types: {"motion_detected", "door_opened", "light_turned_on"}
    //     (You can expand this later or make it configurable)
    //   - Log: System.out.println("[FACTORY] RuleFactory initialized with " + validEventTypes.size() + " event types");
    public RuleFactory(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
        this.validEventTypes = new HashSet<>();
        this.validEventTypes.add("motion_detected");
        this.validEventTypes.add("door_opened");
        this.validEventTypes.add("light_turned_on");
        System.out.println("[FACTORY] RuleFactory initialized with " + validEventTypes.size() + " event types");
    }
    
    // Parameters:
    //   - String ruleID: unique identifier for the rule
    //   - String triggerEventType: event that activates this rule (e.g., "motion_detected")
    //   - String targetDeviceId: device this rule will control (e.g., "light_1")
    //   - List<Action> actions: what to execute when rule fires
    //
    // Return: Rule (newly created rule in DRAFT state)
    //
    // Validation (RQ_03 - Reject if any of these fail):
    //   1. ruleID is not null and not empty
    //   2. triggerEventType is not null and not empty
    //   3. triggerEventType exists in validEventTypes (use validEventTypes.contains())
    //   4. targetDeviceId is not null and not empty
    //   5. deviceRegistry.deviceExists(targetDeviceId) returns true
    //   6. actions is not null and not empty (RQ_03: "rule contains zero action")
    //
    // UPDATED FOR OPTION B: Multiple rules per event type are ALLOWED
    //   - Do NOT check if another rule already listens for triggerEventType
    //   - Allow multiple rules for same event (e.g., motion_detected → light_1 AND light_2)
    //   - Conflicts are resolved at runtime in RulesEngine.processEvent()
    //
    // Error Handling (Throw specific exceptions with descriptive messages):
    //   - InvalidRuleException for missing required fields
    //   - UnknownEventTypeException if triggerEventType not recognized
    //   - UnknownDeviceException if targetDeviceId not registered
    //   - Example: throw new InvalidRuleException("Rule ID cannot be null or empty");
    //
    // Success Flow:
    //   - Create new Rule instance: new Rule(ruleID, actions)
    //   - Store triggerEventType in rule (may need to add field to Rule class)
    //   - Store targetDeviceId in rule (may need to add field to Rule class)
    //   - Log successful creation: "[FACTORY] Created rule '" + ruleID + "' for event '" + triggerEventType + "'"
    //   - Return the rule (it starts in DRAFT state per Rule constructor)
    //
    // NOTE: Rules returned are in DRAFT state. RulesEngine or user must call rule.activate()
    //       before the rule can be triggered.
    public Rule createRule(String ruleID, String triggerEventType, String targetDeviceId, List<Action> actions) {
        // VALIDATION
        if (ruleID == null || ruleID.trim().isEmpty()) {                    throw new InvalidRuleException("Rule ID cannot be null or empty");}
        if (triggerEventType == null || triggerEventType.trim().isEmpty()) {throw new InvalidRuleException("Trigger event type cannot be null or empty");}
        if (!validEventTypes.contains(triggerEventType)) {                  throw new UnknownEventTypeException("Unknown event type: " + triggerEventType);}
        if (targetDeviceId == null || targetDeviceId.trim().isEmpty()) {    throw new InvalidRuleException("Target device ID cannot be null or empty");}
        if (!deviceRegistry.deviceExists(targetDeviceId)) {                 throw new UnknownDeviceException("Unknown device: " + targetDeviceId);}
        if (actions == null || actions.isEmpty()) {                         throw new InvalidRuleException("Rule must contain at least one action");}

        // RULE CREATION AND RETURN
        Rule rule = new Rule(ruleID, actions);
        rule.setTriggerEventType(triggerEventType);
        rule.setTargetDeviceId(targetDeviceId);

        System.out.println("[FACTORY] Created rule '" + ruleID + "' for event '" + triggerEventType + "' targeting device '" + targetDeviceId + "'");
        return rule;
    }
    
    // Parameters: String eventType (e.g., "custom_event_type")
    // Purpose: Allow system to add new event types at runtime
    // Used by: Simulator or admin interface to extend event types
    // Implementation: validEventTypes.add(eventType)
    // Log: "[FACTORY] Registered new event type: " + eventType
    // For MVP: This is optional; hardcoded types are fine for Phase 1
    public void registerEventType(String eventType) {
        validEventTypes.add(eventType);
        System.out.println("[FACTORY] Registered new event type: " + eventType);
    }
    
    // Return: Set<String> of all recognized event types
    // Purpose: Used by simulator UI or diagnostics to show available event types
    // Implementation: return new HashSet<>(validEventTypes) to prevent external modification
    public Set<String> getValidEventTypes() {
        return new HashSet<>(validEventTypes);
    }
    
}

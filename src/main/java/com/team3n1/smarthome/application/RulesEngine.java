package com.team3n1.smarthome.application;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import com.team3n1.smarthome.core.model.Rule;
import com.team3n1.smarthome.core.model.Event;
import com.team3n1.smarthome.core.model.SmartDevice;
import com.team3n1.smarthome.core.model.RuleState;
import com.team3n1.smarthome.core.actions.Action;
import com.team3n1.smarthome.core.exceptions.DomainException;
import com.team3n1.smarthome.core.model.DeviceEventListener;
import com.team3n1.smarthome.infrastructure.logging.AuditLogger;

/**
 * RulesEngine is the orchestration/coordination layer for the smart home system.
 * It is the "brain" that receives events, matches them to rules, and executes actions.
 *
 * Observer pattern role: Observer (Concrete Observer)
 *   RulesEngine implements DeviceEventListener so it can be registered directly
 *   on a MotionSensor. When the sensor detects motion it calls onEvent(), which
 *   delegates to processEvent() — no manual Event construction is needed in Main.
 *
 * Responsibilities:
 * - Register and store rules
 * - Accept incoming events via processEvent()
 * - Evaluate which rules match each event
 * - Execute rule actions with error handling
 * - Log all activity to AuditLogger
 * - Enable/disable rules at runtime
 *
 * Requirements Supported:
 * - RQ_04: Evaluate all enabled rules in deterministic order
 * - RQ_05: Execute rule actions in the order defined
 * - RQ_06: Isolate action failures (one failing action doesn't stop others or other rules)
 * - RQ_10: Write audit entry for every processed event
 *
 * Design Notes:
 * - MVP: Evaluate rules in registration order (simple deterministic order)
 * - MVP: No throttling (reduced scope per professor)
 * - MVP: Event matching is simple string equality on event type
 *
 * @author Team 3n1
 * @version MVP
 */
public class RulesEngine implements DeviceEventListener {
    // 1. activeRules: List<Rule>
    //    Purpose: Store rules that are registered and ready to be evaluated
    //    Used in: processEvent() - iterate through all active rules
    //    Note: Only ACTIVE rules should be evaluated. DRAFT and DISABLED rules are skipped.
    //
    // 2. deviceRegistry: DeviceRegistry
    //    Purpose: Look up target devices when executing actions
    //    Used in: Action execution (rule.execute() needs the actual device object)
    //
    // 3. auditLogger: AuditLogger
    //    Purpose: Log all events, rule matches, and action outcomes (RQ_10)
    //    Used in: processEvent() after each action execution
    //
    private List<Rule> activeRules;
    private DeviceRegistry deviceRegistry;
    private AuditLogger auditLogger;
    private ExecutionPolicy policy;
    
    // Parameters:
    //   - DeviceRegistry deviceRegistry
    //   - AuditLogger auditLogger
    // Purpose: Accept dependency injection
    // Implementation:
    //   - Store both dependencies
    //   - Initialize activeRules as an empty ArrayList<>
    //   - Log: "[ENGINE] RulesEngine initialized"
    public RulesEngine(DeviceRegistry deviceRegistry, AuditLogger auditLogger) {
        this.deviceRegistry = deviceRegistry;
        this.auditLogger = auditLogger;
        this.activeRules = new ArrayList<>();
        this.policy = ExecutionPolicy.LENIENT;
        this.auditLogger.logSystemEvent("[ENGINE] RulesEngine initialized with policy: " + this.policy);
    }

    /**
     * Switch the execution policy at runtime (REQ-2.6).
     * Call this before processEvent() to demo LENIENT vs STRICT behaviour.
     *
     * @param policy the new policy (must not be null)
     */
    public void setPolicy(ExecutionPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("Policy must not be null");
        this.policy = policy;
        auditLogger.logSystemEvent("[ENGINE] Execution policy changed to: " + policy);
    }

    /** @return the currently active execution policy */
    public ExecutionPolicy getPolicy() {
        return policy;
    }
    
    // Parameters: Rule rule
    // Purpose: Add a rule to the active rules list
    // Validation:
    //   - Rule cannot be null
    //   - Rule should be in DRAFT state
    // Implementation:
    //   - Add rule to activeRules list
    //   - Do NOT automatically activate it; user/system must call rule.activate()
    //   - Log: "[ENGINE] Registered rule '" + rule.getRuleID() + "'"
    // Return: void
    public void registerRule(Rule rule) {
        if (rule != null) {
            if (rule.getCurrentState() == RuleState.DRAFT) {
                rule.activate();
                this.activeRules.add(rule);
                auditLogger.logSystemEvent("[ENGINE] Registered rule '" + rule.getRuleID() + "' in DRAFT state and activated");
            }
            else {
                // error log, rules should be registered in DRAFT state
                this.activeRules.add(rule);
                auditLogger.logSystemEvent("[ENGINE] Warning: Registering rule '" + rule.getRuleID() + "' which is not in DRAFT state. Current state: " + rule.getCurrentState());
            }
        }
        else {
            throw new DomainException("INVALID_RULE", "Rule cannot be null");
        }
    }
    
    // processEvent() - IMPLEMENT (This is the HOT PATH per SDD performance plan)
    // Parameters: Event event
    // Purpose: Main orchestration method. Evaluate all matching rules and execute their actions.
    // 
    // UPDATED FOR OPTION B: Multiple rules OK, but can't target same device in one event cycle
    // 
    // High-level Algorithm:
    //   1. Log event arrival: "[ENGINE] Processing event: " + event.getEventType()
    //   2. Track devices targeted in this event cycle: Set<String> devicesTargetedThisEvent
    //   3. Find ALL rules that match this event type AND are ACTIVE
    //   4. For each matching rule:
    //      a. Check if rule.getTargetDeviceId() already in devicesTargetedThisEvent
    //         - If YES: skip rule, log "Device already targeted this event"
    //         - If NO: execute rule actions, add device to devicesTargetedThisEvent
    //      b. For each action in rule:
    //         - Get target device from deviceRegistry
    //         - Execute action.execute(device)
    //         - If exception caught, log error (RQ_06: continue with next action)
    //      c. Log the action outcome to auditLogger
    //   5. Return (void) or return summary for logging
    //
    // Event Matching: rule.getTriggerEventType().equals(event.getType())
    // Device Conflict Detection: devicesTargetedThisEvent.contains(rule.getTargetDeviceId())
    // Error Handling (RQ_06): try-catch per action, continue on failure
    // Audit Logging (RQ_10): log each action attempt, success, or failure
    //
    // Example Flow:
    //   Event: motion_detected
    //   rule_1: motion_detected → light_1 → execute, devicesTargetedThisEvent.add("light_1")
    //   rule_2: motion_detected → light_1 → CONFLICT! skip, log "Device already targeted"
    //   rule_3: motion_detected → light_2 → execute, devicesTargetedThisEvent.add("light_2")
    //
    // Getting Rule Info (may need to add to Rule class):
    //   - rule.getEventType() or rule.getTriggerEventType() - what event this rule listens for
    //   - rule.getTargetDeviceId() - which device to execute actions on
    //   - rule.getActions() - list of Action objects
    //   - rule.getCurrentState() - RuleState value (check == RuleState.ACTIVE)
    //
    // Error Handling (RQ_06):
    //   try {
    //       action.execute(device);
    //       // Log success
    //   } catch (DomainException e) {
    //       // Log failure but continue with next action
    //       auditLogger.logActionFailure(...);
    //   }
    //
    // Audit Logging (RQ_10):
    //   After each action (success or failure), call auditLogger with:
    //   - Event details
    //   - Rule that matched
    //   - Action details
    //   - Outcome (success/failure)
    //   - If failure: reason/exception message
    //
    public void processEvent(Event event) {
        // LOG EVENT ARRIVAL
        auditLogger.logEventReceived(event);
        
        // INITIALIZE DEVICE CONFLICT TRACKING
        Set<String> devicesTargetedThisEvent = new HashSet<>();
        
        // FIND ALL MATCHING RULES (filter by event.getType() AND rule.isActive())
        List<Rule> matchingRules = new ArrayList<>();
        for (Rule rule : activeRules) {
            if (rule.getCurrentState() == RuleState.ACTIVE && rule.getTriggerEventType().equals(event.getEventType())) {
                matchingRules.add(rule);
            }
        }
        
        // FOR EACH MATCHING RULE
        //   - Check if rule.getTargetDeviceId() already targeted this event
        //   - If conflict: logRuleSkipped(), continue to next rule
        //   - If no conflict: execute actions, add device to targeted set
        //   - FOR EACH ACTION: try-catch, log outcome
        for (Rule rule : matchingRules) {
            String targetDeviceId = rule.getTargetDeviceId();
            auditLogger.logRuleMatched(event, rule);
            if (devicesTargetedThisEvent.contains(targetDeviceId)) {
                // Log conflict and skip
                auditLogger.logRuleSkipped(event, rule, "Device already targeted this event");
                continue; // skip to next rule
            }
            
            // No conflict, execute actions
            SmartDevice device = deviceRegistry.getDevice(targetDeviceId);
            if (device == null) {
                // This should not happen if validation is correct, but log just in case
                auditLogger.logRuleSkipped(event, rule, "Target device not found at execution time");
                continue;
            }
            
            // Mark this device as targeted for this event
            devicesTargetedThisEvent.add(targetDeviceId);
            
            // Execute each action with error handling, respecting the active policy
            for (Action action : rule.getActions()) {
                try {
                    auditLogger.logActionAttempted(rule, action, targetDeviceId);
                    action.execute(device);
                    auditLogger.logActionSuccess(rule, action, targetDeviceId);
                } catch (DomainException e) {
                    auditLogger.logActionFailure(rule, action, targetDeviceId, e.getMessage());
                    if (policy == ExecutionPolicy.STRICT) {
                        auditLogger.logSystemEvent("[ENGINE] STRICT policy: aborting remaining actions for rule '" + rule.getRuleID() + "'");
                        break; // stop processing further actions in this rule
                    }
                    // LENIENT policy: continue with next action despite failure (RQ_06)
                }
            }
        }
    }
    
    // Parameters: String ruleID
    // Purpose: Disable a rule at runtime (user or admin request)
    // Implementation:
    //   - Find rule by ID in activeRules
    //   - Call rule.disable() to change state to DISABLED
    //   - Optionally remove from activeRules or just skip during evaluation
    //   - Log: "[ENGINE] Disabled rule '" + ruleID + "'"
    // Return: void
    public void disableRule(String ruleID) {
        Rule rule = findRuleById(ruleID);
        if (rule != null) {
            rule.disable();
            auditLogger.logSystemEvent("[ENGINE] Disabled rule '" + ruleID + "'");
        }
        else {
            auditLogger.logSystemEvent("[ENGINE] Warning: Rule '" + ruleID + "' not found for disabling");
        }

    }
    
    // Parameters: String ruleID
    // Purpose: Activate a rule (transition from DRAFT to ACTIVE)
    // Implementation:
    //   - Find rule by ID in activeRules
    //   - Call rule.activate() to change state to ACTIVE
    //   - Log: "[ENGINE] Activated rule '" + ruleID + "'"
    // Return: void
    public void activateRule(String ruleID) {
        Rule rule = findRuleById(ruleID);
        if (rule != null) {
            rule.activate();
            auditLogger.logSystemEvent("[ENGINE] Activated rule '" + ruleID + "'");
        }
        else {
            auditLogger.logSystemEvent("[ENGINE] Warning: Rule '" + ruleID + "' not found for activation");
        }
    }
    
    // Return: int (number of rules currently registered)
    // Purpose: Diagnostics, testing, monitoring
    // Implementation: return activeRules.size()
    public int getRuleCount() {
        return activeRules.size();
    }

    /**
     * Observer pattern: called by a MotionSensor (Subject) when it detects an event.
     * Delegates directly to processEvent() so the sensor never needs to know about rules.
     *
     * @param event the event fired by the sensor
     */
    @Override
    public void onEvent(Event event) {
        processEvent(event);
    }
    
    // Purpose: Search for a rule by ID in activeRules
    // Implementation: iterate through activeRules, compare ruleID, return or null if not found
    // Used by: disableRule(), activateRule(), and other methods that need to find a rule
    // Consider: Should this throw exception if not found, or return null? Choose based on error handling style.
    private Rule findRuleById(String ruleID) {
        for (Rule rule : activeRules) {
            if (rule.getRuleID().equals(ruleID)) {
                return rule;
            }
        }
        return null; // not found
    }
    
}


package com.team3n1.smarthome.core.model;

import java.util.List;
import com.team3n1.smarthome.core.actions.Action;

/**
 * Rule represents a trigger-condition-action automation.
 * Structure: "If [trigger event type] occurs, then [execute actions] on [target device]"
 * 
 * Lifecycle States (per SDD):
 * - DRAFT: Rule is being created, not yet active
 * - ACTIVE: Rule is live and monitoring for trigger events
 * - TRIGGERED: Event matched this rule, actions are executing (not in MVP, simplified)
 * - DISABLED: User disabled rule, it will not be evaluated
 * 
 * Requirements Supported:
 * - RQ_02: Rules have ruleID, enabled/disabled state, trigger conditions (event type), actions
 * - RQ_05: Execute actions in the order defined
 * 
 * @author Team 3n1
 * @version MVP
 */
public class Rule{
    private String ruleID;
    private List<Action> actions;
    private RuleState currentState;
    
    // TODO: Add field - IMPLEMENT
    // Type: String triggerEventType
    // Purpose: What event type activates this rule (e.g., "motion_detected")
    // Used by: RulesEngine.processEvent() to match event types against rules
    // Initialized by: RuleFactory or constructor parameter
    private String triggerEventType;
    
    // TODO: Add field - IMPLEMENT
    // Type: String targetDeviceId
    // Purpose: Which device will the actions execute on (e.g., "light_1")
    // Used by: RulesEngine when executing actions (pass to RulesEngine for lookup)
    // Initialized by: RuleFactory or constructor parameter
    private String targetDeviceId;

    // TODO: Update constructor - IMPLEMENT
    // Current signature: Rule(String ruleID, List<Action> actions)
    // New signature should be: Rule(String ruleID, String triggerEventType, String targetDeviceId, List<Action> actions)
    // OR: Keep current constructor for backward compatibility + add setter methods below
    //
    // Recommended: Add setter methods (setTriggerEventType(), setTargetDeviceId()) 
    // and initialize fields in constructor or via setters from RuleFactory
    //
    // Initialize currentState = RuleState.DRAFT (rules start inactive)
    public Rule(String ruleID, List<Action> actions){
        this.ruleID = ruleID;
        this.actions = actions;
        this.currentState = RuleState.DRAFT; // initial state
        this.triggerEventType = null; // placeholder, should be set by RuleFactory
        this.targetDeviceId = null; // placeholder, should be set by RuleFactory
    }

    public void activate(){
        //cannot activate without actions
        if (actions == null || actions.isEmpty()){
            throw new IllegalStateException("Cannot activate rule without actions");
        }
        this.currentState = RuleState.ACTIVE; //valid transition
        System.out.println("[CHECK-STATE] Rule " + ruleID + " is in " + currentState + "state.");
    }

    public void trigger(SmartDevice device){
        if (this.currentState != RuleState.ACTIVE){
           System.out.println("[REJECTED] Rule " + ruleID + " is in " + currentState + " state.");
            return; // only trigger if active
        }
        for (Action action : actions){
            action.execute(device);
        } 

    }

    public void disable(){
        this.currentState = RuleState.DISABLED; // can disable from any state
    }

    public void addAction(Action action){
        this.actions.add(action);
    }
    
    // getRuleID()
    // Return: this.ruleID
    // Used by: RulesEngine to identify rules in logs, DeviceRegistry validation
    public String getRuleID() {
        return this.ruleID;
    }
    
    // getTriggerEventType() or getEventType()
    // Return: this.triggerEventType
    // Used by: RulesEngine.processEvent() to match event types
    public String getTriggerEventType() {
        return this.triggerEventType;
    }
    
    // getTargetDeviceId()
    // Return: this.targetDeviceId
    // Used by: RulesEngine to know which device to execute actions on
    public String getTargetDeviceId() {
        return this.targetDeviceId;
    }
    
    // getActions()
    // Return: this.actions (or copy for immutability if desired)
    // Used by: RulesEngine to iterate and execute all actions
    public List<Action> getActions() {
        return this.actions;
    }
    
    // getCurrentState()
    // Return: this.currentState
    // Used by: RulesEngine to check if rule is ACTIVE before evaluation
    public RuleState getCurrentState() {
        return this.currentState;
    }
    
    // setTriggerEventType(String triggerEventType)
    // Purpose: Set the event type this rule listens for (called by RuleFactory)
    // Parameter: triggerEventType (e.g., "motion_detected")
    public void setTriggerEventType(String triggerEventType) {
        this.triggerEventType = triggerEventType;
    }
    
    // setTargetDeviceId(String targetDeviceId)
    // Purpose: Set the device this rule controls (called by RuleFactory)
    // Parameter: targetDeviceId (e.g., "light_1")
    public void setTargetDeviceId(String targetDeviceId) {
        this.targetDeviceId = targetDeviceId;
    }
}


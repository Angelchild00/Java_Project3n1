package com.team3n1.smarthome.infrastructure.persistence;

import com.team3n1.smarthome.core.model.Rule;
import com.team3n1.smarthome.core.model.RuleState;
import com.team3n1.smarthome.core.model.SmartDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory storage for Rules and SmartDevices.
 *
 * Satisfies REQ-2.3: persistence with abstraction boundary.
 * Data lives in Maps for the lifetime of the process; no file I/O is needed
 * for the MVP scope the professor defined.
 *
 * Insertion order is preserved (LinkedHashMap) so that rules are always
 * evaluated in the order they were saved — the simplified deterministic
 * ordering agreed in the reduced scope.
 *
 * @author Team 3n1
 * @version MVP
 */
public class InMemoryRepository {

    // --- storage ---

    private final Map<String, Rule> rules = new LinkedHashMap<>();
    private final Map<String, SmartDevice> devices = new LinkedHashMap<>();

      /**
     * Save a rule. Overwrites any existing rule with the same ID.
     *
     * @param rule the rule to save (must not be null, ruleID must not be null)
     * @throws IllegalArgumentException if rule or ruleID is null
     */
    public void saveRule(Rule rule) {
        if (rule == null || rule.getRuleID() == null) {
            throw new IllegalArgumentException("Rule and ruleID must not be null");
        }
        rules.put(rule.getRuleID(), rule);
    }

    /**
     * Retrieve a rule by its ID.
     *
     * @param ruleId the rule identifier
     * @return the Rule, or null if not found
     */
    public Rule findRuleById(String ruleId) {
        return rules.get(ruleId);
    }

    /**
     * Return all saved rules in insertion order.
     *
     * @return unmodifiable list of all rules
     */
    public List<Rule> findAllRules() {
        return Collections.unmodifiableList(new ArrayList<>(rules.values()));
    }

    /**
     * REQ-2.4 query: return all rules whose current state matches the given state.
     * Example usage: findRulesByState(RuleState.ACTIVE)
     *
     * @param state the state to filter by
     * @return list of matching rules (may be empty, never null)
     */
    public List<Rule> findRulesByState(RuleState state) {
        List<Rule> result = new ArrayList<>();
        for (Rule rule : rules.values()) {
            if (rule.getCurrentState() == state) {
                result.add(rule);
            }
        }
        return result;
    }

    /**
     * REQ-2.4 query: return all rules that listen for a specific event type.
     * Example usage: findRulesByEventType("motion_detected")
     *
     * @param eventType the trigger event type to search for
     * @return list of matching rules (may be empty, never null)
     */
    public List<Rule> findRulesByEventType(String eventType) {
        List<Rule> result = new ArrayList<>();
        for (Rule rule : rules.values()) {
            if (eventType != null && eventType.equals(rule.getTriggerEventType())) {
                result.add(rule);
            }
        }
        return result;
    }

    /**
     * Delete a rule by ID.
     *
     * @param ruleId the rule identifier
     * @return true if a rule was removed, false if no rule had that ID
     */
    public boolean deleteRule(String ruleId) {
        return rules.remove(ruleId) != null;
    }

    /** @return the number of rules currently stored */
    public int getRuleCount() {
        return rules.size();
    }

    /**
     * Save a device. Overwrites any existing device with the same ID.
     *
     * @param device the device to save (must not be null, deviceId must not be null)
     * @throws IllegalArgumentException if device or deviceId is null
     */
    public void saveDevice(SmartDevice device) {
        if (device == null || device.getDeviceId() == null) {
            throw new IllegalArgumentException("Device and deviceId must not be null");
        }
        devices.put(device.getDeviceId(), device);
    }

    /**
     * Retrieve a device by its ID.
     *
     * @param deviceId the device identifier
     * @return the SmartDevice, or null if not found
     */
    public SmartDevice findDeviceById(String deviceId) {
        return devices.get(deviceId);
    }

    /**
     * Return all saved devices in insertion order.
     *
     * @return unmodifiable list of all devices
     */
    public List<SmartDevice> findAllDevices() {
        return Collections.unmodifiableList(new ArrayList<>(devices.values()));
    }

    /**
     * Delete a device by ID.
     *
     * @param deviceId the device identifier
     * @return true if a device was removed, false if no device had that ID
     */
    public boolean deleteDevice(String deviceId) {
        return devices.remove(deviceId) != null;
    }

    /** @return the number of devices currently stored */
    public int getDeviceCount() {
        return devices.size();
    }

 
    /**
     * Clear all rules and devices — useful for resetting state between demo runs
     * or test cases.
     */
    public void clear() {
        rules.clear();
        devices.clear();
    }

    @Override
    public String toString() {
        return "InMemoryRepository{rules=" + rules.size() + ", devices=" + devices.size() + "}";
    }
}

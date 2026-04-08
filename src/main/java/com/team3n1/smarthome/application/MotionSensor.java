package com.team3n1.smarthome.application;

import com.team3n1.smarthome.core.model.DeviceEventListener;
import com.team3n1.smarthome.core.model.Event;
import com.team3n1.smarthome.core.model.SmartDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * MotionSensor is the Subject in the Observer pattern.
 *
 * When its state is set to "MOTION_DETECTED" it automatically builds
 * an Event and notifies all registered DeviceEventListeners (observers).
 * The RulesEngine registers itself as a listener so that motion events
 * are routed through the engine without the sensor needing to know
 * anything about rules.
 *
 * @author Team 3n1
 * @version MVP
 */
public class MotionSensor implements SmartDevice {

    private String id;
    private String status = "NO_MOTION";

    // Observer pattern: list of listeners to notify when motion is detected
    private final List<DeviceEventListener> listeners = new ArrayList<>();

    // Counter used to generate unique event IDs
    private int eventCounter = 0;

    public MotionSensor(String id) {
        this.id = id;
    }

    // -------------------------------------------------------------------------
    // Observer pattern methods
    // -------------------------------------------------------------------------

    /**
     * Register an observer. Typically called once with the RulesEngine.
     *
     * @param listener the observer to add
     */
    public void addListener(DeviceEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a previously registered observer.
     *
     * @param listener the observer to remove
     */
    public void removeListener(DeviceEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all registered observers with the given event.
     * Called internally when motion is detected.
     */
    private void notifyListeners(Event event) {
        for (DeviceEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    // -------------------------------------------------------------------------
    // SmartDevice implementation
    // -------------------------------------------------------------------------

    @Override
    public String getDeviceId() {
        return id;
    }

    @Override
    public String getType() {
        return "SENSOR";
    }

    @Override
    public String getState() {
        return status;
    }

    /**
     * Update the sensor state. When state is set to "MOTION_DETECTED" a
     * motion_detected Event is created and all registered listeners are
     * notified automatically.
     *
     * @param state new state (e.g., "MOTION_DETECTED", "NO_MOTION")
     */
    @Override
    public void setState(String state) {
        this.status = state;
        System.out.println("[DEVICE-UPDATE] Sensor " + id + " state changed to: " + state);

        if ("MOTION_DETECTED".equals(state)) {
            eventCounter++;
            String eventId = id + "_evt_" + eventCounter;
            Event event = new Event(eventId, "motion_detected", id, System.currentTimeMillis());
            notifyListeners(event);
        }
    }
}

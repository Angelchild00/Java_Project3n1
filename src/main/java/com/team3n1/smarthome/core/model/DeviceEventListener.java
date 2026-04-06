package com.team3n1.smarthome.core.model;

/**
 * Observer interface for the Observer design pattern.
 *
 * Subject:  MotionSensor (holds the list of listeners, fires events)
 * Observer: RulesEngine  (receives the event and processes it)
 *
 * When a MotionSensor detects motion it creates an Event and calls
 * onEvent() on every registered listener — the RulesEngine is the
 * primary listener in this system.
 *
 * Trade-off (per Phase 1 design plan):
 *   The sensor does not need to know how rules work; it only knows
 *   about this interface. This decouples event production from rule
 *   evaluation, making it easy to add new listeners (e.g., a logger
 *   or a notification service) without touching the sensor or engine.
 *   The downside is that the notification chain is implicit, which
 *   can make call-flow harder to trace during debugging.
 *
 * @author Team 3n1
 * @version MVP
 */
public interface DeviceEventListener {

    /**
     * Called by a device (Subject) when it produces an event.
     *
     * @param event the event that just occurred
     */
    void onEvent(Event event);
}

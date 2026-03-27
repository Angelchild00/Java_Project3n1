package com.team3n1.smarthome.core.model;

/**
 * Interface for virtual devices
 * Supports req 2.7 and 2.2
 */

public interface SmartDevice {
    String getDeviceId();
    String getType();
    String getState();
    void setState(String state);
}
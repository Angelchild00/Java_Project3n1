package com.team3n1.smarthome.application;
import com.team3n1.smarthome.core.model.SmartDevice;

public class MotionSensor implements SmartDevice {
    private String id;
    private String status = "NO_MOTION";

    public MotionSensor(String id) {
        this.id = id;
    }

    @Override public String getDeviceId() { 
        return id; 
    }
    @Override public String getType() {
        return "SENSOR"; 
    }

    @Override public String getState() { 
        return status;
    }

    @Override
    public void setState(String state) {
        this.status = state;
        System.out.println("[DEVICE-UPDATE] Sensor " + id + " detected: " + state);
    }
}

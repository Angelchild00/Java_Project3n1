package com.team3n1.smarthome.application;
import com.team3n1.smarthome.core.model.SmartDevice;

public class LightDevice implements SmartDevice {
    private String id;
    private String status = "OFF"; // default status

    public LightDevice(String id) {
        this.id = id;
    }

    @Override public String getDeviceId() {
        return id;
    }
    @Override public String getType() {
        return "Light";
    }
    @Override public String getState() {
        return status;
    }

    @Override public void setState(String state){
        this.status = state;
        System.out.println("[DEVICE-UPDATE] " + id + " is now " + status);
    }

}

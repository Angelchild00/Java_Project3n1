package com.team3n1.smarthome.application;
import com.team3n1.smarthome.core.model.SmartDevice;

public class DeviceFactory {
    /** Creates a virtual device based on type
     * Req 2.7 (Creational Pattern: Factory)
     */

    public static SmartDevice createDevice(String type, String id){
        if (type.equalsIgnoreCase("LIGHT")){
            return new LightDevice(id);
        } else if (type.equalsIgnoreCase("SENSOR")){
            return new MotionSensor(id);
        } else {
            throw new IllegalArgumentException("Unsupported device type: " + type);     
        }
    }
}

package com.team3n1.smarthome.core.actions;

import com.team3n1.smarthome.core.model.SmartDevice;

public class TurnOffAction implements Action{
    @Override
    public void execute(SmartDevice device) {
        device.setState("OFF");
    }

    @Override
    public String getDescription() {
        return "Turn Device Off";
    }
    
}

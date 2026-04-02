package com.team3n1.smarthome.core.actions;

import com.team3n1.smarthome.core.model.SmartDevice;

public class TurnOnAction implements Action{
    @Override
    public void execute(SmartDevice device) {
        device.setState("ON");
    }

    @Override
    public String getDescription() {
        return "Turn Device On";
    }
    
}

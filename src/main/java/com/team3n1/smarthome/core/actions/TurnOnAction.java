package com.team3n1.smarthome.core.actions;

public class TurnOnAction implements Action{
    @Override
    public void execute(SmartDevice device) {
        device.setState(isOn -> true);
    }

    @Override
    public String getDescription() {
        return "Turn Device On";
    }
    
}

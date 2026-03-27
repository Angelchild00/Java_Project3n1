package com.team3n1.smarthome.core.actions;
import com.team3n1.smarthome.core.model.SmartDevice;

    public interface Action{
        void execute(SmartDevice device); //execute the action on the given device
        String getDescription();
    }

package com.team3n1.smarthome.core.actions;

public class Action {
    public interface Action{
        void execute(SmartDevice device);
        String getDescription();
    }
}

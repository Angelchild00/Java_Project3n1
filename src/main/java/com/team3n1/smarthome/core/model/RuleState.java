package com.team3n1.smarthome.core.model;

public enum RuleState {
    DRAFT, //intial state
    ACTIVE, // rule is live and monitoring conditions
    TRIGGERED, // rule is currently executing actions
    DISABLED // rule is turned off and will not trigger
}
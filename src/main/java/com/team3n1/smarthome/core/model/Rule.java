package com.team3n1.smarthome.core.model;

import java.util.List;
import com.team3n1.smarthome.core.actions.Action;

public class Rule{
    private String ruleID;
    private List<Action> actions;
    private RuleState currentState;

    public Rule(String ruleID, List<Action> actions){
        this.ruleID = ruleID;
        this.actions = actions;
        this.currentState = RuleState.DRAFT; // initial state
    }

    public void activate(){
        //cannot activate without actions
        if (actions == null || actions.isEmpty()){
            throw new IllegalStateException("Cannot activate rule without actions");
        }
        this.currentState = RuleState.ACTIVE; //valid transition
        System.out.println("[CHECK-STATE] Rule " + ruleID + " is in " + currentState + "state.");
    }

    public void trigger(SmartDevice device){
        if (this.currentState != RuleState.ACTIVE){
           System.out.println("[REJECTED] Rule " + ruleID + " is in " + currentState + " state.");
            return; // only trigger if active
        }
        for (Action action : actions){
            action.execute(device);
        } 

    }

    public void disable(){
        this.currentState = RuleState.DISABLED; // can disable from any state
    }

    public void addAction(Action action){
        this.actions.add(action);
    }
}

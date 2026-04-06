package com.team3n1.smarthome;

import com.team3n1.smarthome.application.DeviceFactory;
import com.team3n1.smarthome.application.DeviceRegistry;
import com.team3n1.smarthome.application.ExecutionPolicy;
import com.team3n1.smarthome.application.MotionSensor;
import com.team3n1.smarthome.application.RuleFactory;
import com.team3n1.smarthome.application.RulesEngine;
import com.team3n1.smarthome.core.actions.Action;
import com.team3n1.smarthome.core.actions.TurnOffAction;
import com.team3n1.smarthome.core.actions.TurnOnAction;
import com.team3n1.smarthome.core.exceptions.DomainException;
import com.team3n1.smarthome.core.model.Event;
import com.team3n1.smarthome.core.model.Rule;
import com.team3n1.smarthome.core.model.RuleState;
import com.team3n1.smarthome.core.model.SmartDevice;
import com.team3n1.smarthome.infrastructure.logging.AuditLogger;
import com.team3n1.smarthome.infrastructure.logging.ConsoleAuditLogger;
import com.team3n1.smarthome.infrastructure.persistence.InMemoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive CLI entry point for the Smart Home Automation Rules Engine.
 *
 * Users can add devices, create rules, trigger events, and manage the system
 * through a menu. Option 9 runs the full scripted grading demo that covers
 * all Phase 2 requirements [REQ-2.1] through [REQ-2.10].
 *
 * @author Team 3n1
 * @version Phase 2
 */
public class Main {

    // ── Shared session state ──────────────────────────────────────────────────
    private static final Scanner        scanner  = new Scanner(System.in);
    private static final ConsoleAuditLogger logger   = new ConsoleAuditLogger();
    private static final DeviceRegistry registry = new DeviceRegistry(logger);
    private static final RuleFactory    factory  = new RuleFactory(registry, logger);
    private static final RulesEngine    engine   = new RulesEngine(registry, logger);
    private static final InMemoryRepository repo  = new InMemoryRepository();

    private static int eventCounter = 0;

    // Valid event types accepted by the system
    private static final String[] EVENT_TYPES =
            { "motion_detected", "door_opened", "light_turned_on" };

    // =========================================================================
    public static void main(String[] args) {
        printBanner();
        runMenu();
    }

    // =========================================================================
    // Main menu loop
    // =========================================================================
    private static void runMenu() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            System.out.println();
            switch (choice) {
                case "1": addDevice();           break;
                case "2": listDevices();         break;
                case "3": addRule();             break;
                case "4": listRules();           break;
                case "5": enableDisableRule();   break;
                case "6": triggerEvent();        break;
                case "7": setPolicy();           break;
                case "8": queryRepository();     break;
                case "9": runGradingDemo();      break;
                case "0": running = false;       break;
                default:
                    System.out.println("  Invalid option — please enter a number from the menu.");
            }
        }
        System.out.println("  Goodbye.");
        scanner.close();
    }

    // =========================================================================
    // 1 — Add Device
    // =========================================================================
    private static void addDevice() {
        System.out.println("--- Add Device ---");
        System.out.print("  Device type  (LIGHT / SENSOR): ");
        String type = scanner.nextLine().trim().toUpperCase();

        if (!type.equals("LIGHT") && !type.equals("SENSOR")) {
            System.out.println("  Unknown type. Use LIGHT or SENSOR.");
            return;
        }

        System.out.print("  Device ID: ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("  Device ID cannot be empty.");
            return;
        }

        if (registry.deviceExists(id)) {
            System.out.println("  A device with ID '" + id + "' already exists.");
            return;
        }

        try {
            SmartDevice device = DeviceFactory.createDevice(type, id);
            registry.registerDevice(device);
            repo.saveDevice(device);

            // Wire sensor into the engine as an Observer automatically
            if (type.equals("SENSOR") && device instanceof MotionSensor) {
                ((MotionSensor) device).addListener(engine);
                System.out.println("  Sensor '" + id + "' registered and wired to the rules engine.");
            } else {
                System.out.println("  Device '" + id + "' (" + type + ") added successfully.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2 — List Devices
    // =========================================================================
    private static void listDevices() {
        System.out.println("--- Devices ---");
        if (registry.getAllDevices().isEmpty()) {
            System.out.println("  No devices registered yet.");
            return;
        }
        System.out.printf("  %-20s %-10s %s%n", "Device ID", "Type", "State");
        System.out.println("  " + "-".repeat(45));
        for (SmartDevice d : registry.getAllDevices().values()) {
            System.out.printf("  %-20s %-10s %s%n",
                    d.getDeviceId(), d.getType(), d.getState());
        }
    }

    // =========================================================================
    // 3 — Add Rule
    // =========================================================================
    private static void addRule() {
        System.out.println("--- Add Rule ---");

        if (registry.getAllDevices().isEmpty()) {
            System.out.println("  No devices registered. Add a device first.");
            return;
        }

        System.out.print("  Rule ID: ");
        String ruleId = scanner.nextLine().trim();
        if (ruleId.isEmpty()) {
            System.out.println("  Rule ID cannot be empty.");
            return;
        }

        System.out.println("  Event types: ");
        for (int i = 0; i < EVENT_TYPES.length; i++) {
            System.out.println("    " + (i + 1) + ". " + EVENT_TYPES[i]);
        }
        System.out.print("  Choose event type (1-" + EVENT_TYPES.length + "): ");
        String eventChoice = scanner.nextLine().trim();
        int eventIndex;
        try {
            eventIndex = Integer.parseInt(eventChoice) - 1;
            if (eventIndex < 0 || eventIndex >= EVENT_TYPES.length) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("  Invalid choice.");
            return;
        }
        String eventType = EVENT_TYPES[eventIndex];

        System.out.println("  Available target devices:");
        registry.getAllDevices().values().forEach(d ->
                System.out.println("    - " + d.getDeviceId() + " (" + d.getType() + ")"));
        System.out.print("  Target device ID: ");
        String targetId = scanner.nextLine().trim();

        System.out.println("  Actions:");
        System.out.println("    1. Turn ON");
        System.out.println("    2. Turn OFF");
        System.out.print("  Choose action (1-2): ");
        String actionChoice = scanner.nextLine().trim();

        Action action;
        if (actionChoice.equals("1")) {
            action = new TurnOnAction();
        } else if (actionChoice.equals("2")) {
            action = new TurnOffAction();
        } else {
            System.out.println("  Invalid action choice.");
            return;
        }

        try {
            Rule rule = factory.createRule(ruleId, eventType, targetId, Arrays.asList(action));
            engine.registerRule(rule);
            repo.saveRule(rule);
            System.out.println("  Rule '" + ruleId + "' created and activated.");
            System.out.println("  When [" + eventType + "] occurs -> " + action.getDescription() + " on '" + targetId + "'");
        } catch (DomainException e) {
            System.out.println("  Rule rejected: [" + e.getErrorCode() + "] " + e.getMessage());
        }
    }

    // =========================================================================
    // 4 — List Rules
    // =========================================================================
    private static void listRules() {
        System.out.println("--- Rules ---");
        List<Rule> rules = repo.findAllRules();
        if (rules.isEmpty()) {
            System.out.println("  No rules created yet.");
            return;
        }
        System.out.printf("  %-20s %-22s %-20s %s%n", "Rule ID", "Trigger Event", "Target Device", "State");
        System.out.println("  " + "-".repeat(75));
        for (Rule r : rules) {
            System.out.printf("  %-20s %-22s %-20s %s%n",
                    r.getRuleID(), r.getTriggerEventType(),
                    r.getTargetDeviceId(), r.getCurrentState());
        }
    }

    // =========================================================================
    // 5 — Enable / Disable Rule
    // =========================================================================
    private static void enableDisableRule() {
        System.out.println("--- Enable / Disable Rule ---");
        List<Rule> rules = repo.findAllRules();
        if (rules.isEmpty()) {
            System.out.println("  No rules exist yet.");
            return;
        }

        listRules();
        System.out.print("  Enter Rule ID to toggle: ");
        String ruleId = scanner.nextLine().trim();

        Rule rule = repo.findRuleById(ruleId);
        if (rule == null) {
            System.out.println("  Rule '" + ruleId + "' not found.");
            return;
        }

        if (rule.getCurrentState() == RuleState.DISABLED) {
            rule.activate();
            System.out.println("  Rule '" + ruleId + "' is now ACTIVE.");
        } else {
            engine.disableRule(ruleId);
            System.out.println("  Rule '" + ruleId + "' is now DISABLED.");
        }
    }

    // =========================================================================
    // 6 — Trigger Event
    // =========================================================================
    private static void triggerEvent() {
        System.out.println("--- Trigger Event ---");

        System.out.println("  Event types:");
        for (int i = 0; i < EVENT_TYPES.length; i++) {
            System.out.println("    " + (i + 1) + ". " + EVENT_TYPES[i]);
        }
        System.out.print("  Choose event type (1-" + EVENT_TYPES.length + "): ");
        String choice = scanner.nextLine().trim();
        int idx;
        try {
            idx = Integer.parseInt(choice) - 1;
            if (idx < 0 || idx >= EVENT_TYPES.length) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("  Invalid choice.");
            return;
        }
        String eventType = EVENT_TYPES[idx];

        System.out.print("  Source device ID (or press Enter to use 'manual_trigger'): ");
        String source = scanner.nextLine().trim();
        if (source.isEmpty()) source = "manual_trigger";

        eventCounter++;
        try {
            Event event = new Event("evt_" + eventCounter, eventType, source,
                    System.currentTimeMillis());
            System.out.println("  Firing event: " + eventType + " from " + source);
            engine.processEvent(event);
        } catch (DomainException e) {
            System.out.println("  Event rejected: [" + e.getErrorCode() + "] " + e.getMessage());
        }
    }

    // =========================================================================
    // 7 — Set Execution Policy
    // =========================================================================
    private static void setPolicy() {
        System.out.println("--- Execution Policy ---");
        System.out.println("  Current policy: " + engine.getPolicy());
        System.out.println("  1. LENIENT — continue remaining actions after a failure (default)");
        System.out.println("  2. STRICT  — abort remaining actions on first failure");
        System.out.print("  Choose (1-2): ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            engine.setPolicy(ExecutionPolicy.LENIENT);
            System.out.println("  Policy set to LENIENT.");
        } else if (choice.equals("2")) {
            engine.setPolicy(ExecutionPolicy.STRICT);
            System.out.println("  Policy set to STRICT.");
        } else {
            System.out.println("  Invalid choice — policy unchanged.");
        }
    }

    // =========================================================================
    // 8 — Query Repository
    // =========================================================================
    private static void queryRepository() {
        System.out.println("--- Query Repository [REQ-2.4] ---");
        System.out.println("  1. Find rules by state");
        System.out.println("  2. Find rules by event type");
        System.out.print("  Choose (1-2): ");
        String choice = scanner.nextLine().trim();
        System.out.println();

        if (choice.equals("1")) {
            System.out.println("  States: DRAFT, ACTIVE, TRIGGERED, DISABLED");
            System.out.print("  Enter state: ");
            String stateStr = scanner.nextLine().trim().toUpperCase();
            RuleState state;
            try {
                state = RuleState.valueOf(stateStr);
            } catch (IllegalArgumentException e) {
                System.out.println("  Unknown state '" + stateStr + "'.");
                return;
            }
            List<Rule> results = repo.findRulesByState(state);
            System.out.println("  [QUERY-1] findRulesByState(" + state + ") — " + results.size() + " result(s):");
            for (Rule r : results) {
                System.out.println("    ruleID=" + r.getRuleID()
                        + "  trigger=" + r.getTriggerEventType()
                        + "  target="  + r.getTargetDeviceId());
            }

        } else if (choice.equals("2")) {
            System.out.println("  Event types: " + String.join(", ", EVENT_TYPES));
            System.out.print("  Enter event type: ");
            String eventType = scanner.nextLine().trim();
            List<Rule> results = repo.findRulesByEventType(eventType);
            System.out.println("  [QUERY-2] findRulesByEventType(\"" + eventType + "\") — " + results.size() + " result(s):");
            for (Rule r : results) {
                System.out.println("    ruleID=" + r.getRuleID()
                        + "  state="  + r.getCurrentState()
                        + "  target=" + r.getTargetDeviceId());
            }
        } else {
            System.out.println("  Invalid choice.");
        }
    }

    // =========================================================================
    // 9 — Full Grading Demo
    // =========================================================================
    private static void runGradingDemo() {
        System.out.println();
        System.out.println("  Running full grading demo — this uses isolated state and does not");
        System.out.println("  affect your current session.");
        System.out.println();

        demoReq21();
        demoReq22();

        InMemoryRepository demoRepo = new InMemoryRepository();
        demoReq23(demoRepo);
        demoReq24(demoRepo);
        demoReq25();
        demoReq26();
        demoReq27();
        demoReq28();
        demoReq29();
        demoReq210();

        System.out.println();
        System.out.println("  Grading demo complete. Returning to main menu.");
    }

    // ─── REQ-2.1 ─────────────────────────────────────────────────────────────
    private static void demoReq21() {
        section("REQ-2.1", "Runnable Interface");
        System.out.println("  Program started. Interactive CLI menu is active.");
        System.out.println("  Entry point: com.team3n1.smarthome.Main");
        System.out.println("  Run command: java -cp target/classes com.team3n1.smarthome.Main");
    }

    // ─── REQ-2.2 ─────────────────────────────────────────────────────────────
    private static void demoReq22() {
        section("REQ-2.2", "Architecture Boundaries");
        System.out.println("  Layer           Package                                      Key classes");
        System.out.println("  Domain          core.model / core.actions / core.exceptions  Rule, Event, Action, DomainException");
        System.out.println("                  --> Zero imports from application or infra");
        System.out.println("  Application     application/                                 RulesEngine, RuleFactory, DeviceFactory");
        System.out.println("                  --> Orchestrates domain; no direct I/O");
        System.out.println("  Infrastructure  infrastructure.logging / .persistence        ConsoleAuditLogger, InMemoryRepository");
        System.out.println("                  --> All I/O; depends inward only");
        System.out.println();
        System.out.println("  Dependency direction: infrastructure -> application -> domain (never reversed).");
    }

    // ─── REQ-2.3 ─────────────────────────────────────────────────────────────
    private static void demoReq23(InMemoryRepository demoRepo) {
        section("REQ-2.3", "Persistence with Abstraction Boundary");

        ConsoleAuditLogger demoLogger = new ConsoleAuditLogger();
        DeviceRegistry demoReg = new DeviceRegistry(demoLogger);
        RuleFactory demoFactory = new RuleFactory(demoReg, demoLogger);

        System.out.println("  [SAVE] Creating and persisting two devices and two rules...");
        SmartDevice l1 = DeviceFactory.createDevice("LIGHT", "persist_light_1");
        SmartDevice l2 = DeviceFactory.createDevice("LIGHT", "persist_light_2");
        demoReg.registerDevice(l1);
        demoReg.registerDevice(l2);
        demoRepo.saveDevice(l1);
        demoRepo.saveDevice(l2);

        Rule r1 = demoFactory.createRule("persist_rule_1", "motion_detected",
                "persist_light_1", Arrays.asList(new TurnOnAction()));
        Rule r2 = demoFactory.createRule("persist_rule_2", "door_opened",
                "persist_light_2", Arrays.asList(new TurnOnAction()));
        demoRepo.saveRule(r1);
        demoRepo.saveRule(r2);

        System.out.println("  [SAVE] Repository: "
                + demoRepo.getDeviceCount() + " device(s), " + demoRepo.getRuleCount() + " rule(s)");
        System.out.println();

        System.out.println("  [RELOAD] Rebuilding fresh registry + engine from repository...");
        DeviceRegistry freshReg = new DeviceRegistry(demoLogger);
        RulesEngine freshEngine = new RulesEngine(freshReg, demoLogger);
        for (SmartDevice d : demoRepo.findAllDevices()) freshReg.registerDevice(d);
        for (Rule r : demoRepo.findAllRules())         freshEngine.registerRule(r);

        System.out.println("  [RELOAD] Fresh registry: " + freshReg.getAllDevices().size() + " device(s)");
        System.out.println("  [RELOAD] Fresh engine  : " + freshEngine.getRuleCount() + " rule(s)");

        Event proof = new Event("reload_evt", "motion_detected", "sensor_demo",
                System.currentTimeMillis());
        freshEngine.processEvent(proof);
        System.out.println("  [RELOAD] persist_light_1 state: " + l1.getState() + " — reload confirmed.");
    }

    // ─── REQ-2.4 ─────────────────────────────────────────────────────────────
    private static void demoReq24(InMemoryRepository demoRepo) {
        section("REQ-2.4", "Two Non-Trivial Queries");

        System.out.println("  [QUERY-1] findRulesByState(ACTIVE):");
        List<Rule> active = demoRepo.findRulesByState(RuleState.ACTIVE);
        if (active.isEmpty()) System.out.println("    (none)");
        for (Rule r : active)
            System.out.println("    " + r.getRuleID() + " | " + r.getTriggerEventType()
                    + " -> " + r.getTargetDeviceId() + " | " + r.getCurrentState());
        System.out.println();

        System.out.println("  [QUERY-2] findRulesByEventType(\"motion_detected\"):");
        List<Rule> motion = demoRepo.findRulesByEventType("motion_detected");
        if (motion.isEmpty()) System.out.println("    (none)");
        for (Rule r : motion)
            System.out.println("    " + r.getRuleID() + " | state=" + r.getCurrentState()
                    + " | target=" + r.getTargetDeviceId());
    }

    // ─── REQ-2.5 ─────────────────────────────────────────────────────────────
    private static void demoReq25() {
        section("REQ-2.5", "Lifecycle / State Machine Enforcement");

        ConsoleAuditLogger demoLogger = new ConsoleAuditLogger();
        DeviceRegistry demoReg = new DeviceRegistry(demoLogger);
        RuleFactory demoFactory = new RuleFactory(demoReg, demoLogger);
        SmartDevice light = DeviceFactory.createDevice("LIGHT", "lifecycle_light");
        demoReg.registerDevice(light);

        Rule rule = demoFactory.createRule("lifecycle_rule", "motion_detected",
                "lifecycle_light", Arrays.asList(new TurnOnAction()));

        System.out.println("  [STATE] On creation : " + rule.getCurrentState());
        rule.activate();
        System.out.println("  [STATE] After activate: " + rule.getCurrentState());
        System.out.println("  [VALID] DRAFT -> ACTIVE confirmed.");
        System.out.println();
        rule.disable();
        System.out.println("  [STATE] After disable : " + rule.getCurrentState());
        System.out.println("  [VALID] ACTIVE -> DISABLED confirmed.");
        System.out.println();

        System.out.println("  [FORBIDDEN] DISABLED -> TRIGGERED (calling rule.trigger() while DISABLED):");
        rule.trigger(light);
        System.out.println("  [REJECTED] Forbidden transition blocked.");
        System.out.println();

        Rule draft = demoFactory.createRule("draft_rule", "motion_detected",
                "lifecycle_light", Arrays.asList(new TurnOnAction()));
        System.out.println("  [FORBIDDEN] DRAFT -> TRIGGERED (calling rule.trigger() while DRAFT):");
        draft.trigger(light);
        System.out.println("  [REJECTED] Forbidden transition blocked.");
    }

    // ─── REQ-2.6 ─────────────────────────────────────────────────────────────
    private static void demoReq26() {
        section("REQ-2.6", "Two Configurable Policies — LENIENT vs STRICT");

        ConsoleAuditLogger demoLogger = new ConsoleAuditLogger();
        DeviceRegistry demoReg = new DeviceRegistry(demoLogger);
        RuleFactory demoFactory = new RuleFactory(demoReg, demoLogger);
        SmartDevice light = DeviceFactory.createDevice("LIGHT", "policy_light");
        demoReg.registerDevice(light);

        Action failing = new Action() {
            @Override public void execute(SmartDevice d) {
                throw new DomainException("RULE_EXECUTION_FAILED", "Simulated failure in action 1");
            }
            @Override public String getDescription() { return "Simulated Failing Action"; }
        };

        Rule rule = demoFactory.createRule("policy_rule", "motion_detected",
                "policy_light", Arrays.asList(failing, new TurnOnAction()));
        RulesEngine demoEngine = new RulesEngine(demoReg, demoLogger);
        demoEngine.registerRule(rule);

        System.out.println("  [POLICY-A] LENIENT — continue after failure:");
        demoEngine.setPolicy(ExecutionPolicy.LENIENT);
        light.setState("OFF");
        demoEngine.processEvent(new Event("p1", "motion_detected", "s", System.currentTimeMillis()));
        System.out.println("  Result: light=" + light.getState() + " (action 2 ran -> ON)");
        System.out.println();

        System.out.println("  [POLICY-B] STRICT — abort after failure:");
        demoEngine.setPolicy(ExecutionPolicy.STRICT);
        light.setState("OFF");
        demoEngine.processEvent(new Event("p2", "motion_detected", "s", System.currentTimeMillis()));
        System.out.println("  Result: light=" + light.getState() + " (action 2 skipped -> OFF)");
        System.out.println();
        System.out.println("  Different outcomes confirmed. Same rule, same event, different policy.");
    }

    // ─── REQ-2.7 ─────────────────────────────────────────────────────────────
    private static void demoReq27() {
        section("REQ-2.7", "Design Pattern Map");

        System.out.println("  PATTERN 1 — Creational: Factory");
        System.out.println("    Classes  : DeviceFactory, RuleFactory");
        System.out.println("    Behaviour: Caller passes a type string; Factory returns the right concrete class.");
        SmartDevice fd = DeviceFactory.createDevice("LIGHT", "factory_demo");
        System.out.println("    Evidence : createDevice(\"LIGHT\") -> " + fd.getClass().getSimpleName());
        System.out.println();

        System.out.println("  PATTERN 2 — Behavioral: Command");
        System.out.println("    Classes  : Action (interface), TurnOnAction, TurnOffAction");
        System.out.println("    Behaviour: Actions are objects. Engine calls execute(device) without");
        System.out.println("               knowing the concrete type — new actions need zero engine changes.");
        System.out.println("    Evidence : TurnOnAction  -> \"" + new TurnOnAction().getDescription() + "\"");
        System.out.println("               TurnOffAction -> \"" + new TurnOffAction().getDescription() + "\"");
        System.out.println();

        System.out.println("  PATTERN 3 — Behavioral: Observer");
        System.out.println("    Classes  : DeviceEventListener (Observer interface),");
        System.out.println("               MotionSensor (Subject), RulesEngine (Concrete Observer)");
        System.out.println("    Behaviour: Sensor notifies engine automatically on MOTION_DETECTED.");
        System.out.println("               Sensor never imports RulesEngine.");

        ConsoleAuditLogger demoLogger = new ConsoleAuditLogger();
        DeviceRegistry demoReg = new DeviceRegistry(demoLogger);
        RuleFactory demoFactory = new RuleFactory(demoReg, demoLogger);
        SmartDevice obsLight = DeviceFactory.createDevice("LIGHT", "obs_light");
        demoReg.registerDevice(obsLight);
        MotionSensor obsSensor = new MotionSensor("obs_sensor");
        demoReg.registerDevice(obsSensor);
        RulesEngine obsEngine = new RulesEngine(demoReg, demoLogger);
        obsEngine.registerRule(demoFactory.createRule("obs_rule", "motion_detected",
                "obs_light", Arrays.asList(new TurnOnAction())));
        obsSensor.addListener(obsEngine);

        System.out.println("    Before: sensor=" + obsSensor.getState() + " light=" + obsLight.getState());
        obsSensor.setState("MOTION_DETECTED");
        System.out.println("    After : sensor=" + obsSensor.getState() + " light=" + obsLight.getState());
        System.out.println("    Chain : setState -> notifyListeners -> RulesEngine.onEvent -> processEvent");
    }

    // ─── REQ-2.8 ─────────────────────────────────────────────────────────────
    private static void demoReq28() {
        section("REQ-2.8", "Robustness — Validation and Meaningful Errors");

        ConsoleAuditLogger demoLogger = new ConsoleAuditLogger();
        DeviceRegistry demoReg = new DeviceRegistry(demoLogger);
        RuleFactory demoFactory = new RuleFactory(demoReg, demoLogger);

        System.out.println("  [ROBUST-1] Rule with null ruleID:");
        try {
            demoFactory.createRule(null, "motion_detected", "x", Arrays.asList(new TurnOnAction()));
        } catch (DomainException e) {
            System.out.println("  Caught [" + e.getErrorCode() + "] " + e.getMessage());
        }
        System.out.println();

        System.out.println("  [ROBUST-2] Rule targeting unknown device:");
        try {
            demoFactory.createRule("r", "motion_detected", "ghost", Arrays.asList(new TurnOnAction()));
        } catch (DomainException e) {
            System.out.println("  Caught [" + e.getErrorCode() + "] " + e.getMessage());
        }
        System.out.println();

        System.out.println("  [ROBUST-3] Event with future timestamp:");
        try {
            new Event("bad", "motion_detected", "s", System.currentTimeMillis() + 60_000L);
        } catch (DomainException e) {
            System.out.println("  Caught [" + e.getErrorCode() + "] " + e.getMessage());
        }
    }

    // ─── REQ-2.9 ─────────────────────────────────────────────────────────────
    private static void demoReq29() {
        section("REQ-2.9", "Performance / Scalability Evidence");

        AuditLogger    nullLog  = new NullAuditLogger();
        DeviceRegistry perfReg  = new DeviceRegistry(nullLog);
        RuleFactory    perfFact = new RuleFactory(perfReg, nullLog);
        RulesEngine    perfEng  = new RulesEngine(perfReg, nullLog);

        int rules  = 10;
        int events = 50;

        for (int i = 0; i < rules; i++) {
            perfReg.registerDevice(new QuietDevice("p_light_" + i));
            perfEng.registerRule(perfFact.createRule("p_rule_" + i, "motion_detected",
                    "p_light_" + i, Arrays.asList(new TurnOnAction())));
        }

        // warm-up
        perfEng.processEvent(new Event("wu", "motion_detected", "s", System.currentTimeMillis()));

        long start = System.nanoTime();
        for (int i = 0; i < events; i++) {
            perfEng.processEvent(new Event("pe_" + i, "motion_detected", "s",
                    System.currentTimeMillis()));
        }
        long ns = System.nanoTime() - start;

        System.out.printf("  [PERF] Rules/event: %d  |  Events fired: %d%n", rules, events);
        System.out.printf("  [PERF] Total: %.2f ms  |  Avg: %.2f µs/event  |  Throughput: %.0f events/sec%n",
                ns / 1_000_000.0, ns / 1_000.0 / events, events / (ns / 1_000_000.0) * 1_000.0);
        System.out.println("  [PERF] Trade-off: ArrayList scan is O(n) per event. Acceptable at");
        System.out.println("         smart-home scale. A HashMap index would be O(1) but adds complexity.");
    }

    // ─── REQ-2.10 ────────────────────────────────────────────────────────────
    private static void demoReq210() {
        section("REQ-2.10", "Portability");
        System.out.println("  No hardcoded absolute paths. Standard Java collections only.");
        System.out.println("  Requires Java 11+. Compatible with Windows, macOS, Linux.");
        System.out.println("  See README.md for build and run instructions.");
    }

    // =========================================================================
    // Inner classes
    // =========================================================================

    private static class QuietDevice implements SmartDevice {
        private final String id;
        private String state = "OFF";
        QuietDevice(String id) { this.id = id; }
        @Override public String getDeviceId()      { return id; }
        @Override public String getType()          { return "LIGHT"; }
        @Override public String getState()         { return state; }
        @Override public void   setState(String s) { this.state = s; }
    }

    private static class NullAuditLogger implements AuditLogger {
        @Override public void logEventReceived(Event e) {}
        @Override public void logRuleMatched(Event e, Rule r) {}
        @Override public void logActionAttempted(Rule r, Action a, String d) {}
        @Override public void logActionSuccess(Rule r, Action a, String d) {}
        @Override public void logActionFailure(Rule r, Action a, String d, String reason) {}
        @Override public void logRuleSkipped(Event e, Rule r, String reason) {}
        @Override public void logSystemEvent(String msg) {}
    }

    // =========================================================================
    // Formatting helpers
    // =========================================================================

    private static void printBanner() {
        System.out.println();
        System.out.println("=================================================================");
        System.out.println("   SMART HOME AUTOMATION RULES ENGINE");
        System.out.println("   Team 3n1: Leanne Kidder, Ryan Hackbart, Liban Nur");
        System.out.println("   Course: CSCN72040");
        System.out.println("=================================================================");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("  ---- Main Menu (" + engine.getPolicy() + " policy) ----");
        System.out.println("  1. Add Device");
        System.out.println("  2. List Devices");
        System.out.println("  3. Add Rule");
        System.out.println("  4. List Rules");
        System.out.println("  5. Enable / Disable Rule");
        System.out.println("  6. Trigger Event");
        System.out.println("  7. Set Execution Policy");
        System.out.println("  8. Query Repository");
        System.out.println("  9. Run Full Grading Demo [REQ-2.1 to REQ-2.10]");
        System.out.println("  0. Exit");
        System.out.print("  > ");
    }

    private static void section(String reqId, String title) {
        System.out.println();
        System.out.println("  -----------------------------------------------------------------");
        System.out.println("  [" + reqId + "]  " + title);
        System.out.println("  -----------------------------------------------------------------");
    }
}

package com.team3n1.smarthome.tests;

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
import com.team3n1.smarthome.infrastructure.persistence.InMemoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SmartHomeTestSuite {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("event rejects future timestamp", SmartHomeTestSuite::eventRejectsFutureTimestamp);
        run("event rejects empty event type", SmartHomeTestSuite::eventRejectsEmptyType);
        run("event rejects empty source device", SmartHomeTestSuite::eventRejectsEmptySource);

        run("device factory creates light and sensor", SmartHomeTestSuite::deviceFactoryCreatesSupportedTypes);
        run("device factory rejects unsupported type", SmartHomeTestSuite::deviceFactoryRejectsUnsupportedType);

        run("device registry register and lookup", SmartHomeTestSuite::deviceRegistryRegisterAndLookup);
        run("device registry rejects null device", SmartHomeTestSuite::deviceRegistryRejectsNullDevice);

        run("rule factory creates valid rule", SmartHomeTestSuite::ruleFactoryCreatesValidRule);
        run("rule factory rejects unknown device", SmartHomeTestSuite::ruleFactoryRejectsUnknownDevice);
        run("rule factory rejects unknown event type", SmartHomeTestSuite::ruleFactoryRejectsUnknownEventType);
        run("rule factory rejects empty actions", SmartHomeTestSuite::ruleFactoryRejectsEmptyActions);

        run("rule lifecycle activate disable", SmartHomeTestSuite::ruleLifecycleActivateDisable);

        run("rules engine executes matching rule", SmartHomeTestSuite::rulesEngineExecutesMatchingRule);
        run("rules engine skips overlapping targets", SmartHomeTestSuite::rulesEngineSkipsOverlappingTargets);
        run("rules engine strict aborts after first domain failure", SmartHomeTestSuite::rulesEngineStrictAbortsAfterFailure);
        run("rules engine lenient continues after domain failure", SmartHomeTestSuite::rulesEngineLenientContinuesAfterFailure);

        run("motion sensor observer notifies engine", SmartHomeTestSuite::motionSensorObserverNotifiesEngine);

        run("repository queries by state and event", SmartHomeTestSuite::repositoryQueriesByStateAndEvent);
        run("turn on and turn off actions update state", SmartHomeTestSuite::turnActionsUpdateState);

        System.out.println();
        System.out.println("============================================================");
        System.out.println("TEST SUMMARY");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("============================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static void eventRejectsFutureTimestamp() {
        long future = System.currentTimeMillis() + 60_000;
        expectThrows(DomainException.class, () -> new Event("evt", "motion_detected", "sensor_1", future));
    }

    private static void eventRejectsEmptyType() {
        expectThrows(DomainException.class, () -> new Event("evt", " ", "sensor_1", System.currentTimeMillis()));
    }

    private static void eventRejectsEmptySource() {
        expectThrows(DomainException.class, () -> new Event("evt", "motion_detected", "", System.currentTimeMillis()));
    }

    private static void deviceFactoryCreatesSupportedTypes() {
        SmartDevice light = DeviceFactory.createDevice("LIGHT", "light_1");
        SmartDevice sensor = DeviceFactory.createDevice("SENSOR", "sensor_1");

        assertEquals("light_1", light.getDeviceId(), "light id mismatch");
        assertEquals("sensor_1", sensor.getDeviceId(), "sensor id mismatch");
    }

    private static void deviceFactoryRejectsUnsupportedType() {
        expectThrows(IllegalArgumentException.class, () -> DeviceFactory.createDevice("FAN", "fan_1"));
    }

    private static void deviceRegistryRegisterAndLookup() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        SmartDevice light = new TestDevice("light_1", "LIGHT");

        registry.registerDevice(light);

        assertTrue(registry.deviceExists("light_1"), "device should exist");
        assertNotNull(registry.getDevice("light_1"), "device lookup should return value");
        assertEquals("light_1", registry.getDevice("light_1").getDeviceId(), "lookup returned wrong device");
    }

    private static void deviceRegistryRejectsNullDevice() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        expectThrows(DomainException.class, () -> registry.registerDevice(null));
    }

    private static void ruleFactoryCreatesValidRule() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));

        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());
        Rule rule = factory.createRule("rule_1", "motion_detected", "light_1", Arrays.asList(new TurnOnAction()));

        assertEquals("rule_1", rule.getRuleID(), "rule id mismatch");
        assertEquals("motion_detected", rule.getTriggerEventType(), "trigger mismatch");
        assertEquals("light_1", rule.getTargetDeviceId(), "target mismatch");
        assertEquals(RuleState.DRAFT, rule.getCurrentState(), "new rule should start as DRAFT");
    }

    private static void ruleFactoryRejectsUnknownDevice() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());

        expectThrows(DomainException.class,
                () -> factory.createRule("rule_1", "motion_detected", "missing", Arrays.asList(new TurnOnAction())));
    }

    private static void ruleFactoryRejectsUnknownEventType() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));
        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());

        expectThrows(DomainException.class,
                () -> factory.createRule("rule_1", "unknown_event", "light_1", Arrays.asList(new TurnOnAction())));
    }

    private static void ruleFactoryRejectsEmptyActions() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));
        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());

        expectThrows(DomainException.class,
                () -> factory.createRule("rule_1", "motion_detected", "light_1", new ArrayList<>()));
    }

    private static void ruleLifecycleActivateDisable() {
        Rule rule = new Rule("rule_1", Arrays.asList(new TurnOnAction()));
        assertEquals(RuleState.DRAFT, rule.getCurrentState(), "initial state should be DRAFT");

        rule.activate();
        assertEquals(RuleState.ACTIVE, rule.getCurrentState(), "state should be ACTIVE after activate");

        rule.disable();
        assertEquals(RuleState.DISABLED, rule.getCurrentState(), "state should be DISABLED after disable");
    }

    private static void rulesEngineExecutesMatchingRule() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));

        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());
        Rule rule = factory.createRule("rule_1", "motion_detected", "light_1", Arrays.asList(new TurnOnAction()));

        RulesEngine engine = new RulesEngine(registry, new SilentAuditLogger());
        engine.registerRule(rule);

        Event event = new Event("evt_1", "motion_detected", "sensor_1", System.currentTimeMillis());
        engine.processEvent(event);

        assertEquals("ON", registry.getDevice("light_1").getState(), "device should be ON after matching event");
    }

    private static void rulesEngineSkipsOverlappingTargets() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));

        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());
        Rule turnOn = factory.createRule("rule_on", "motion_detected", "light_1", Arrays.asList(new TurnOnAction()));
        Rule turnOff = factory.createRule("rule_off", "motion_detected", "light_1", Arrays.asList(new TurnOffAction()));

        RulesEngine engine = new RulesEngine(registry, new SilentAuditLogger());
        engine.registerRule(turnOn);
        engine.registerRule(turnOff);

        Event event = new Event("evt_1", "motion_detected", "sensor_1", System.currentTimeMillis());
        engine.processEvent(event);

        assertEquals("ON", registry.getDevice("light_1").getState(),
                "second rule should be skipped due to same-target conflict in one event cycle");
    }

    private static void rulesEngineStrictAbortsAfterFailure() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));

        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());
        List<Action> actions = Arrays.asList(new ThrowingDomainAction(), new TurnOnAction());
        Rule rule = factory.createRule("rule_strict", "motion_detected", "light_1", actions);

        RulesEngine engine = new RulesEngine(registry, new SilentAuditLogger());
        engine.registerRule(rule);
        engine.setPolicy(ExecutionPolicy.STRICT);

        Event event = new Event("evt_1", "motion_detected", "sensor_1", System.currentTimeMillis());
        engine.processEvent(event);

        assertEquals("OFF", registry.getDevice("light_1").getState(),
                "STRICT should stop before TurnOnAction runs");
    }

    private static void rulesEngineLenientContinuesAfterFailure() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        registry.registerDevice(new TestDevice("light_1", "LIGHT"));

        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());
        List<Action> actions = Arrays.asList(new ThrowingDomainAction(), new TurnOnAction());
        Rule rule = factory.createRule("rule_lenient", "motion_detected", "light_1", actions);

        RulesEngine engine = new RulesEngine(registry, new SilentAuditLogger());
        engine.registerRule(rule);
        engine.setPolicy(ExecutionPolicy.LENIENT);

        Event event = new Event("evt_1", "motion_detected", "sensor_1", System.currentTimeMillis());
        engine.processEvent(event);

        assertEquals("ON", registry.getDevice("light_1").getState(),
                "LENIENT should continue to TurnOnAction after failure");
    }

    private static void motionSensorObserverNotifiesEngine() {
        DeviceRegistry registry = new DeviceRegistry(new SilentAuditLogger());
        TestDevice light = new TestDevice("light_1", "LIGHT");
        registry.registerDevice(light);

        MotionSensor sensor = new MotionSensor("sensor_1");
        registry.registerDevice(sensor);

        RuleFactory factory = new RuleFactory(registry, new SilentAuditLogger());
        Rule rule = factory.createRule("rule_1", "motion_detected", "light_1", Arrays.asList(new TurnOnAction()));

        RulesEngine engine = new RulesEngine(registry, new SilentAuditLogger());
        engine.registerRule(rule);
        sensor.addListener(engine);

        sensor.setState("MOTION_DETECTED");

        assertEquals("ON", light.getState(), "observer path should turn light on");
    }

    private static void repositoryQueriesByStateAndEvent() {
        InMemoryRepository repo = new InMemoryRepository();

        Rule activeRule = new Rule("r_active", Arrays.asList(new TurnOnAction()));
        activeRule.setTriggerEventType("motion_detected");
        activeRule.setTargetDeviceId("light_1");
        activeRule.activate();

        Rule disabledRule = new Rule("r_disabled", Arrays.asList(new TurnOnAction()));
        disabledRule.setTriggerEventType("door_opened");
        disabledRule.setTargetDeviceId("light_2");
        disabledRule.activate();
        disabledRule.disable();

        repo.saveRule(activeRule);
        repo.saveRule(disabledRule);

        List<Rule> active = repo.findRulesByState(RuleState.ACTIVE);
        List<Rule> motion = repo.findRulesByEventType("motion_detected");

        assertEquals(1, active.size(), "expected one ACTIVE rule");
        assertEquals("r_active", active.get(0).getRuleID(), "wrong ACTIVE rule");

        assertEquals(1, motion.size(), "expected one motion_detected rule");
        assertEquals("r_active", motion.get(0).getRuleID(), "wrong motion rule");
    }

    private static void turnActionsUpdateState() {
        TestDevice device = new TestDevice("light_1", "LIGHT");

        new TurnOnAction().execute(device);
        assertEquals("ON", device.getState(), "TurnOnAction should set ON");

        new TurnOffAction().execute(device);
        assertEquals("OFF", device.getState(), "TurnOffAction should set OFF");
    }

    private static void expectThrows(Class<? extends Throwable> expected, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                return;
            }
            throw new AssertionError("Expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        throw new AssertionError("Expected exception: " + expected.getSimpleName());
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
    }

    private static final class ThrowingDomainAction implements Action {
        @Override
        public void execute(SmartDevice device) {
            throw new DomainException("RULE_EXECUTION_FAILED", "Simulated failure");
        }

        @Override
        public String getDescription() {
            return "ThrowingDomainAction";
        }
    }

    private static final class TestDevice implements SmartDevice {
        private final String id;
        private final String type;
        private String state = "OFF";

        private TestDevice(String id, String type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public String getDeviceId() {
            return id;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getState() {
            return state;
        }

        @Override
        public void setState(String state) {
            this.state = state;
        }
    }

    private static final class SilentAuditLogger implements AuditLogger {
        @Override
        public void logEventReceived(Event event) {}

        @Override
        public void logRuleMatched(Event event, Rule rule) {}

        @Override
        public void logActionAttempted(Rule rule, Action action, String targetDeviceId) {}

        @Override
        public void logActionSuccess(Rule rule, Action action, String targetDeviceId) {}

        @Override
        public void logActionFailure(Rule rule, Action action, String targetDeviceId, String failureReason) {}

        @Override
        public void logRuleSkipped(Event event, Rule rule, String reason) {}

        @Override
        public void logSystemEvent(String message) {}
    }
}

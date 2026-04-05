// QUICK REFERENCE: Rules Engine MVP Implementation Guide
// Use this to track your progress through the skeleton files

// ============================================================================
// FLOW: How Events Become Actions (High Level)
// ============================================================================

1. SETUP PHASE (Main.java or test)
   └─> Create DeviceRegistry
   └─> Register LightDevice("light_1"), MotionSensor("sensor_1")
   └─> Create RuleFactory(deviceRegistry)
   └─> Create ConsoleAuditLogger
   └─> Create RulesEngine(deviceRegistry, auditLogger)

2. RULE CREATION PHASE
   └─> admin: RuleFactory.createRule(
         ruleID="rule_1",
         triggerEventType="motion_detected",
         targetDeviceId="light_1",
         actions=[new TurnOnAction()]
       )
   └─> Factory validates: event type exists, device exists, action list not empty
   └─> Returns Rule in DRAFT state
   └─> RulesEngine.registerRule(rule)
   └─> RulesEngine.activateRule("rule_1") → transitions to ACTIVE

3. EVENT PROCESSING PHASE (when motion is detected)
   └─> Simulator: RulesEngine.processEvent(new Event("motion_detected", "sensor_1"))
   └─> Engine finds ALL rules matching "motion_detected"
   └─> For each matching ACTIVE rule:
       └─> Check if target device already used in this event cycle
       └─> If device already targeted → SKIP rule, log conflict
       └─> If device available → Execute all actions, mark device as used
   └─> Action executes on device → device state changes
   └─> AuditLogger prints to console

// ============================================================================
// UPDATED: "Do Not Allow Overlapping Triggers" = OPTION B
// ============================================================================

Interpretation B: Multiple Rules OK, But Can't Target Same Device

What It Means:
  - Multiple rules CAN match the same event type
  - But within ONE event cycle, no two rules can target the SAME device
  - If conflict detected: skip the conflicting rule, continue with others
  - Allows: "motion_detected" → turn on light_1 AND turn on light_2 (different devices)
  - Prevents: "motion_detected" → turn on light_1 AND turn off light_1 (same device)

Example:
  Setup:
    - rule_1: "IF motion_detected THEN turn on light_1"
    - rule_2: "IF motion_detected THEN turn off light_1"   ← CONFLICT with rule_1
    - rule_3: "IF motion_detected THEN turn on light_2"    ← NO CONFLICT
  
  Event: Motion detected
  
  Behavior:
    ✓ rule_1 executes → light_1 turns ON, device "light_1" marked as used
    ✗ rule_2 SKIPPED → would target "light_1" (already used), log conflict
    ✓ rule_3 executes → light_2 turns ON, device "light_2" marked as used
    → Result: light_1 is ON, light_2 is ON

Code Impact:
  RulesEngine.processEvent(Event event):
    Set<String> devicesTargetedThisEvent = new HashSet<>();
    
    for (Rule rule : activeRules) {
      if (rule.getTriggerEventType().equals(event.getType()) && rule.isActive()) {
        String targetDevice = rule.getTargetDeviceId();
        
        if (devicesTargetedThisEvent.contains(targetDevice)) {
          auditLogger.logRuleSkipped(event, rule, "Device already targeted this event");
          continue;
        }
        
        executeRule(rule);
        devicesTargetedThisEvent.add(targetDevice);
      }
    }

// ============================================================================
// IMPLEMENTATION CHECKLIST
// ============================================================================

Event.java
  ☑ Constructor: validate timestamp not in future
  ☑ getEventId(), getEventType(), getSourceDeviceId(), getTimestamp()
  ☑ toString() for logging

DeviceRegistry.java
  ☑ Constructor: initialize Map<String, SmartDevice>
  ☑ registerDevice(SmartDevice): validate device not null, store in map
  ☑ getDevice(String deviceId): return device or null
  ☑ deviceExists(String deviceId): boolean check
  ☑ getAllDevices(): return devices collection

DomainException.java
  ☑ Single `DomainException` extends RuntimeException
  ☑ Supports error codes such as `INVALID_RULE`, `UNKNOWN_DEVICE`, `UNKNOWN_EVENT_TYPE`, `RULE_EXECUTION_FAILED`
  ☑ Constructor with `(errorCode, message)`
  ☑ Constructor with `(errorCode, message, cause)`

Rule.java
  ☑ Add field: private String triggerEventType
  ☑ Add field: private String targetDeviceId
  ☑ getRuleID(): return ruleID
  ☑ getTriggerEventType(): return triggerEventType
  ☑ getTargetDeviceId(): return targetDeviceId
  ☑ getActions(): return actions
  ☑ getCurrentState(): return currentState
  ☑ setTriggerEventType(String): set field
  ☑ setTargetDeviceId(String): set field

RuleFactory.java
  ☑ Constructor: accept DeviceRegistry, initialize validEventTypes Set
  ☑ Constructor: populate validEventTypes with "motion_detected", etc.
  ☑ createRule(): validate all RQ_03 conditions, throw if any fail
  ☑ createRule(): create Rule, set triggerEventType + targetDeviceId, return
  ☑ registerEventType(String): add to validEventTypes
  ☑ getValidEventTypes(): return copy of set

ConsoleAuditLogger.java
  ☑ Constructor: initialize DateTimeFormatter
  ☑ logEventReceived(Event): print "[AUDIT] ... EVENT_RECEIVED: ..."
  ☑ logRuleMatched(Event, Rule): print "[AUDIT] ... RULE_MATCHED: ..."
  ☑ logActionAttempted(Rule, Action, String): print "[AUDIT] ... ACTION_ATTEMPT: ..."
  ☑ logActionSuccess(Rule, Action, String): print "[AUDIT] ... ACTION_SUCCESS: ..."
  ☑ logActionFailure(Rule, Action, String, String): print "[AUDIT] ... ACTION_FAILURE: ..."
  ☑ logRuleSkipped(Event, Rule, String): print "[AUDIT] ... RULE_SKIPPED: ..."
  ☑ logSystemEvent(String): print "[AUDIT] ... SYSTEM: ..."

RulesEngine.java (Most Complex)
  ☑ Constructor: accept DeviceRegistry + AuditLogger, initialize activeRules List
  ☑ registerRule(Rule): add to activeRules, log
  ☑ activateRule(String): find rule by ID, call rule.activate()
  ☑ disableRule(String): find rule by ID, call rule.disable()
  ☑ getRuleCount(): return activeRules.size()
  ☑ findRuleById(String): iterate rules, find and return or null
  ☑ processEvent(Event): THE HOT PATH
       - ☑ Log event received via auditLogger
       - ☑ Find all rules where rule.getTriggerEventType().equals(event.getType())
       - For each matching rule:
         ☑ Check if rule.getCurrentState() == RuleState.ACTIVE
         ☑ Get target device: deviceRegistry.getDevice(rule.getTargetDeviceId())
         ☑ For each action in rule.getActions():
           ☑ try { action.execute(device); auditLogger.logActionSuccess(...); }
           ☑ catch (DomainException e) { auditLogger.logActionFailure(...); continue; }

// ============================================================================
// KEY DESIGN DECISIONS FOR MVP
// ============================================================================

✓ Event Matching: Simple string equality on event type only
  - rule.triggerEventType.equals(event.getType())
  - No complex conditions (AND/OR/NOT) — Phase 2

✓ Rule Ordering: Registration order (first rule registered = evaluated first)
  - No priority field — simplified for MVP
  - SDD says "deterministic order" satisfied by registration order

✓ Action Execution Order: Loop through actions in list order
  - rule.getActions() is iterated as-is
  - If action 2 fails, action 3 still executes (RQ_06)

✓ Throttling: NOT in MVP
  - No cooldown states
  - No per-rule or per-device throttling
  - Phase 2 feature

✓ Constraints: None in MVP
  - No conditions like "only if time > 6AM"
  - Simple event-matching only

✓ Audit Logging: Console output
  - No file persistence
  - Real-time console output with timestamps
  - Sufficient for MVP testing and debugging

// ============================================================================
// TESTING VERIFICATION (After Implementation)
// ============================================================================

Test 1: Device Registration
  ☐ Register light → deviceRegistry.deviceExists("light_1") == true
  ☐ Get light → deviceRegistry.getDevice("light_1") != null
  ☐ Query unknown device → returns null (doesn't crash)

Test 2: Rule Creation & Validation
  ☐ Valid rule → factory creates, returned in DRAFT state
  ☐ No actions → throws InvalidRuleException
  ☐ Unknown device → throws UnknownDeviceException
  ☐ Unknown event type → throws UnknownEventTypeException

Test 3: Rule Activation & Storage
  ☐ Register rule → RulesEngine.getRuleCount() increases
  ☐ Call activateRule() → rule.getCurrentState() == ACTIVE
  ☐ Call disableRule() → rule.getCurrentState() == DISABLED

Test 4: Event Processing (Main Flow)
  ☐ Send event matching registered rule → auditLogger prints logs
  ☐ Rule executes action → light state changes to ON
  ☐ Multiple actions in rule → all execute in order
  ☐ Action throws exception → next action still executes (caught, logged)
  ☐ Send event with no matching rules → logged as no matches, system stable

Test 5: Audit Logging
  ☐ Each log line starts with [AUDIT] and timestamp
  ☐ EVENT_RECEIVED logged when event arrives
  ☐ RULE_MATCHED logged when condition met
  ☐ ACTION_ATTEMPT, ACTION_SUCCESS logged per action
  ☐ ACTION_FAILURE logged with reason on exceptions

// ============================================================================
// COMMON PITFALLS TO AVOID
// ============================================================================

❌ Rule starts ACTIVE instead of DRAFT
   → Rules should default to DRAFT (call activate() manually)

❌ Passing device instead of deviceId through system
   → Store device IDs in rules, look up device in registry during execution
   → Allows device to be offline without breaking entire rule

❌ Returning null from getters instead of actual values
   → Complete all getter implementations, RulesEngine depends on them

❌ Not handling exception in action execution
   → Use try-catch per action with auditLogger.logActionFailure()
   → CONTINUE to next action (RQ_06)

❌ Hardcoding "motion_detected" in matching logic
   → Use rule.getTriggerEventType() for flexible matching
   → Allows any event type without code changes

❌ Forgetting to initialize Map/List/Set in constructors
   → NullPointerException when adding items
   → Always initialize collections in constructor

❌ Logging too verbose or not at all
   → Use "[AUDIT]" tag consistently for filtering
   → Log at key points: event received, rule matched, action outcome
   → Skip logging every intermediate check

// ============================================================================
// NEXT STEPS AFTER SKELETON IMPLEMENTATION
// ============================================================================

1. Implement all skeleton TODO items in the order recommended above
2. Create simple Main.java test to verify happy path
3. Run and verify console output matches expected logs
4. Add more actions (AlertAction, LogAction) if time permits
5. Test error cases (missing device, invalid event type, action exception)
6. Prepare for RulesEngine testing and Phase 2 features (throttling)

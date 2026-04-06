# Smart Home Automation Rules Engine
**CSCN72040 Term Project — Phase 2**
Team 3n1: Leanne Kidder, Ryan Hackbart, Liban Nur

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 11 or higher |
| No external libraries | Standard library only |

Verify your Java version:
```
java -version
javac -version
```

---

## Project Structure

```
src/main/java/com/team3n1/smarthome/
├── Main.java                          Entry point — runs the full demo
├── application/
│   ├── DeviceFactory.java             Factory pattern — creates devices by type
│   ├── DeviceRegistry.java            Central device registry
│   ├── ExecutionPolicy.java           LENIENT / STRICT policy enum
│   ├── LightDevice.java               Virtual light device
│   ├── MotionSensor.java              Virtual motion sensor (Observer subject)
│   ├── RuleFactory.java               Factory pattern — validates and creates rules
│   └── RulesEngine.java               Core engine — processes events, fires rules
├── core/
│   ├── actions/
│   │   ├── Action.java                Command pattern interface
│   │   ├── TurnOnAction.java          Turns a device ON
│   │   └── TurnOffAction.java         Turns a device OFF
│   ├── exceptions/
│   │   └── DomainException.java       Domain-level runtime exception
│   └── model/
│       ├── DeviceEventListener.java   Observer pattern interface
│       ├── Event.java                 Immutable event (motion_detected, door_opened, …)
│       ├── Rule.java                  Automation rule with lifecycle state machine
│       ├── RuleState.java             DRAFT / ACTIVE / TRIGGERED / DISABLED
│       └── SmartDevice.java           SmartDevice interface
└── infrastructure/
    ├── logging/
    │   ├── AuditLogger.java           Logging interface
    │   └── ConsoleAuditLogger.java    Prints timestamped audit entries to console
    └── persistence/
        └── InMemoryRepository.java    In-memory storage for rules and devices
```

---

## How to Run

### Option A — Command line (javac + java)

**Step 1 — Compile** from the project root:
```
javac -d target/classes -sourcepath src/main/java src/main/java/com/team3n1/smarthome/Main.java
```

**Step 2 — Run:**
```
java -cp target/classes com.team3n1.smarthome.Main
```

> If `target/classes` does not exist, create it first:
> ```
> mkdir -p target/classes
> ```
> On Windows (Command Prompt):
> ```
> mkdir target\classes
> ```

---

### Option B — IntelliJ IDEA

1. Open IntelliJ IDEA and choose **Open** → select the `Java_Project3n1` folder.
2. Mark `src/main/java` as the **Sources Root**:
   right-click the folder → **Mark Directory as → Sources Root**.
3. Open `src/main/java/com/team3n1/smarthome/Main.java`.
4. Click the green **Run** arrow next to `public static void main`.

---

### Option C — VS Code

1. Install the **Extension Pack for Java** (Microsoft).
2. Open the `Java_Project3n1` folder.
3. Open `Main.java` and click **Run** above the `main` method,
   or press `F5`.

---

## Demo Output

Running `Main` executes a fully scripted demonstration with no user input required.
Each section is labelled so the instructor can map output to rubric items:

| Label | Requirement |
|---|---|
| `[REQ-2.1]` | Runnable CLI interface |
| `[REQ-2.2]` | Architecture boundaries (domain / application / infrastructure) |
| `[REQ-2.3]` | Persistence — save to `InMemoryRepository`, reload into a fresh engine |
| `[REQ-2.4]` | Two queries — `findRulesByState(ACTIVE)` and `findRulesByEventType(…)` |
| `[REQ-2.5]` | Lifecycle / state machine — valid transitions and forbidden rejections |
| `[REQ-2.6]` | Two policies — LENIENT (continue on failure) vs STRICT (abort on failure) |
| `[REQ-2.7]` | Design pattern map — Factory, Command, Observer with live evidence |
| `[REQ-2.8]` | Robustness — three invalid inputs each caught with a meaningful error |
| `[REQ-2.9]` | Performance — 50 events × 10 rules, prints ms / µs / events-per-second |
| `[REQ-2.10]` | Portability — no hardcoded paths, standard Java only |

---

## Design Notes

### Architecture

Three strict layers with a single direction of dependency:

```
infrastructure  →  application  →  domain
```

- **Domain** (`core/`) — `Rule`, `Event`, `SmartDevice`, `Action`, `DomainException`.
  Contains all business rules and state machine logic. Zero imports from other layers.
- **Application** (`application/`) — `RulesEngine`, `RuleFactory`, `DeviceFactory`, `DeviceRegistry`.
  Orchestrates domain objects. No direct I/O.
- **Infrastructure** (`infrastructure/`) — `ConsoleAuditLogger`, `InMemoryRepository`.
  All I/O and persistence. Depends inward on the other two layers.

### Design Patterns

| Pattern | Type | Classes |
|---|---|---|
| Factory | Creational | `DeviceFactory`, `RuleFactory` |
| Command | Behavioral | `Action`, `TurnOnAction`, `TurnOffAction` |
| Observer | Behavioral | `DeviceEventListener`, `MotionSensor`, `RulesEngine` |

### Execution Policy (REQ-2.6)

`ExecutionPolicy.LENIENT` (default) — if an action throws, the engine logs the failure
and continues with the next action in the rule (maximises reliability).

`ExecutionPolicy.STRICT` — if an action throws, the engine aborts all remaining actions
for that rule (treats the action list as atomic).

Switch at runtime: `engine.setPolicy(ExecutionPolicy.STRICT)`.

### Rule Lifecycle States

```
DRAFT  ──save+validate──▶  ACTIVE  ──event match──▶  TRIGGERED  ──action sent──▶  COOLDOWN
                              ▲                           │
                              └────────────disable()──────┘
                                                          ▼
                                                       DISABLED
```

Forbidden transitions (rejected with `[REJECTED]` message):
- `DRAFT → TRIGGERED`
- `DISABLED → TRIGGERED`

---

## Portability Notes

- No hardcoded absolute file paths anywhere in the codebase.
- All storage is in-memory (`java.util.LinkedHashMap`); no file system access required.
- No third-party dependencies — compiles with a plain `javac` invocation.
- Tested on Java 11. Compatible with Java 11 through Java 21.

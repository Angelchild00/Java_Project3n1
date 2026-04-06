package com.team3n1.smarthome.application;

/**
 * Configurable execution policy for the RulesEngine (REQ-2.6).
 *
 * Controls what happens when an action fails mid-rule:
 *
 *   LENIENT — continue executing the remaining actions in the rule even after
 *             a failure. This is the "best-effort" approach: as many actions
 *             as possible are attempted. Already the default behaviour before
 *             this policy was made explicit.
 *             Quality attribute gained: Reliability (RQ_06 — one failure must
 *             not stop other actions).
 *             Trade-off: a partially-executed rule can leave devices in an
 *             inconsistent intermediate state.
 *
 *   STRICT  — abort the remaining actions in the rule as soon as one fails.
 *             No further actions for that rule are attempted; the engine then
 *             moves on to the next matching rule.
 *             Quality attribute gained: Consistency (the rule is treated as
 *             an atomic unit — all-or-nothing per rule).
 *             Trade-off: fewer actions are executed when there is a fault,
 *             which may leave some devices untouched.
 *
 * Demo usage (REQ-2.6 — run the same scenario with Policy A then Policy B
 * and show a different outcome):
 *
 *   engine.setPolicy(ExecutionPolicy.LENIENT);
 *   engine.processEvent(event);   // both actions attempted even if first fails
 *
 *   engine.setPolicy(ExecutionPolicy.STRICT);
 *   engine.processEvent(event);   // second action skipped after first fails
 *
 * @author Team 3n1
 * @version MVP
 */
public enum ExecutionPolicy {

    /**
     * Best-effort: continue with remaining actions after a failure.
     * Satisfies RQ_06 in its original form.
     */
    LENIENT,

    /**
     * Fail-fast: abort remaining actions in the current rule on first failure.
     * Treats each rule's action list as an atomic unit.
     */
    STRICT
}

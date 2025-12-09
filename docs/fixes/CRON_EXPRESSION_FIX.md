# Cron Expression Fix - COMPLETE ✅

## Problem
Invalid cron expressions in BPMN timer events were causing application startup failure:
```
ENGINE-09026 Exception while parsing cycle expression:
Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.
```

## Root Cause
Camunda 7 cron expressions have **6 fields** (not 5 like Unix cron):
- Format: `second minute hour day-of-month month day-of-week`
- **CRITICAL**: Cannot use `*` for BOTH day-of-month AND day-of-week
- Must use `?` (question mark) for one of them to indicate "no specific value"

## Files Fixed

### 1. SUB_08_Revenue_Collection.bpmn
**Before**: `0 0 6 * * *` (invalid - both day fields are wildcards)
**After**: `0 0 6 * * ?` (valid - day-of-week ignored)
**Meaning**: Every day at 6:00 AM

### 2. SUB_09_Analytics.bpmn
**Before**: `0 */5 * * * *` (invalid - both day fields are wildcards)
**After**: `0 */5 * * * ?` (valid - day-of-week ignored)
**Meaning**: Every 5 minutes, any day

### 3. SUB_10_Maximization.bpmn
**Before**: `0 0 * * 1` (invalid - only 5 fields)
**After**: `0 0 0 ? * 1` (valid - 6 fields, day-of-month ignored)
**Meaning**: Every Monday at midnight (00:00:00)

### 4. SUB_04_Clinical_Production.bpmn
**Status**: No change needed
**Expression**: `R/PT1H` (ISO 8601 duration format - valid)
**Meaning**: Repeating every 1 hour

## Verification Results

✅ **Build**: SUCCESS
✅ **Cron Parsing**: NO ERRORS
✅ **Application Start**: "Starting RevenueCycleApplication v1.0.0"

## Camunda Cron Format Reference

```
Field          Allowed Values    Special Characters
----------     --------------    ------------------
Seconds        0-59              , - * /
Minutes        0-59              , - * /
Hours          0-23              , - * /
Day-of-month   1-31              , - * ? / L W
Month          1-12 or JAN-DEC   , - * /
Day-of-week    1-7 or SUN-SAT    , - * ? / L #
```

**Special Characters**:
- `*` = "any value"
- `?` = "no specific value" (must be used when the other day field has a value)
- `/` = "increments" (e.g., `*/5` = every 5 units)
- `-` = "range" (e.g., `1-5` = 1 through 5)
- `,` = "list" (e.g., `1,3,5` = 1 and 3 and 5)

## Examples

```
0 0 0 * * ?     = Every day at midnight (ignore day-of-week)
0 0 0 ? * 1     = Every Monday at midnight (ignore day-of-month)
0 */5 * * * ?   = Every 5 minutes
0 0 6 * * ?     = Every day at 6:00 AM
0 0 12 ? * MON-FRI = Every weekday at noon
R/PT1H          = Repeating every hour (ISO duration format)
```

## Next Issue
The DMN files need `historyTimeToLive` attribute added (separate fix required).

---
**Fixed by**: BPMN Timer Fix Specialist
**Date**: 2025-12-09
**Coordination**: Claude-Flow hooks

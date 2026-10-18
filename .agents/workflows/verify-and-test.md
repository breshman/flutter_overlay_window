---
description: Run the full dual verification suite (Flutter analyze + Flutter test + Android Kotlin test + Graphify update)
---

# Verify and Test Workflow

Use this workflow to validate that all code passes quality gates and architecture standards.

## Execution Steps

1. Run the full verification script:
   ```bash
   .agents/skills/feature-lifecycle/scripts/run_all_tests.sh
   ```

2. Synchronize the knowledge graph:
   ```bash
   graphify update .
   ```

3. Confirm all checks report green:
   - Root `flutter analyze`: `No issues found!`
   - Example `flutter analyze`: `No issues found!`
   - `flutter test`: 19/19 passing tests
   - Kotlin `testDebugUnitTest`: 11/11 passing tests

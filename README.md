# AppFunctions Compose Agent Demo

A dedicated multi-module Android sample showing how to expose Android AppFunctions from a Compose app so authorized agent apps (Gemini, ChatGPT integrations, OEM assistants) can invoke them.

## Modules

- `app`: Compose UI + AppFunctions provider (`ExpenseAgentFunctions`)
- `core:ledger`: Pure Kotlin expense logic reused by UI and AppFunctions

## AppFunctions included

- `addSharedExpense(...)`
- `listRecentExpenses(...)`

Implemented in `app/src/main/java/com/example/appfunctionsdemo/functions/ExpenseAgentFunctions.kt` using `@AppFunction`.

## Build + test

```bash
./gradlew check
```

This runs unit tests, lint, and coverage verification.

## Coverage gates

- App logic gate: `app:jacocoDebugCoverageVerification` with minimum line coverage `0.80`
- Core module gate: `core:ledger:jacocoTestCoverageVerification` with minimum line coverage `0.80`

Reports:

- `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html`
- `core/ledger/build/reports/jacoco/test/html/index.html`

## Verify AppFunctions are indexed on device

Use an Android 16+ image/device:

```bash
adb shell cmd app_function list-app-functions
```

You should see this package and the exposed functions.

## Important note on callers

AppFunctions execution by external apps requires callers with platform permission `android.permission.EXECUTE_APP_FUNCTIONS`, which is restricted to authorized agent/system apps.

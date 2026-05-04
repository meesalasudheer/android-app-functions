# AppFunctions Compose Agent Demo

A dedicated multi-module Android sample showing how to expose Android AppFunctions from a Compose app so authorized agent apps (Gemini, ChatGPT integrations, OEM assistants) can invoke them.

## Modules

- `app`: Compose UI + AppFunctions provider (`ExpenseAgentFunctions`)
- `core:ledger`: Pure Kotlin expense logic reused by UI and AppFunctions
- `agent-caller`: Separate caller app that discovers metadata and executes target app functions

## AppFunctions included

- `addSharedExpense(...)`
- `listRecentExpenses(...)`

Implemented in `app/src/main/java/com/example/appfunctionsdemo/functions/ExpenseAgentFunctions.kt` using `@AppFunction`.

## Build + test

```bash
# Unit tests + lint + coverage gates
./gradlew check

# Caller module build + unit tests
./gradlew :agent-caller:assembleDebug :agent-caller:testDebugUnitTest

# Connected instrumentation tests (requires running emulator/device)
./gradlew :agent-caller:connectedDebugAndroidTest
```

This runs unit tests, lint, and coverage verification.

For CI, run the first two commands on all pipelines, and run the connected test command on pipelines where an Android 16+ emulator/device is available.

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

## Agent caller app usage

1. Install provider app and caller app on the same Android 16+ device.
2. Open **Agent Caller** app (`com.example.agentcaller`).
3. Tap **Invoke addSharedExpense** then **Invoke listRecentExpenses**.
4. Caller discovers target metadata via `observeAppFunctions(...)`, builds typed `AppFunctionData` with metadata, and executes.

## Important note on callers

AppFunctions execution by external apps requires callers with platform permission `android.permission.EXECUTE_APP_FUNCTIONS`, which is restricted to authorized agent/system apps.

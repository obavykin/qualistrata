# Cross-module telemetry model

QualiStrata telemetry is a cross-module capability, not a UI-only feature.

Runtime modules such as `qualistrata-ui`, `qualistrata-db`, `qualistrata-unix`, and future API/runtime modules should be instrumentable through the lightweight telemetry contracts in `qualistrata-core`.

## Design split

```text
qualistrata-core
  Lightweight telemetry API:
  - TelemetrySink
  - TelemetryEvent
  - TelemetryEventType
  - TelemetryTimer
  - NoOpTelemetrySink

qualistrata-perf
  Full performance implementation, later:
  - local JSON/HTML performance reports
  - publishers
  - safe publisher wrappers
  - guards
  - aggregation/statistics
  - future InfluxDB integration

runtime modules
  Emit generic telemetry through the core API.
```

The core API is intentionally small so runtime modules can be instrumented without forcing all consumers to depend on the full performance module.

## Runtime module rule

Runtime modules may emit generic metrics such as:

```text
unix.command.execution.ms
unix.file.upload.ms
db.connection.open.ms
db.statement.execution.ms
ui.window.open.ms
api.request.duration.ms
```

Consumer projects may add business-level metrics on top, for example:

```text
tnt.utility.data-load.duration.ms
tnt.flow.third-party-load.total.duration.ms
```

## Failure behavior

Normal functional, smoke, and regression tests must not fail because telemetry publishing failed.

Only explicit guards or dedicated performance thresholds may fail execution.

# QualiStrata

**QualiStrata** is a modular Java framework for layered test automation and product validation.

It helps engineering and QA teams avoid slow, fragile UI-heavy regression suites by moving checks to the lowest reliable layer: UI, API, database, Unix/infrastructure, exports, and performance telemetry.

> Test at the right layer.

## Why QualiStrata?

Many legacy enterprise regression suites try to validate everything through the UI: reports, data transformations, CSV exports, backend calculations, and environment readiness. This makes tests slow, flaky, expensive to run, and difficult to diagnose.

QualiStrata promotes a layered validation model:

- UI tests for user journeys, navigation, rendering, and critical smoke checks
- API tests for service contracts and integration behavior
- DB/report tests for dataset correctness and backend calculations
- Unix/infrastructure checks for environment readiness
- Performance telemetry for trends, guardrails, and observability

## Project status

QualiStrata is in early development.

The project is currently being extracted from a real enterprise test automation redesign initiative and is not yet ready for production adoption.

The public repository intentionally contains only generic framework modules. Product-specific implementations, credentials, environments, and customer-related code must stay outside this repository.

## Modules

| Module | Purpose |
|---|---|
| `qualistrata-core` | Configuration, target resolution, common contracts |
| `qualistrata-ui` | UI automation abstractions and browser automation support |
| `qualistrata-api` | API testing support |
| `qualistrata-db` | Database and report validation support |
| `qualistrata-unix` | SSH/Unix automation utilities |
| `qualistrata-perf` | Performance metrics and telemetry infrastructure |


## Current core capabilities

`qualistrata-core` currently provides:

- generic target configuration model;
- environment profile model;
- Unix command/result DTOs;
- framework exceptions;
- YAML target configuration loading;
- deep target configuration merging;
- target selection from `-Dtarget`, `TARGET`, or explicit project default;
- conventional target configuration path resolution;
- resolved target configuration service.

## Design principles

- Test at the lowest reliable layer
- Keep UI tests compact, deterministic, and diagnosable
- Do not use UI as a universal data validator
- Avoid hard sleeps
- Prefer condition-based waits and explicit diagnostics
- Keep framework modules generic
- Keep product-specific logic outside the framework
- Generate useful local artifacts
- Keep metrics infrastructure optional and non-blocking by default

## Documentation

- [Target configuration](docs/configuration/target-configuration.md)

## Build

```bash
./gradlew build
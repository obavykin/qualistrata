# Contributing to QualiStrata

Thank you for your interest in contributing to QualiStrata.

QualiStrata is currently in early development. The public API is not stable yet, and the project structure may still change while the framework is being extracted and cleaned up.

## Development principles

Please keep the following principles in mind:

- Keep framework modules generic.
- Do not add product-specific logic to framework modules.
- Prefer testing at the lowest reliable layer.
- Keep UI tests compact, deterministic, and diagnosable.
- Do not use hard sleeps.
- Prefer condition-based waits and explicit diagnostics.
- Add unit tests for reusable framework code.
- Keep secrets, environment-specific configuration, and customer-related information out of the repository.

## Build

```bash
./gradlew build
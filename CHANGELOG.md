# Changelog

All notable changes to QualiStrata will be documented in this file.

The project follows a simple chronological changelog format during early development.

## 0.1.0-SNAPSHOT

### Added

- Initial multi-module Gradle project structure;
- Core modules:
    - `qualistrata-core`
    - `qualistrata-ui`
    - `qualistrata-api`
    - `qualistrata-db`
    - `qualistrata-unix`
    - `qualistrata-perf`
- Initial project README;
- Apache License 2.0;
- Generic target configuration loader;
- Deep target configuration merger;
- Target selection model;
- Target configuration path resolver;
- Resolved target configuration service;
- Unit tests for core configuration pipeline;
- Generic structural validator for resolved target configuration;
- Public configuration facade for resolving and validating target configuration;
- Unix session and pooling architecture documentation;
- Pooled Unix session invalidation support;
- Documentation for pooled Unix session invalidation lifecycle;
- Generic Unix session health check contract and default open-session implementation;
- SSH connection abstraction and SSH-backed Unix host session factory;
- JSch-based SSH connection implementation;
- Documentation for JSch SSH transport implementation;
- Integration-test source set for infrastructure-dependent tests;
- Documentation for guarded SSH integration-test configuration;
- Documentation for Unix session provider factory usage;
- Unix host session request factory for creating session requests from resolved target configuration;
- Documentation for Unix host session request factory usage;
- Documentation for high-level Unix facade usage;
- Convenience command execution overloads in `UnixFacade`;
- Documentation for `UnixFacade` convenience command execution overloads;
- README usage example for high-level Unix automation via `UnixFacade`;
- Package-level Javadoc documentation for Unix public API and infrastructure packages;
- Generic environment check result model and suite summary contracts;
- Environment check definition and runner for executing check suites and aggregating results;
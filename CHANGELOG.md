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
- Unix command environment check for reusable command-based readiness validation;
- Documentation for Unix command environment checks and readiness suite usage;
- Unix operating system environment check based on `/etc/os-release`;
- Documentation for environment checks, Unix readiness checks, and generic report writers;
- Generic environment value matchers for exact, contains, and regex-based baseline validation;
- Generic Unix component environment check using command output and environment value matchers;
- Documentation for Unix component environment checks and environment value matchers;
- Added generic JDBC database client implementation for `qualistrata-db`.
- Added generic row and row collection mapping for SQL query results.
- Added scalar query, existence query, update execution, and transaction support.
- Added Oracle database provider with service-name and legacy SID JDBC URL support.
- Added PostgreSQL database provider with standard PostgreSQL JDBC URL support.
- Added provider-level health-check SQL and database-version SQL for Oracle and PostgreSQL.
- Added JUnit unit tests for JDBC client behavior, row mapping, transactions, and provider URL builders.
- Added package-level documentation through `package-info.java` files for DB module packages.
- Added `docs/qualistrata-db.md` describing module scope, package responsibilities, provider model, URL rules, security rules, and testing strategy;
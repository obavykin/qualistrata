# Changelog

All notable changes to QualiStrata will be documented in this file.

The project follows a simple chronological changelog format during early development.

## 0.1.0-SNAPSHOT

### Added

- Initial multi-module Gradle project structure.
- Core framework modules:
  - `qualistrata-core`
  - `qualistrata-ui`
  - `qualistrata-api`
  - `qualistrata-db`
  - `qualistrata-unix`
  - `qualistrata-perf`
- Initial project README and Apache License 2.0.

#### Core configuration

- Generic target configuration loading.
- Deep target configuration merging.
- Target selection and resolved target configuration services.
- Public configuration facade for resolving and validating target configuration.
- Structural validation for resolved target configuration.
- Unit tests for the core configuration pipeline.

#### Unix automation

- Generic Unix session abstraction.
- SSH-backed Unix host session factory.
- JSch-based SSH transport implementation.
- Unix session pooling and invalidation support.
- Unix session provider factory.
- Unix host session request factory for creating session requests from resolved target configuration.
- High-level `UnixFacade` for command execution.
- Guarded integration-test source set for infrastructure-dependent Unix tests.
- Package-level Javadoc and user documentation for Unix public API and infrastructure packages.

#### Environment validation

- Generic environment check result model.
- Environment check definition and suite runner.
- Suite summary contracts.
- Generic environment value matchers:
  - exact match;
  - contains match;
  - regex match.
- Unix command environment check.
- Unix operating system check based on `/etc/os-release`.
- Generic Unix component environment check using command output and value matchers.
- Generic report writer documentation for environment/readiness validation.

#### Database automation

- Generic database access API for automated tests.
- Generic JDBC database client implementation.
- SQL query, scalar query, existence query, update execution, and transaction support.
- Generic row and row collection mapping for SQL query results.
- Database provider registry with support for provider override.
- Oracle database provider:
  - service-name JDBC URL support;
  - legacy SID JDBC URL support;
  - Oracle health-check SQL;
  - Oracle database-version SQL.
- PostgreSQL database provider:
  - standard PostgreSQL JDBC URL support;
  - PostgreSQL health-check SQL;
  - PostgreSQL database-version SQL.
- Generic DB environment checks:
  - DB connection check;
  - DB version check.
- Safe DB diagnostics policy for reportable environment-check results.
- JUnit unit tests for DB contracts, JDBC behavior, row mapping, transactions, providers, and environment checks.
- Package-level Javadoc and user documentation for the DB module.

#### Documentation

- Documentation for:
  - target configuration;
  - Unix session pooling;
  - SSH transport;
  - guarded integration tests;
  - Unix facade usage;
  - Unix command and component environment checks;
  - environment value matchers;
  - DB module usage;
  - DB providers;
  - DB environment checks.
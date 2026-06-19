# QualiStrata DB Module

`qualistrata-db` is a generic database access module for QualiStrata-based automated tests.

The module is not Oracle-specific and is not limited to environment validation. It provides a reusable API that consumer projects can use from UI, API, performance, integration, smoke, and regression tests.

## Current Scope

Implemented capabilities:

- generic DB connection configuration;
- generic DB client contract;
- row and row collection abstractions;
- scalar query support;
- existence query support;
- update execution;
- transaction callback API;
- generic JDBC implementation;
- database provider registry;
- Oracle provider;
- PostgreSQL provider.

Environment checks are intentionally not part of this increment. They should be implemented on top of the generic DB API and providers.

## Package Overview

| Package | Responsibility |
| --- | --- |
| `com.bavykin.qualistrata.db` | Shared DB concepts such as `DbType`. |
| `com.bavykin.qualistrata.db.config` | Connection configuration model. |
| `com.bavykin.qualistrata.db.client` | Generic DB client contract. |
| `com.bavykin.qualistrata.db.result` | Query result and row abstractions. |
| `com.bavykin.qualistrata.db.transaction` | Transaction contracts. |
| `com.bavykin.qualistrata.db.provider` | Provider contracts and registry. |
| `com.bavykin.qualistrata.db.jdbc` | Generic JDBC implementation. |
| `com.bavykin.qualistrata.db.oracle` | Oracle provider and Oracle JDBC URL builder. |
| `com.bavykin.qualistrata.db.postgres` | PostgreSQL provider and PostgreSQL JDBC URL builder. |

## Provider Model

Consumers use the same high-level DB API regardless of the database engine.

Engine-specific behavior is isolated in providers:

- `OracleDbProvider`
- `PostgresDbProvider`

Providers are responsible for:

- validating matching `DbType`;
- building JDBC URLs when an explicit JDBC URL is not provided;
- creating a `DbClient`;
- exposing database-specific health-check SQL;
- exposing database-specific version SQL.

## Oracle JDBC URL Rules

Oracle supports two URL modes.

Default service-name mode:

```text
jdbc:oracle:thin:@//host:port/service
```

Legacy SID mode:

```text
jdbc:oracle:thin:@host:port:SID
```

SID mode is selected by setting the connection property:

```text
oracle.connectionMode=sid
```

If `jdbcUrl` is explicitly provided in `DbConnectionConfig`, the provider uses it as-is and does not build a URL from host, port, or database fields.

## PostgreSQL JDBC URL Rules

PostgreSQL uses the standard URL format:

```text
jdbc:postgresql://host:port/database
```

If `jdbcUrl` is explicitly provided in `DbConnectionConfig`, the provider uses it as-is and does not build a URL from host, port, or database fields.

## Health Check SQL

| Engine | SQL |
| --- | --- |
| Oracle | `SELECT 1 FROM dual` |
| PostgreSQL | `SELECT 1` |

## Version SQL

| Engine | SQL |
| --- | --- |
| Oracle | `SELECT version_full FROM product_component_version WHERE product LIKE 'Oracle Database%'` |
| PostgreSQL | `SHOW server_version` |

Oracle version SQL may need a fallback later if a target Oracle version or privilege model does not expose `version_full`.

## Security Rules

The module must not expose passwords or secrets in:

- logs;
- reports;
- exception messages;
- generated string representations.

SQL text may be included in diagnostics. Parameter values should not be logged by default; parameter count is safer for first implementation.

## Testing Strategy

Unit tests use JUnit 5.

The JDBC implementation is tested with H2 as a test-only dependency. H2 is not currently a production-supported engine.

Oracle and PostgreSQL provider tests are unit tests and do not require live databases. Real database integration tests should be added later and guarded by explicit system properties.

## Next Increment

The next logical increment is database environment checks built on top of the generic DB API:

- DB connection health check;
- DB version check foundation;
- structured environment check result mapping.

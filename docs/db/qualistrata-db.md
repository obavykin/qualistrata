# QualiStrata DB Module

`qualistrata-db` is a generic database access module for QualiStrata-based automated tests.

The module is not Oracle-specific and is not limited to environment validation. It provides a reusable API that consumer projects can use from UI, API, performance, integration, smoke, and regression tests.

## Current MVP Scope

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
- PostgreSQL provider;
- generic DB connection environment check;
- generic DB version environment check;
- safe environment-check diagnostics that do not expose raw driver exception messages.

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
| `com.bavykin.qualistrata.db.environment` | Generic DB environment checks built on top of providers and clients. |

## Basic Usage

Create a connection config:

```text
DbConnectionConfig config = new DbConnectionConfig(
        "main",
        DbType.POSTGRES,
        "localhost",
        5432,
        "reporting",
        "public",
        "tester",
        "<password>",
        null,
        Duration.ofSeconds(10),
        Duration.ofSeconds(30),
        Map.of()
);
```

Create a provider and a client:

```text
DbProvider provider = new PostgresDbProvider();

try (DbClient db = provider.createClient(config)) {
    String status = db.queryValue(
            "SELECT status FROM orders WHERE order_id = ?",
            String.class,
            orderId
    );
}
```

Execute an update:

```text
db.update(
        "UPDATE users SET role = ? WHERE username = ?",
        "ADMIN",
        username
);
```

Execute a transaction:

```text
db.transaction(tx -> {
    tx.update("INSERT INTO test_customer(id, name) VALUES (?, ?)", id, name);
    tx.update("INSERT INTO test_account(customer_id, type) VALUES (?, ?)", id, "PREMIUM");
});
```

Run a generic connection check:

```text
DbProviderRegistry registry = new DefaultDbProviderRegistry(List.of(
        new OracleDbProvider(),
        new PostgresDbProvider()
));

DbEnvironmentCheckResult result =
        new DbConnectionEnvironmentCheck(registry).execute(config);
```

Run a generic version check:

```text
DbEnvironmentCheckResult result =
        new DbVersionEnvironmentCheck(registry).execute(config, "16.3");
```

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

## Environment Check Diagnostics

Environment check results are intended to be written into generated reports.

For that reason, failure messages must remain high-level and must not include raw JDBC or driver exception messages. Driver messages may contain connection URLs, usernames, host names, SQL fragments, or other infrastructure details.

Current failure result policy:

- `message` contains only a stable high-level check failure message;
- `details.exceptionType` contains the exception class name;
- `details.causeType` may contain the cause class name;
- raw exception message is not exposed in the result.

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

The next logical increment is guarded database integration tests:

- Oracle integration check guarded by explicit system properties;
- PostgreSQL integration check guarded by explicit system properties;
- skipped by default during normal `build`;
- no dependency on live DB infrastructure unless explicitly enabled.

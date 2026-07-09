# QualiStrata DB Environment Checks

This document describes generic database environment checks built on top of the `qualistrata-db` provider and client API.

## Scope

Included:

- `DbConnectionEnvironmentCheck`
- `DbVersionEnvironmentCheck`
- `DbEnvironmentCheckResult`
- `DbEnvironmentCheckStatus`
- package documentation
- unit tests with fake providers and fake clients
- safe failure diagnostics that avoid raw driver exception messages

Excluded for now:

- project-specific reporting integration
- JSON/HTML report writers
- real Oracle/PostgreSQL integration tests
- SQL-based custom checks from YAML
- advanced version matchers

## Architecture

The checks are implemented above the provider/client layer:

```text
DbConnectionEnvironmentCheck / DbVersionEnvironmentCheck
  -> DbProviderRegistry
  -> DbProvider
  -> DbClient
```

They do not branch on Oracle or PostgreSQL directly. Provider-specific behavior stays in providers.

## Connection Check

`DbConnectionEnvironmentCheck` resolves the provider, creates a client, and executes:

```text
provider.defaultHealthCheckSql()
```

For example:

```text
Oracle:      SELECT 1 FROM dual
PostgreSQL: SELECT 1
```

## Version Check

`DbVersionEnvironmentCheck` resolves the provider, creates a client, and executes:

```text
provider.databaseVersionSql()
```

It supports two modes:

```text
read-only mode:
  report actual version only

exact baseline mode:
  compare actual version with expected version
```

Advanced matching through `EnvironmentValueMatcher` can be added later.

## Safe Failure Diagnostics

Environment check results are reportable artifacts, so they should not expose raw JDBC driver messages.

Failure results use this policy:

```text
message:
  high-level check failure only

details.exceptionType:
  Java exception class name

details.causeType:
  Java cause class name when available
```

The original exception message is intentionally not copied to `message` or `details` because it may include connection URLs, usernames, host names, or other infrastructure details.

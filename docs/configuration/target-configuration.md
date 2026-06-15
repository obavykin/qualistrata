# Target Configuration

QualiStrata uses target configuration files to describe the technical environment under test.

The target configuration model is intentionally generic. It describes reusable access points such as UI, API, database, Unix hosts, and performance settings. Product-specific page objects, business flows, credentials, test data, environment naming conventions, and orchestration rules must live outside reusable framework modules.

## Purpose

Target configuration exists to answer a simple question:

> Which technical environment should QualiStrata use for the current test run?

A target may describe:

* application UI entry point;
* API entry point;
* database connection details;
* Unix or infrastructure hosts;
* generic performance and telemetry settings.

The target configuration model belongs to `qualistrata-core` because all other framework modules may need access to the same resolved environment information.

## Configuration Layers

QualiStrata distinguishes between **committed target configuration** and **resolved runtime configuration**.

Committed target configuration contains non-secret technical information that is safe to store in a project repository.

Resolved runtime configuration is the effective configuration after one or more override or secret layers have been merged.

A typical consuming project may use layers like this:

```text
public target YAML
  + local uncommitted secrets YAML
  + target-specific local secrets YAML
  + explicit secrets file
  + system properties or environment variable overrides
  = resolved runtime TargetConfig
```

QualiStrata core provides generic models, loading, and merging primitives. The consuming project decides which layers exist, where they are stored, and how secrets are resolved.

## Public Target Configuration

Public target configuration should contain only open, non-secret environment information.

It may contain:

* logical environment name;
* UI base URL when it is safe to expose;
* API base URL when it is safe to expose;
* database host, port, SID, service, and username when safe to expose;
* Unix host aliases;
* host roles;
* hostnames or placeholders;
* SSH ports;
* logical user aliases;
* usernames;
* key paths;
* non-sensitive timeout or pool settings.

It must not contain:

* real passwords;
* private keys;
* API tokens;
* database passwords;
* CI/CD tokens;
* customer-sensitive hostnames;
* proprietary environment details;
* logs or reports containing sensitive information.

## Resolved Runtime Configuration

Resolved runtime configuration may contain sensitive values after public configuration and secret layers have been merged.

For example, a public target file may define:

```yaml
users:
  app:
    username: "app"
    keyPath: "/home/app/.ssh/id_rsa"
```

A local uncommitted secrets file may provide:

```yaml
hosts:
  app:
    users:
      app:
        password: "<resolved outside source control>"
```

After merging, the effective runtime `UserConfig` may contain:

```text
username = app
keyPath  = /home/app/.ssh/id_rsa
password = value resolved from a secure source
```

This is why `UserConfig` may have a `password` field in the core model: it represents the effective user configuration after layers are merged. Real password values must come only from secure runtime layers and must not be stored in committed public target YAML files.

## Merge Rules

QualiStrata uses deterministic deep merge rules for target configuration.

The general rule is:

> Override values win only when they are explicitly non-null.

Null values in an override layer do not erase values from the base layer.

Example:

```yaml
# Base layer
ui:
  baseUrl: "https://app.demo.test"
```

```yaml
# Override layer
ui:
  baseUrl:
```

The resolved value remains:

```text
ui.baseUrl = https://app.demo.test
```

But if the override layer contains a non-null value:

```yaml
ui:
  baseUrl: "https://app.override.test"
```

then the resolved value becomes:

```text
ui.baseUrl = https://app.override.test
```

## Deep Merge Policy

Deep merge is the intended behavior for all target configuration sections.

Current and future sections should follow this rule:

| Section | Merge behavior                                                                 |
| ------- | ------------------------------------------------------------------------------ |
| `env`   | Non-null override value wins.                                                  |
| `ui`    | Fields are merged individually.                                                |
| `api`   | Fields are merged individually when generic API fields are introduced.         |
| `db`    | Fields are merged individually.                                                |
| `perf`  | Fields are merged individually when generic performance fields are introduced. |
| `hosts` | Hosts are merged by logical host alias.                                        |
| `users` | Users are merged by logical user alias.                                        |
| `pool`  | Pool fields are merged individually.                                           |

Some sections, such as `api` and `perf`, may be minimal or empty in early versions. They still follow the all-sections-deep-merge design and are expected to evolve without changing the overall configuration model.

## Example Public Target YAML

The following example is safe for documentation because it uses placeholder `.test` hosts and contains no real secrets.

```yaml
env: demo

ui:
  baseUrl: "https://app.demo.test"

api: {}

db:
  host: "db.demo.test"
  port: 1521
  sid: "DEMO"
  service:
  username: "demo_user"

perf: {}

hosts:
  app:
    role: "application"
    host: "app.demo.test"
    port: 22
    defaultUser: "app"
    users:
      app:
        username: "app"
        keyPath: "/home/app/.ssh/id_rsa"
    pool:
      size: 5
      keepAliveSeconds: 60
      borrowTimeoutSeconds: 10
    commandTimeoutSeconds: 60

  db:
    role: "database"
    host: "db.demo.test"
    port: 22
    defaultUser: "oracle"
    users:
      oracle:
        username: "oracle"
        keyPath: "/home/oracle/.ssh/id_rsa"
    pool:
      size: 3
      keepAliveSeconds: 60
      borrowTimeoutSeconds: 10
    commandTimeoutSeconds: 120
```

The `.test` top-level domain is intentionally used for examples and documentation. It should not be used as a real public production domain.

## Example Local Secrets YAML

A local secrets file must not be committed to source control.

Example:

```yaml
hosts:
  app:
    users:
      app:
        password: "<local-app-password>"

  db:
    users:
      oracle:
        password: "<local-oracle-password>"
```

This file should be ignored by Git.

Recommended local paths for consuming projects may include:

```text
config/secrets.yml
config/secrets/<target>.yml
```

These paths are examples only. QualiStrata core must not require a specific repository layout.

## Sections

| Section | Purpose                                                      |
| ------- | ------------------------------------------------------------ |
| `env`   | Logical target or environment name.                          |
| `ui`    | UI access settings.                                          |
| `api`   | API access settings.                                         |
| `db`    | Database access settings.                                    |
| `perf`  | Performance and telemetry-related settings.                  |
| `hosts` | Unix or infrastructure hosts available to framework modules. |

## `env`

The `env` field is a logical name of the configured target.

Example:

```yaml
env: demo
```

The framework must not attach product-specific meaning to this value. It is a label that can be used in logs, reports, metrics, and diagnostics.

QualiStrata core must not hardcode fallback target names such as `release_6.6`, `dev`, or `local`. Default target selection belongs to the consuming project.

## `ui`

The `ui` section contains generic UI access settings.

Example:

```yaml
ui:
  baseUrl: "https://app.demo.test"
```

| Field     | Description                             |
| --------- | --------------------------------------- |
| `baseUrl` | Base URL of the application under test. |

Product-specific UI behavior must not be configured here. Page objects, navigation components, login flows, and business actions belong to project-specific modules.

## `api`

The `api` section is reserved for reusable API access settings.

Example:

```yaml
api: {}
```

The section is currently intentionally minimal. It exists so that the configuration format can evolve without changing the high-level target structure.

Future fields may include generic API base URLs, timeouts, proxy settings, or authentication-provider references. Product-specific API clients and endpoint definitions must remain outside `qualistrata-core`.

## `db`

The `db` section contains generic database connection details.

Example:

```yaml
db:
  host: "db.demo.test"
  port: 1521
  sid: "DEMO"
  service:
  username: "demo_user"
```

| Field      | Description                   |
| ---------- | ----------------------------- |
| `host`     | Database host.                |
| `port`     | Database port.                |
| `sid`      | Optional Oracle SID.          |
| `service`  | Optional Oracle service name. |
| `username` | Non-secret database username. |

For Oracle connections, project or database modules should follow this rule:

* if `service` is configured, use a service-style JDBC URL;
* otherwise, if `sid` is configured, use a SID-style JDBC URL;
* if neither is configured, fail fast with a clear configuration error.

Passwords must not be stored in public target files. They should be resolved through a dedicated secrets mechanism.

## `perf`

The `perf` section is reserved for performance and telemetry-related settings.

Example:

```yaml
perf: {}
```

The section is currently intentionally minimal. Future reusable settings may include:

* metrics collection enabled/disabled flag;
* local metrics reporting flag;
* optional InfluxDB publishing flag;
* strict/non-strict publishing behavior;
* generic metric tags.

Product-specific performance scenarios, thresholds, and business metric names must not live in `qualistrata-core`.

## `hosts`

The `hosts` section describes Unix or infrastructure hosts by logical alias.

Example:

```yaml
hosts:
  app:
    role: "application"
    host: "app.demo.test"
    port: 22
    defaultUser: "app"
    users:
      app:
        username: "app"
        keyPath: "/home/app/.ssh/id_rsa"
```

| Field                   | Description                                                             |
| ----------------------- | ----------------------------------------------------------------------- |
| `role`                  | Logical host role, for example `application`, `database`, or `utility`. |
| `host`                  | Hostname or IP address.                                                 |
| `port`                  | Connection port.                                                        |
| `defaultUser`           | Logical user key used by default for this host.                         |
| `users`                 | Map of logical user definitions.                                        |
| `pool`                  | Optional Unix connection pool settings.                                 |
| `commandTimeoutSeconds` | Optional default command execution timeout for this host.               |

The host alias, such as `app` or `db`, is a logical key. Framework and project modules should use this alias instead of duplicating hostnames across tests.

## Users

Each host can define one or more logical users.

Public target configuration should contain only non-secret user information.

Example:

```yaml
users:
  oracle:
    username: "oracle"
    keyPath: "/home/oracle/.ssh/id_rsa"
```

| Field      | Description                          |
| ---------- | ------------------------------------ |
| `username` | Login name used for the target user. |
| `keyPath`  | Optional path to a private key file. |

Passwords must not be stored in committed target configuration files.

At runtime, project-specific secret resolution may enrich user configuration with passwords loaded from system properties, environment variables, local `secrets.yml`, CI/CD secret storage, Vault, or another dedicated secrets mechanism.

## Unix Pool

The `pool` section defines generic Unix connection reuse settings.

Example:

```yaml
pool:
  size: 5
  keepAliveSeconds: 60
  borrowTimeoutSeconds: 10
```

| Field                  | Description                                                                                   |
| ---------------------- | --------------------------------------------------------------------------------------------- |
| `size`                 | Maximum number of reusable Unix connections managed by the pool.                              |
| `keepAliveSeconds`     | Number of seconds an idle connection may remain alive before validation, refresh, or closure. |
| `borrowTimeoutSeconds` | Maximum number of seconds to wait when borrowing a connection from the pool.                  |

Pool configuration is merged field by field. An override layer may change only one setting without repeating the whole pool configuration.

## Secret Resolution

QualiStrata core does not own project secrets.

A consuming project may resolve secrets from:

* local uncommitted secret files;
* environment variables;
* system properties;
* CI/CD secret storage;
* Vault or another secret-management system;
* project-specific credential providers.

The core framework should provide reusable primitives for loading and merging configuration, but it must not assume a specific secret storage mechanism.

## Public Repository Rules

The public QualiStrata repository must contain only generic examples.

Public configuration examples may use:

```yaml
env: demo
host: "app.demo.test"
username: "demo_user"
keyPath: "/home/demo/.ssh/id_rsa"
```

Public configuration examples must not use:

```yaml
env: release_6.6
host: "real-customer-host.example.com"
username: "real_user"
password: "real_password"
```

## Loading Target Configuration

`qualistrata-core` provides a generic target configuration loader.

The loader accepts an explicit file path and parses it into the target configuration model. It does not decide which environment should be used and does not provide product-specific defaults.

Project-specific modules are responsible for:

* deciding which target name is active;
* resolving where target files are stored;
* resolving which secret layers should be applied;
* validating project-specific required fields;
* creating project-specific runtime objects from the generic model.

## Merging Target Configuration

`qualistrata-core` provides a generic target configuration merger.

The merger accepts already loaded `TargetConfig` objects and combines them according to the deep merge rules.

It does not:

* load files;
* read system properties;
* read environment variables;
* decide the active target;
* resolve secrets directly;
* know product-specific environment names;
* know repository-specific configuration paths.

This separation allows consuming projects to build their own resolution pipeline while reusing generic merge behavior.

## Recommended Project Layout

A project that uses QualiStrata may choose a layout like this:

```text
project-root/
├── config/
│   ├── targets/
│   │   ├── demo.yml
│   │   ├── staging.yml
│   │   └── production-like.yml
│   ├── secrets.yml              # local only, must be ignored by Git
│   └── secrets/
│       └── demo.yml             # local only, must be ignored by Git
├── src/
└── build.gradle.kts
```

This is only a recommendation. QualiStrata core must not require a specific repository layout.

## Runtime Target Selection

A project-specific module may select the active target using:

* system property;
* environment variable;
* CI/CD parameter;
* local developer configuration;
* explicit test code.

Example project-level convention:

```text
-Dtarget=demo
```

or:

```text
TARGET=demo
```

The reusable framework should not hardcode a fallback such as `release_6.6`, `dev`, or `local`. Such defaults belong to the consuming project, not to QualiStrata core.

## Validation Responsibility

The generic configuration model stores values as loaded and merged.

Validation should be separated by responsibility:

| Responsibility                   | Owner                                                   |
| -------------------------------- | ------------------------------------------------------- |
| YAML parsing                     | `qualistrata-core`                                      |
| Generic in-memory merging        | `qualistrata-core`                                      |
| Generic structural validation    | `qualistrata-core` or generic validation utilities      |
| Secret source selection          | consuming project                                       |
| Secret value resolution          | consuming project or dedicated secret provider          |
| Product-specific required fields | consuming project                                       |
| Environment baseline validation  | consuming project or project-specific validation module |
| Connection checks                | framework integration modules and consuming project     |

This separation keeps the core framework reusable and avoids embedding product assumptions into generic modules.

## Common Mistakes

### Storing real secrets in target files

Do not commit real secrets. Use a secret provider or local uncommitted secrets file.

### Showing password fields in public target examples

Public target YAML examples should not show password fields. Passwords belong to secret layers.

### Hardcoding product-specific target names in the framework

Do not add default target names to `qualistrata-core`.

### Putting page object settings into target configuration

Page object and navigation details belong to project-specific modules.

### Using target configuration as test data

Target configuration describes the environment, not business test data.

### Mixing public examples with customer data

Public examples must use safe placeholder values only.

## Summary

Target configuration is the generic description of the environment under test.

It should be:

* reusable;
* explicit;
* safe to commit when using placeholders;
* free from product-specific assumptions;
* separated from secrets;
* resolved by project-specific code before test execution.

QualiStrata core provides generic model, loading, and merging capabilities. Consuming projects decide how targets are selected, how secrets are resolved, and how product-specific validation is performed.

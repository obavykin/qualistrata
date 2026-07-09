# QualiStrata Resource / Access / Secrets Model

QualiStrata target configuration is split into two independent models:

```text
target.yml  -> resources / topology / non-secret settings
secrets.yml -> access / credentials / tokens / secret material
```

Secrets are not merged into target resources. A runtime access resolver combines a resource alias with an access alias only when a client/session needs to be created.

## Target shape

```yaml
env: demo

resources:
  ui:
    application:
      type: web-ui
      baseUrl: https://app.demo.test
      defaultAccess: admin

  unix:
    app-host:
      role: application
      host: app.demo.test
      port: 22
      defaultAccess: app-user

  databases:
    main:
      type: oracle
      host: db.demo.test
      port: 1521
      databaseName: DEMO
      connectionMode: sid
      defaultAccess: app-db-user
```

## Secrets shape

```yaml
access:
  ui:
    application:
      admin:
        username: demo-admin
        password: "<ui-admin-password>"

  unix:
    app-host:
      app-user:
        username: app
        password: "<app-ssh-password>"

  databases:
    main:
      app-db-user:
        username: app_schema
        password: "<database-password>"
```

## Rules

1. `target.yml` contains no credentials.
2. `secrets.yml` contains no topology.
3. Resource aliases are consumer-project-defined.
4. Access aliases are consumer-project-defined.
5. QualiStrata core must not hardcode TNT, Oracle-only, or any other consumer-project-specific aliases.
6. Unix users and database users are separate access profiles even if they point to the same physical server.
7. Resolved runtime configs must mask secrets in `toString()`.

## Resolution API

```java
AccessResolver resolver = new AccessResolver(resolvedTarget.config(), resolvedTarget.secrets());

ResolvedUnixHostConfig appHost = resolver.unix("app-host");
ResolvedDatabaseConfig database = resolver.database("main");
ResolvedUiConfig ui = resolver.ui("application", "admin");
ResolvedMetricsConfig metrics = resolver.metrics("automation-metrics");
```

Consumer projects should keep their aliases in their own integration layer, for example `ProjectResources.MAIN_DATABASE = "main"`.

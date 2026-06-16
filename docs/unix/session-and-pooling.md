# Unix Session and Pooling Architecture

QualiStrata Unix module provides reusable contracts for command execution, Unix host sessions, and session pooling.

The module is designed to support fast and reliable Unix automation without forcing test or project code to depend directly on SSH, JSch, connection pools, or any concrete implementation.

## Purpose

Unix automation often needs to execute multiple commands on the same remote host during one test flow.

Opening a new SSH connection for every command is expensive because it may involve:

* network connection setup;
* authentication;
* remote shell or channel creation;
* command execution;
* connection shutdown.

For this reason, session reuse is a first-class architectural requirement in QualiStrata.

## Design Principle

Project and test code should depend on:

```text
UnixSessionProvider
```

and not directly on:

```text
SSH client
JSch
UnixHostSessionFactory
UnixHostSessionPool
concrete pool implementation
```

This keeps pooling transparent for consumers.

Example consumer usage:

```java
try (UnixHostSession session = sessionProvider.open(request)) {
    CommandResult result = session.execute(commandRequest);
}
```

The same consumer code can work with:

* direct non-pooled sessions;
* pooled sessions;
* local diagnostic sessions;
* mock sessions in tests;
* future SSH/JSch-based sessions.

## Architecture Overview

The current Unix architecture is layered as follows:

```text
UnixCommandRequest
  -> describes command and timeout

UnixCommandClient
  -> low-level command execution contract

LocalUnixCommandClient
  -> local ProcessBuilder-based implementation

UnixHostSession
  -> host/user-bound execution context

UnixHostSessionFactory
  -> creates new physical sessions

UnixHostSessionPool
  -> borrows, releases, and invalidates reusable sessions

UnixSessionProvider
  -> consumer-facing session entry point

DirectUnixSessionProvider
  -> opens a new session for every request

PooledUnixSessionProvider
  -> borrows a session from the pool and returns it on close
```

## Command Execution Layer

`UnixCommandClient` is the lowest-level command execution contract.

It accepts a `UnixCommandRequest` and returns `CommandResult`.

A non-zero exit code is a valid command result and must not automatically be treated as a framework exception.

Framework-level exceptions should be reserved for cases such as:

* invalid request;
* infrastructure failure;
* timeout;
* interrupted execution;
* connection failure;
* inability to start or read command output.

## Local Command Client

`LocalUnixCommandClient` executes commands on the machine where tests are running.

It is based on `ProcessBuilder`.

It is useful for:

* local diagnostics;
* unit and integration tests;
* validating command execution contracts;
* reference behavior before SSH implementation is introduced.

Although the module is called `qualistrata-unix`, the local client supports both Unix-like systems and Windows developer machines through a small shell adapter.

## Host Session Layer

`UnixHostSession` represents an open execution context bound to a specific host and user.

A session may represent:

* a physical SSH connection;
* a pooled wrapper around a physical connection;
* a local process execution context;
* a mock session used in tests.

Consumers should use try-with-resources:

```java
try (UnixHostSession session = sessionProvider.open(request)) {
    session.execute(commandRequest);
}
```

## Direct Session Provider

`DirectUnixSessionProvider` opens a new physical session for every request.

It delegates session creation to `UnixHostSessionFactory`.

It does not use pooling.

This provider is useful for:

* diagnostics;
* simple projects;
* fallback mode;
* tests;
* troubleshooting scenarios where pool behavior should be excluded.

## Pooled Session Provider

`PooledUnixSessionProvider` borrows a session from `UnixHostSessionPool`.

It returns a consumer-facing `UnixHostSession` wrapper.

When the wrapper is closed, the borrowed session is released back to the pool.

This means:

```java
session.close();
```

does not necessarily close the physical connection.

For pooled sessions, `close()` means:

```text
return borrowed session to the pool
```

## Pool Lifecycle

`UnixHostSessionPool` defines the reusable session lifecycle:

```text
borrow
release
invalidate
close
```

The intended meaning is:

| Operation    | Purpose                                              |
| ------------ | ---------------------------------------------------- |
| `borrow`     | Get a reusable session for a host/user request.      |
| `release`    | Return a healthy session to the pool.                |
| `invalidate` | Remove a broken or unsafe session from reuse.        |
| `close`      | Close the pool and release all underlying resources. |

## Session Health Checks

Pooled sessions should be checked before reuse.

A session may still exist as an object but no longer be safe to reuse because the remote host closed the connection, authentication state expired, or the underlying transport became broken.

QualiStrata models this with `UnixSessionHealthCheck`.

The default implementation is `OpenUnixSessionHealthCheck`, which treats a session as healthy when it is non-null and open.

The simple pool uses the health check before returning an idle session:

```text
borrow idle session
  -> check health
  -> if healthy: reuse
  -> if unhealthy: close, remove from pool accounting, create replacement when possible
```
Future SSH-specific implementations may provide protocol-level health checks.

Health checks should not treat normal non-zero command exit codes as session failures.

## SSH Connection Abstraction

QualiStrata separates Unix session lifecycle from the concrete SSH library.

The SSH abstraction layer contains:

- `SshConnectionRequest`;
- `SshConnection`;
- `SshConnectionFactory`;
- `SshConnectionException`;
- `SshUnixHostSession`;
- `SshUnixHostSessionFactory`.

The intended chain is:

```text
UnixSessionProvider
  -> DirectUnixSessionProvider or PooledUnixSessionProvider
  -> UnixHostSessionFactory
  -> SshUnixHostSessionFactory
  -> SshConnectionFactory
  -> concrete SSH implementation
```
`SshUnixHostSessionFactory` converts generic `HostConfig` and `UserConfig` into `SshConnectionRequest`.

The concrete SSH implementation is intentionally hidden behind `SshConnectionFactory`.

This allows the framework to support JSch, Apache MINA SSHD, SSHJ, or another SSH transport library later without changing the higher-level Unix session contracts.

## Release vs Invalidate

A healthy session should be released:

```text
borrow -> use -> close -> release
```

A broken session should be invalidated:

```text
borrow -> failure -> invalidate -> close physical session -> remove from pool -> replacement on next borrow
```

Broken sessions must not be returned to the reusable pool.

Examples of situations where a session may need invalidation:

* SSH connection is broken;
* authentication state is invalid;
* command channel cannot be opened;
* session health check fails;
* remote host closes the connection;
* command execution fails due to infrastructure-level session problem.

Normal non-zero command exit codes do not automatically mean the session is broken.

## Invalidatable Sessions

Invalidation is modeled as an optional session capability.

The base `UnixHostSession` contract defines only common session operations:

```text
execute
isOpen
close
```

Pool-aware sessions that support explicit invalidation may implement `InvalidatableUnixHostSession`.

This keeps the base session abstraction simple and avoids forcing every session implementation to support pooling-specific lifecycle operations.

Consumer code may use invalidation when it detects an infrastructure-level session problem:

```java
if (session instanceof InvalidatableUnixHostSession invalidatableSession) {
    invalidatableSession.invalidate();
}
```

For pooled sessions:

```text
close()
  -> releases a healthy borrowed session back to the pool

invalidate()
  -> removes a broken borrowed session from the pool
  -> closes the physical session
  -> allows a replacement session to be created on the next borrow
```

Invalidation must be idempotent from the consumer perspective. Calling `invalidate()` multiple times must not invalidate the same physical session more than once.

Calling `close()` after `invalidate()` must not return the invalidated session to the pool.


## Pool Configuration

Pool settings come from `UnixPoolConfig` in target configuration.

Current generic pool fields are:

| Field                  | Purpose                                                        |
| ---------------------- | -------------------------------------------------------------- |
| `size`                 | Maximum number of reusable sessions.                           |
| `keepAliveSeconds`     | Idle session keep-alive or validation interval.                |
| `borrowTimeoutSeconds` | Maximum time to wait for a session when the pool is exhausted. |

The exact behavior belongs to the concrete pool implementation.

## Consumer Responsibility

Consumers should:

* depend on `UnixSessionProvider`;
* use try-with-resources;
* treat non-zero exit code as command result;
* avoid managing SSH connections directly;
* avoid manually opening and closing physical sessions unless implementing infrastructure code.

Consumers should not:

* depend directly on JSch or SSH implementation classes;
* bypass `UnixSessionProvider` when pooling is expected;
* return broken sessions to the pool;
* assume that `session.close()` always physically closes the connection.

## Implementation Responsibility

Concrete Unix implementations should:

* validate session requests before opening or borrowing sessions;
* respect `UnixPoolConfig`;
* keep pooling transparent for consumers;
* return healthy sessions to the pool;
* invalidate unsafe sessions;
* close all physical resources when the provider or pool is closed;
* provide clear diagnostics for connection and session failures.

## JSch SSH Implementation

QualiStrata provides a JSch-based SSH transport implementation using the maintained `com.github.mwiede:jsch` fork.

The implementation includes:

* `JschSshConnectionFactory`;
* `JschSshConnection`.

The implementation is hidden behind the generic SSH abstraction:

```text
SshConnectionFactory
  -> JschSshConnectionFactory

SshConnection
  -> JschSshConnection
```

Higher-level Unix session code should still depend on generic contracts:

```text
UnixSessionProvider
UnixHostSessionFactory
SshConnectionFactory
```

and not directly on JSch classes.

`JschSshConnectionFactory` opens connected SSH sessions from `SshConnectionRequest`.

`JschSshConnection` executes commands through JSch exec channels and returns `CommandResult`.

A non-zero command exit code is returned as a normal command result.

Transport-level failures are reported as `SshConnectionException`.

Regular unit tests do not open real SSH connections. Real SSH connectivity tests must be implemented as explicit integration tests with external infrastructure configuration.

## Unix Session Provider Factory

QualiStrata provides `UnixSessionProviderFactory` as a high-level entry point for creating Unix session providers.

The factory hides low-level wiring between:

* `SshConnectionFactory`;
* `SshUnixHostSessionFactory`;
* `SimpleUnixHostSessionPool`;
* `DirectUnixSessionProvider`;
* `PooledUnixSessionProvider`.

This allows consuming projects to depend on the generic `UnixSessionProvider` contract instead of assembling the full object graph manually.

### Default JSch-backed Factory

The default constructor uses the JSch-based SSH transport implementation.

```java
UnixSessionProviderFactory factory = new UnixSessionProviderFactory();

UnixSessionProvider provider = factory.create(UnixSessionProviderType.POOLED);
```

This creates a pooled Unix session provider backed by the following chain:

```text
UnixSessionProvider
  -> PooledUnixSessionProvider
  -> SimpleUnixHostSessionPool
  -> SshUnixHostSessionFactory
  -> JschSshConnectionFactory
```

### Direct Provider

Use a direct provider when each request should open a new physical Unix session.

```java
UnixSessionProviderFactory factory = new UnixSessionProviderFactory();

UnixSessionProvider provider = factory.createDirect();
```

Equivalent typed creation:

```java
UnixSessionProvider provider = factory.create(UnixSessionProviderType.DIRECT);
```

A direct provider is simple and predictable, but it does not reuse physical SSH sessions.

### Pooled Provider

Use a pooled provider when physical Unix sessions should be reused.

```java
UnixSessionProviderFactory factory = new UnixSessionProviderFactory();

UnixSessionProvider provider = factory.createPooled();
```

Equivalent typed creation:

```java
UnixSessionProvider provider = factory.create(UnixSessionProviderType.POOLED);
```

A pooled provider is usually preferred for repeated command execution against the same hosts because it avoids opening a new SSH connection for every operation.

### Custom SSH Connection Factory

Consumers may provide a custom `SshConnectionFactory` while still using QualiStrata's standard SSH-backed Unix session implementation.

```java
SshConnectionFactory connectionFactory = new JschSshConnectionFactory();

UnixSessionProviderFactory factory = new UnixSessionProviderFactory(connectionFactory);

UnixSessionProvider provider = factory.createPooled();
```

This constructor is useful for alternative SSH transports, test doubles, or custom connection behavior.

### Custom Unix Host Session Factory

The most flexible option is to provide a custom `UnixHostSessionFactory`.

```java
UnixHostSessionFactory hostSessionFactory = request -> {
    // Create and return a UnixHostSession implementation.
};

UnixSessionProviderFactory factory = new UnixSessionProviderFactory(hostSessionFactory);

UnixSessionProvider provider = factory.createPooled();
```

This is useful for tests, local implementations, embedded environments, or future non-SSH Unix session mechanisms.

### Usage Pattern

A typical consumer flow is:

```java
UnixSessionProviderFactory factory = new UnixSessionProviderFactory();

try (UnixSessionProvider provider = factory.createPooled();
     UnixHostSession session = provider.open(request)) {

    CommandResult result = session.execute(new UnixCommandRequest(
            "hostname",
            Duration.ofSeconds(5)
    ));
}
```

The `UnixSessionProvider` owns provider-level resources.

For pooled providers, closing a borrowed `UnixHostSession` returns the physical session to the pool.

Closing the provider closes the underlying pool and all idle physical sessions.

## Unix Host Session Request Factory

QualiStrata provides `UnixHostSessionRequestFactory` for creating `UnixHostSessionRequest` instances from resolved target configuration.

This factory hides host lookup logic from consuming projects.

Instead of manually extracting `HostConfig` from `TargetConfig.hosts()` and creating the request by hand, consumers can create requests by logical host alias.

### Basic Usage

```java
TargetConfig targetConfig = QualiStrataConfig.resolve();

UnixHostSessionRequestFactory requestFactory = new UnixHostSessionRequestFactory(targetConfig);

UnixHostSessionRequest request = requestFactory.create("app");
```

This creates a request for the `app` host and uses the host default user.

The factory expects that `TargetConfig` is already resolved and merged. It does not load files, merge secrets, or perform project-specific validation.

### Explicit User Alias

A consumer may explicitly select a user alias configured under the host:

```java
UnixHostSessionRequest request = requestFactory.create("app", "oracle");
```

When the user alias is not provided, blank, or `null`, the request uses the host default user.

### Full Provider Flow

A typical consumer flow is:

```java
TargetConfig targetConfig = QualiStrataConfig.resolve();

UnixHostSessionRequestFactory requestFactory = new UnixHostSessionRequestFactory(targetConfig);
UnixSessionProviderFactory providerFactory = new UnixSessionProviderFactory();

UnixHostSessionRequest request = requestFactory.create("app");

try (UnixSessionProvider provider = providerFactory.createPooled();
     UnixHostSession session = provider.open(request)) {

    CommandResult result = session.execute(new UnixCommandRequest(
            "hostname",
            Duration.ofSeconds(5)
    ));
}
```

### Error Handling

`UnixHostSessionRequestFactory` fails fast with `UnixSessionException` when:

- target configuration is `null`;
- host alias is `null` or blank;
- target configuration does not contain Unix hosts;
- requested host alias is not configured.

User alias validation is intentionally delegated to `UnixHostSessionRequest` and the existing Unix session request validation layer.

This keeps the request factory focused on target host lookup, while session validation remains centralized in the Unix session layer.

## SSH Integration Test Configuration

QualiStrata includes a guarded JSch SSH integration test that verifies real SSH connectivity and remote command execution.

The test is located under the `integrationTest` source set and is not executed by the regular `build` task.

Run regular build:

```powershell
.\gradlew.bat clean build
```

Run integration tests explicitly:

```powershell
.\gradlew.bat :qualistrata-unix:integrationTest
```

The SSH integration test is guarded by system properties. If the required SSH configuration is not provided, the test is skipped automatically.

Supported properties:

| Property                               |           Required | Description                              |
| -------------------------------------- | -----------------: | ---------------------------------------- |
| `qualistrata.integration.ssh.host`     |                yes | SSH host address or DNS name.            |
| `qualistrata.integration.ssh.port`     |                 no | SSH port. Defaults to `22` when omitted. |
| `qualistrata.integration.ssh.username` |                yes | SSH username.                            |
| `qualistrata.integration.ssh.password` | password auth only | SSH password.                            |
| `qualistrata.integration.ssh.keyPath`  |      key auth only | Path to a private SSH key.               |

At least one authentication method must be provided:

* password-based authentication via `qualistrata.integration.ssh.password`;
* key-based authentication via `qualistrata.integration.ssh.keyPath`.

Password-based example:

```powershell
.\gradlew.bat :qualistrata-unix:integrationTest --rerun-tasks `
  "-Dqualistrata.integration.ssh.host=192.168.1.10" `
  "-Dqualistrata.integration.ssh.port=22" `
  "-Dqualistrata.integration.ssh.username=test-user" `
  "-Dqualistrata.integration.ssh.password=secret-password"
```

Key-based example:

```powershell
.\gradlew.bat :qualistrata-unix:integrationTest --rerun-tasks `
  "-Dqualistrata.integration.ssh.host=192.168.1.10" `
  "-Dqualistrata.integration.ssh.port=22" `
  "-Dqualistrata.integration.ssh.username=test-user" `
  "-Dqualistrata.integration.ssh.keyPath=C:\path\to\private_key"
```

Integration-test secrets must never be committed to the repository.

Use command-line system properties, local untracked configuration, CI secrets, or another secure secret-management mechanism.


## Current Status

The current implementation includes:

* command execution contracts;
* local command execution implementation;
* session contracts;
* direct session provider;
* pooled session provider wrapper;
* session request validation;
* pooling contracts.

The current implementation does not yet include:

* real SSH/JSch session implementation;
* concrete in-memory session pool;
* session health checks;
* automatic invalidation on infrastructure failures.

These will be introduced in later iterations.

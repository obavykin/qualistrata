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

# Unix Component Detectors

## 1. Purpose

QualiStrata provides generic Unix component detectors for validating software versions available in a Unix command environment.

Unix component detectors are used by environment checks to answer questions such as:

```text
Is Python installed on the target host, and does its version match the expected baseline?
Is Apache available, and does its version match the expected baseline?
Is the Oracle Client tool available, and what version does the client binary report?
```

Detectors are generic framework functionality. Consumer projects provide environment-specific baseline configuration, host aliases, user aliases, and shell profiles.

## 2. Responsibility Split

```text
QualiStrata:
  provides detector contracts
  provides built-in generic detectors
  executes detector commands through UnixFacade
  supports optional shell-profile-aware command execution
  maps detector results to EnvironmentCheckResult

Consumer project:
  provides baseline YAML/configuration
  decides which detector id to use
  provides expected versions
  provides Unix host/user/profile information
  may provide custom detectors
  may override built-in detectors
```

QualiStrata must not contain consumer-specific paths, release names, host aliases, or environment assumptions.

## 3. Built-in Detectors

### 3.1 `python`

Detects Python interpreter version.

Command:

```bash
python3 --version
```

Typical output:

```text
Python 3.12.3
```

Example baseline:

```yaml
components:
  python:
    detector: python
    version: "3.12.3"
```

### 3.2 `python-package`

Detects Python package version using `importlib.metadata`.

Command pattern:

```bash
python3 -c "import importlib.metadata as m; print(m.version('<package>'))"
```

Required property:

```text
package
```

Example baseline:

```yaml
components:
  django:
    detector: python-package
    version: "6.0.4"
    properties:
      package: Django
```

### 3.3 `apache`

Detects Apache HTTP Server version.

Command:

```bash
httpd -v || apache2 -v
```

Typical output:

```text
Server version: Apache/2.4.62 (...)
```

Example baseline:

```yaml
components:
  apache:
    detector: apache
    version: "2.4.62"
```

### 3.4 `oracle-client`

Detects Oracle Client tool version using `sqlplus`.

Command:

```bash
sqlplus -versionId
```

Typical real output:

```text
SQL*Plus: Release 19.0.0.0.0 - Production
Version 19.26.0.0.0
```

Example baseline:

```yaml
components:
  oracle-client:
    detector: oracle-client
    version: "19.26.0.0.0"
```

or:

```yaml
components:
  sqlplus:
    detector: oracle-client
    version: "19.26.0.0.0"
```

The component key is consumer-defined. The detector id must be `oracle-client`.

## 4. Oracle Client Detector Limitation

The `oracle-client` detector validates only the version reported by the local Oracle Client command-line tool, currently through:

```bash
sqlplus -versionId
```

It checks the client binary/tool available in the Unix shell environment.

It does **not** connect to an Oracle database.

Therefore, it cannot physically validate the real Oracle Database server version or patch level without database login and SQL execution.

Correct interpretation:

```text
oracle-client:
  validates sqlplus / Oracle Client tool version

oracle database version:
  must be validated by qualistrata-db using DB connection and SQL query
```

Oracle Database server version validation belongs to `qualistrata-db`, not `qualistrata-unix`.

## 5. Shell Profile Behavior

Unix component detectors support optional shell profile sourcing.

When a shell profile is provided, QualiStrata executes detector commands using POSIX dot syntax:

```bash
. '/path/to/profile' >/dev/null 2>&1 && <detector-command>
```

Example:

```bash
. '/home/oracle/.profile' >/dev/null 2>&1 && sqlplus -versionId
```

The profile is sourced in the same shell invocation as the detector command.

Profile stdout and stderr are redirected to `/dev/null` so profile banners or debug output do not pollute command output parsed by detectors.

When shell profile is missing or blank, the detector command is executed unchanged.

## 6. Baseline Example

Example consumer-side baseline:

```yaml
hosts:
  app:
    unix:
      profile: "/tracker/tntpt65b_9010-tnt65/.profile"
      os:
        id: "rhel"
        major: 9
      components:
        apache:
          detector: apache
          version: "2.4.62"
        python:
          detector: python
          version: "3.12.3"
        django:
          detector: python-package
          version: "6.0.4"
          properties:
            package: Django

  db:
    unix:
      profile: "/home/oracle/.profile"
      os:
        id: "rhel"
        major: 9
      components:
        oracle-client:
          detector: oracle-client
          version: "19.26.0.0.0"
```

This YAML structure is an example of a consumer project baseline. QualiStrata does not own this exact file format.

## 7. Extension Model

Consumer projects may provide their own detectors by implementing:

```text
com.bavykin.qualistrata.unix.environment.detector.UnixComponentDetector
```

Custom detectors can be registered in:

```text
com.bavykin.qualistrata.unix.environment.detector.DefaultUnixComponentDetectorRegistry
```

Example:

```text
DefaultUnixComponentDetectorRegistry registry =
        new DefaultUnixComponentDetectorRegistry();

registry.register(new CustomProductVersionDetector());
```

Then pass the registry into:

```text
DetectedUnixComponentEnvironmentCheck
```

This allows a consumer project to use both QualiStrata built-in detectors and project-specific detectors.

## 8. Overriding Built-in Detectors

Registering a detector with an existing detector id replaces the previously registered detector.

Example:

```text
DefaultUnixComponentDetectorRegistry registry =
        new DefaultUnixComponentDetectorRegistry();

registry.register(new CustomOracleClientDetector());
```

If `CustomOracleClientDetector.id()` returns:

```text
oracle-client
```

then it replaces the built-in `oracle-client` detector for checks that use this registry.

This is intentional. Built-in detectors are default behavior, not a closed list.

Consumer projects can override built-in detectors when their environment requires different command execution or parsing logic.

## 9. Detector Result Diagnostics

Detector-based checks should expose useful diagnostics in reports, including:

```text
detector
componentName
hostAlias
userAlias
shellProfile
command
expectedVersion
actualVersion
matched
exitCode
stdout
stderr
```

This is important for debugging environment failures, especially when non-interactive SSH command execution differs from an interactive terminal session.

## 10. Non-Goals

Unix component detectors must not:

```text
hardcode consumer-specific paths
hardcode release names
hardcode host aliases
make shell profile globally mandatory
perform DB-level version checks
replace UnixFacade
load consumer baseline YAML directly
```

Consumer-specific validation rules belong to the consumer project.

For example, TNT may require `unix.profile` for every Unix baseline host, but QualiStrata keeps shell profile optional for generic compatibility.

# Environment Checks

QualiStrata provides a generic environment readiness check foundation.

The goal is to help consuming projects validate that a target environment is ready before smoke, regression, or other live test suites are executed.

Environment checks are designed for live validation and diagnostics. They should not be confused with unit tests.

## Design Principles

Environment checks should follow these principles:

```text
1. Run explicitly.
2. Collect all results.
3. Do not stop after the first failure.
4. Generate diagnostic reports.
5. Let the consuming project decide whether failed checks should block execution.
```

A typical consuming project flow is:

```text
Load target configuration
Create environment checks
Run checks through EnvironmentCheckRunner
Write JSON report
Write HTML report
Fail only after reports are generated, if blocking failures exist
```

## Core Concepts

### EnvironmentCheck

`EnvironmentCheck` is a generic executable check contract.

A check validates one specific readiness condition and returns an `EnvironmentCheckResult`.

```text
EnvironmentCheck check = () -> EnvironmentCheckResult.passed(
        "example.check",
        "Example check",
        "Example check passed."
);
```

Checks should normally convert expected environment problems into failed check results instead of throwing exceptions.

### EnvironmentCheckResult

`EnvironmentCheckResult` represents the result of one check.

It contains:

```text
id
name
status
severity
message
duration
details
error
```

Supported statuses:

```text
PASSED
FAILED
SKIPPED
```

Supported severities:

```text
INFO
WARNING
ERROR
```

By default, a failed check with `ERROR` severity is treated as a blocking failure.

### EnvironmentCheckDefinition

`EnvironmentCheckDefinition` combines check metadata with executable check logic.

It is used by `EnvironmentCheckRunner`.

```text
EnvironmentCheckDefinition.error(
        "unix.app.hostname",
        "Unix app hostname",
        check
);
```

Factory methods are available for common severities:

```text
EnvironmentCheckDefinition.info(...)
EnvironmentCheckDefinition.warning(...)
EnvironmentCheckDefinition.error(...)
```

### EnvironmentCheckRunner

`EnvironmentCheckRunner` executes check definitions sequentially and returns an `EnvironmentCheckSuiteResult`.

```text
EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

EnvironmentCheckSuiteResult suite = runner.run(
        "Target readiness",
        List.of(
                EnvironmentCheckDefinition.error(
                        "unix.app.hostname",
                        "Unix app hostname",
                        check
                )
        )
);
```

The runner also:

```text
adds measured duration when a check does not provide one;
converts unexpected RuntimeException failures into failed check results;
keeps executing remaining checks after failures.
```

### EnvironmentCheckSuiteResult

`EnvironmentCheckSuiteResult` aggregates all check results.

It provides summary methods:

```text
allPassed()
hasFailures()
hasBlockingFailures()
passedCount()
failedCount()
skippedCount()
```

A consuming project can use `hasBlockingFailures()` to decide whether execution should fail after reports are written.

## Unix Command Checks

`UnixCommandEnvironmentCheck` is a reusable Unix command based readiness check.

It executes a command through `UnixFacade`.

Exit code `0` means the check passed.

A non-zero exit code means the check failed.

Infrastructure exceptions are converted into failed check results.

Example:

```text
try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
            unix,
            "unix.app.hostname",
            "Unix app hostname",
            EnvironmentCheckSeverity.ERROR,
            "app",
            "hostname",
            Duration.ofSeconds(5)
    );

    EnvironmentCheckResult result = check.check();
}
```

The check uses the host default user unless an explicit user alias is provided.

Example with explicit user alias:

```text
try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixCommandEnvironmentCheck check = new UnixCommandEnvironmentCheck(
            unix,
            "unix.app.whoami",
            "Unix app whoami",
            EnvironmentCheckSeverity.ERROR,
            "app",
            "oracle",
            "whoami",
            Duration.ofSeconds(5)
    );

    EnvironmentCheckResult result = check.check();
}
```

## Unix Operating System Checks

`UnixOperatingSystemEnvironmentCheck` validates Unix OS metadata from `/etc/os-release`.

It supports:

```text
OS id validation
exact VERSION_ID validation
major version validation
```

Only fields explicitly configured in `UnixOperatingSystemProfile` are validated.

Example:

```text
UnixOperatingSystemProfile expectedOs = new UnixOperatingSystemProfile(
        "rhel",
        9,
        "9.5"
);

try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixOperatingSystemEnvironmentCheck check = new UnixOperatingSystemEnvironmentCheck(
            unix,
            "unix.app.os",
            "Unix app OS",
            EnvironmentCheckSeverity.ERROR,
            "app",
            expectedOs,
            Duration.ofSeconds(5)
    );

    EnvironmentCheckResult result = check.check();
}
```

Exact `versionId` validation is useful when a consuming project requires a strict OS patch baseline.

Major version validation is useful when a broader baseline is acceptable.

If the expected OS profile is missing, the check returns `SKIPPED`.

## Environment Value Matchers

QualiStrata provides generic value matchers for comparing detected environment values with expected baselines.

Value matchers are located in `qualistrata-core` and are independent from Unix, API, DB, UI, or any consuming project.

Available matchers:

```text
EnvironmentValueMatcher.exact(...)
EnvironmentValueMatcher.exactIgnoreCase(...)
EnvironmentValueMatcher.contains(...)
EnvironmentValueMatcher.containsIgnoreCase(...)
EnvironmentValueMatcher.regex(...)
```

Examples:

```text
EnvironmentValueMatcher.exact("Python 3.9.18")
EnvironmentValueMatcher.contains("Python 3.9")
EnvironmentValueMatcher.regex("Python\\s+3\\.9\\.\\d+")
```

Matchers return `EnvironmentValueMatchResult`, which contains:

```text
matched
expected
actual
message
```

These matchers are useful for component version checks, command output checks, API response validation, DB value checks, and other readiness validations.

## Unix Component Checks

`UnixComponentEnvironmentCheck` is a generic command-based component readiness check.

It executes a Unix command through `UnixFacade`, extracts an actual value from the command output, and validates it with an `EnvironmentValueMatcher`.

The check is component-neutral. QualiStrata does not know whether the component is Python, Java, Apache, Oracle, Node, or anything else.

A consuming project defines:

```text
component name
host alias
optional user alias
detection command
output source
expected matcher
severity
```

Basic example:

```text
try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
            unix,
            "unix.app.python",
            "Unix app Python",
            EnvironmentCheckSeverity.ERROR,
            "app",
            "python",
            "python3 --version",
            Duration.ofSeconds(5),
            EnvironmentValueMatcher.contains("Python 3.9")
    );

    EnvironmentCheckResult result = check.check();
}
```

If the command exits with code `0`, the selected output is matched against the expected value.

If the command exits with a non-zero code, the check returns a failed result.

### Output Source

Different tools print version information to different streams.

For example, `python3 --version` usually prints to stdout, while `java -version` often prints to stderr.

`UnixComponentOutputSource` controls which command output is used as the actual value:

```text
STDOUT
STDERR
STDOUT_THEN_STDERR
COMBINED
```

Example for Java:

```text
try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
            unix,
            "unix.app.java",
            "Unix app Java",
            EnvironmentCheckSeverity.ERROR,
            "app",
            null,
            "java",
            "java -version",
            Duration.ofSeconds(5),
            EnvironmentValueMatcher.contains("21.0"),
            UnixComponentOutputSource.STDERR
    );

    EnvironmentCheckResult result = check.check();
}
```

Example using regex:

```text
try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
            unix,
            "unix.app.python",
            "Unix app Python",
            EnvironmentCheckSeverity.ERROR,
            "app",
            "python",
            "python3 --version",
            Duration.ofSeconds(5),
            EnvironmentValueMatcher.regex("Python\\s+3\\.9\\.\\d+")
    );

    EnvironmentCheckResult result = check.check();
}
```

### Explicit User Alias

A component check may use an explicit user alias when component detection must be executed under a specific Unix account.

```text
try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    UnixComponentEnvironmentCheck check = new UnixComponentEnvironmentCheck(
            unix,
            "unix.app.oracle-user",
            "Unix app Oracle user",
            EnvironmentCheckSeverity.ERROR,
            "app",
            "oracle",
            "oracle",
            "whoami",
            Duration.ofSeconds(5),
            EnvironmentValueMatcher.exact("oracle"),
            UnixComponentOutputSource.STDOUT
    );

    EnvironmentCheckResult result = check.check();
}
```

Project-specific Oracle or database client checks may build on this generic mechanism, but QualiStrata itself should not contain product-specific Oracle setup logic.

### Diagnostics

`UnixComponentEnvironmentCheck` adds diagnostic details to the result:

```text
hostAlias
userAlias
componentName
command
outputSource
exitCode
stdout
stderr
expected
actual
matched
exceptionType
```

These details are included in the generic JSON and HTML reports.

## Running a Readiness Suite

A typical readiness suite combines multiple checks.

```text
EnvironmentCheckRunner runner = new EnvironmentCheckRunner();

try (UnixFacade unix = UnixFacade.pooled(targetConfig)) {
    EnvironmentCheckSuiteResult suite = runner.run(
            "Target readiness",
            List.of(
                    EnvironmentCheckDefinition.error(
                            "unix.app.hostname",
                            "Unix app hostname",
                            new UnixCommandEnvironmentCheck(
                                    unix,
                                    "unix.app.hostname",
                                    "Unix app hostname",
                                    EnvironmentCheckSeverity.ERROR,
                                    "app",
                                    "hostname",
                                    Duration.ofSeconds(5)
                            )
                    ),
                    EnvironmentCheckDefinition.error(
                            "unix.app.os",
                            "Unix app OS",
                            new UnixOperatingSystemEnvironmentCheck(
                                    unix,
                                    "unix.app.os",
                                    "Unix app OS",
                                    EnvironmentCheckSeverity.ERROR,
                                    "app",
                                    new UnixOperatingSystemProfile("rhel", 9, "9.5"),
                                    Duration.ofSeconds(5)
                            )
                    ),
                    EnvironmentCheckDefinition.error(
                            "unix.app.python",
                            "Unix app Python",
                            new UnixComponentEnvironmentCheck(
                                    unix,
                                    "unix.app.python",
                                    "Unix app Python",
                                    EnvironmentCheckSeverity.ERROR,
                                    "app",
                                    "python",
                                    "python3 --version",
                                    Duration.ofSeconds(5),
                                    EnvironmentValueMatcher.contains("Python 3.9")
                            )
                    )
            )
    );
}
```

## Report Writers

QualiStrata provides default generic report writers:

```text
JsonEnvironmentCheckReportWriter
HtmlEnvironmentCheckReportWriter
```

Both implement:

```text
EnvironmentCheckReportWriter
```

Example:

```text
Path jsonReport = Path.of("build/reports/environment/environment-checks.json");
Path htmlReport = Path.of("build/reports/environment/environment-checks.html");

new JsonEnvironmentCheckReportWriter().write(suite, jsonReport);
new HtmlEnvironmentCheckReportWriter().write(suite, htmlReport);
```

The writers create parent directories automatically.

The JSON writer is intended for:

```text
CI artifacts
machine-readable diagnostics
future dashboard or trend analysis
troubleshooting
```

The HTML writer is intended for:

```text
human-readable diagnostics
CI artifact browsing
local troubleshooting
```

The default HTML report is standalone and uses embedded CSS.

## Project-Specific Reports

Default report writers are generic.

Consuming projects can replace them when they need:

```text
custom JSON schema
custom HTML layout
project-specific grouping
custom sections
branding
different readiness policy
additional fields
```

A project-specific writer can still implement the generic writer contract:

```text
public class ProjectHtmlEnvironmentReportWriter implements EnvironmentCheckReportWriter {

    @Override
    public void write(EnvironmentCheckSuiteResult suite, Path outputFile) {
        // Convert generic suite result to project-specific report model.
        // Render custom HTML.
    }
}
```

Alternatively, a consuming project may define its own report model and not use the generic writers.

## Recommended Execution Pattern

Live environment validation should follow this pattern:

```text
EnvironmentCheckSuiteResult suite = runner.run(...);

jsonWriter.write(suite, jsonPath);
htmlWriter.write(suite, htmlPath);

if (suite.hasBlockingFailures()) {
    throw new AssertionError(
            "Environment readiness validation failed. See generated reports."
    );
}
```

The important rule is:

```text
Always write reports before failing execution.
```

This ensures that diagnostics are available even when the environment is not ready.

## QualiStrata vs Consuming Project Responsibility

QualiStrata owns generic reusable building blocks:

```text
environment check contracts
environment check result model
environment check runner
generic JSON/HTML writers
generic Unix command checks
generic Unix OS checks
```

A consuming project owns project-specific policy:

```text
which checks to execute
which hosts are required
which commands are used
which failures are blocking
which report paths are used
which custom report schema is needed
how live validation is wired into Gradle/CI
```

This separation keeps QualiStrata product-neutral while still making environment readiness validation reusable across projects.
# QualiStrata UI Module

`qualistrata-ui` is the generic web UI automation runtime layer of QualiStrata.
It provides browser/session lifecycle, driver-independent locators and elements,
base page/component primitives, local UI reports, and execution metadata for CI.

The module is intentionally product-agnostic. It must not contain product roles,
login pages, navigation models, report pages, or business workflows.

## Ownership

`qualistrata-ui` owns:

- browser engine selection;
- browser/session/context lifecycle;
- driver-independent locators;
- driver-independent elements and collections;
- generic page, component, dialog, form, and table primitives;
- new-window and download support;
- UI failure artifact descriptors;
- JSON and HTML UI run report models;
- mutation classification annotations for parallel execution planning.

Consumer projects own:

- login and authentication flow;
- product roles and credentials;
- product page objects;
- navigation components;
- report-specific abstractions such as `TntBaseReportPage`;
- business validations and product-specific waits.

## Important architectural rule

`BaseReportPage` does not belong to `qualistrata-ui`.

Reports are product/domain concepts, not generic browser concepts. The UI module
provides `BasePage`, `BaseComponent`, `DataTableComponent`, `FormComponent`,
`WindowSupport`, and `DownloadSupport`. Consumer projects may compose these into
their own report abstractions.

## Runtime model

Preferred Playwright lifecycle:

```text
Suite / worker
  Browser
    Test / logical user
      BrowserContext
        Page / window / tab
          UiSession
```

Tests and page objects should depend on `UiSession`, `UiElement`, and `UiLocator`
instead of Playwright `Page` or Selenium `WebDriver`.

## Configuration

The module uses resolved UI resources from `qualistrata-core` and applies runtime
overrides from approved properties:

```text
ui.baseUrl
ui.engine
ui.browser
ui.headless
ui.timeout.ms
ui.navigation.timeout.ms
ui.newWindow.timeout.ms
ui.download.timeout.ms
ui.screenshot.on.failure
ui.trace.mode
ui.video.on.failure
ui.max.parallel.sessions
```

Default engine is Playwright with Chromium in headless mode.

## Parallel execution classification

The module provides lightweight annotations that consumer test runners can use:

```text
@NonMutatingUiTest
@MutatingUiTest
@RequiresExclusiveEnvironment
```

Default policy:

- non-mutating tests may run in parallel;
- mutating tests run sequentially unless the consumer project provides isolated data;
- exclusive tests require exclusive environment access.

## Reporting

The module provides report models and local writers for:

```text
build/reports/ui/
  ui-report.json
  ui-report.html
```

Artifact collection is represented by generic descriptors. Playwright screenshot
collection is implemented as the first narrow adapter-specific collector.

## Current implementation increment

Implemented in the first increment:

- generic runtime config;
- generic driver/session/context interfaces;
- generic locators and elements;
- generic base page/component/dialog/table/form classes;
- Playwright driver/session/locator/element implementation;
- Selenium factory extension point with explicit not-implemented failure;
- generic download and new-window support;
- local UI report model and JSON/HTML writers;
- mutation classification annotations and policy;
- unit tests that do not launch a real browser.

## TestNG lifecycle integration

The second implementation increment adds a non-intrusive TestNG listener:

```java
@Listeners(QualiStrataUiTestListener.class)
class SomeUiTest {
    // consumer fixture creates UiDriver / UiBrowserContext / UiSession
}
```

or through TestNG suite XML:

```xml
<listeners>
  <listener class-name="com.bavykin.qualistrata.ui.testng.QualiStrataUiTestListener"/>
</listeners>
```

The listener does not create browsers by itself. It observes sessions registered
in `UiSessionRegistry`, collects diagnostics for failed tests, closes leaked
sessions at the end of every test method, and writes aggregated reports at suite
finish.

Default output:

```text
build/reports/ui/
  ui-report.json
  ui-report.html
  artifacts/
    screenshots/
    traces/
```

The report directory can be changed with:

```text
-Dui.report.dir=build/reports/custom-ui
```

### Failure artifacts

Default failure artifact behaviour:

```text
ui.screenshot.on.failure=true
ui.trace.mode=on_failure
```

Screenshots and Playwright traces are best-effort diagnostics. Artifact
collection failures are intentionally ignored so they never hide the original
functional assertion failure.

### Cleanup rule

`UiSessionRegistry` is thread-local. Playwright sessions created through
`UiBrowserContext.newSession()` and `WindowSupport.waitForNewWindow(...)` are
registered automatically. The listener closes any sessions still registered for
the current TestNG worker thread after each test result.

This protects CI runs from one failed UI test poisoning the next test through an
open page/window. Test fixtures may still close sessions explicitly; explicit
close unregisters the session from the registry.

### Build behaviour

The listener does not enable fail-fast. Consumer Gradle/TestNG configuration
should keep suite execution non-fail-fast so all tests run, all artifacts are
collected, and the build fails only after the full suite result is known.

Recommended Gradle setting:

```kotlin
tasks.register<Test>("uiTest") {
    useTestNG()
    failFast = false
}
```

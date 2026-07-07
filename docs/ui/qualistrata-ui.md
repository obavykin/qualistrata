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
ui.mutation.unclassified.policy
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

## Parallel execution and mutation validation

The third implementation increment formalizes UI mutation classification for CI.
Annotations are the preferred mechanism:

```java
@NonMutatingUiTest
@Test
void userCanOpenDashboard() {
    // read-only navigation/assertions
}

@MutatingUiTest
@Test
void userCanCreateDraftRequest() {
    // changes product data
}

@RequiresExclusiveEnvironment
@Test
void userCanRunEnvironmentWideMaintenanceFlow() {
    // must not run together with any other live UI test
}
```

Equivalent TestNG groups are available for projects that prefer suite XML or
existing group-based selection:

```java
UiTestGroups.NON_MUTATING   // ui-non-mutating
UiTestGroups.MUTATING       // ui-mutating
UiTestGroups.EXCLUSIVE      // ui-exclusive
```

Classification precedence is intentionally conservative:

```text
exclusive > mutating > non-mutating
```

If a test declares more than one mutation type through mixed annotations/groups,
QualiStrata treats this as a configuration error and fails the test before the
browser scenario executes.

### Unclassified test policy

By default, unclassified UI tests are allowed so existing suites can adopt the
model gradually:

```text
-Dui.mutation.unclassified.policy=allow
```

Available values:

```text
allow  - allow unclassified tests and record null mutation type in the report
warn   - allow but print a warning
fail   - fail the test before execution
```

Recommended CI hardening mode once a project has migrated UI tests:

```text
-Dui.mutation.unclassified.policy=fail
```

### Recommended TestNG suite split

Parallel read-only suite:

```xml
<suite name="ui-readonly" parallel="methods" thread-count="4">
  <groups>
    <run>
      <include name="ui-non-mutating"/>
    </run>
  </groups>
</suite>
```

Sequential mutating suite:

```xml
<suite name="ui-mutating" parallel="false">
  <groups>
    <run>
      <include name="ui-mutating"/>
    </run>
  </groups>
</suite>
```

Exclusive environment suite:

```xml
<suite name="ui-exclusive" parallel="false">
  <groups>
    <run>
      <include name="ui-exclusive"/>
    </run>
  </groups>
</suite>
```

### Recommended Gradle tasks

Consumer projects can expose separate tasks for CI:

```kotlin
tasks.register<Test>("uiNonMutatingTest") {
    useTestNG {
        includeGroups("ui-non-mutating")
        listeners.add("com.bavykin.qualistrata.ui.testng.QualiStrataUiTestListener")
    }

    failFast = false
    systemProperty("ui.mutation.unclassified.policy", "fail")
    systemProperty("ui.max.parallel.sessions", System.getProperty("ui.max.parallel.sessions") ?: "4")
}

tasks.register<Test>("uiMutatingTest") {
    useTestNG {
        includeGroups("ui-mutating")
        listeners.add("com.bavykin.qualistrata.ui.testng.QualiStrataUiTestListener")
    }

    failFast = false
    maxParallelForks = 1
    systemProperty("ui.mutation.unclassified.policy", "fail")
}
```

This keeps the fast read-only suite parallel while preserving deterministic
execution for data-changing tests.

## Consumer fixture pattern

`qualistrata-ui` provides an optional TestNG fixture layer for consumer
projects that want a ready-to-use lifecycle instead of writing the same browser
setup code in every module.

The standard lifecycle is:

```text
BeforeClass
  create UiTestRuntime
  launch one heavy browser driver

BeforeMethod
  create isolated browser context
  create primary UiSession for the test

AfterMethod
  close UiSession
  close browser context

AfterClass
  close browser driver
```

The listener remains active as a safety net. If a test fails before the fixture
closes the session, `QualiStrataUiTestListener` can still collect failure
artifacts from `UiSessionRegistry` and close leaked sessions.

### Minimal consumer base class

Consumer projects can extend `AbstractQualiStrataUiTest` directly:

```java
public abstract class ProductUiTest extends AbstractQualiStrataUiTest {

    @Override
    protected UiRuntimeConfig uiRuntimeConfig() {
        ResolvedTarget target = new QualiStrataConfig()
                .resolveTarget(new TargetConfigResolutionRequest(
                        Path.of("."),
                        System.getProperties(),
                        System.getenv(),
                        "local",
                        null
                ));

        ResolvedUiConfig ui = new AccessResolver(target.config(), target.secrets())
                .ui("main");

        return UiRuntimeConfigFactory.from(ui, System.getProperties());
    }
}
```

The exact target resolution helper may differ by consumer project. The important
boundary is that `qualistrata-ui` receives an already resolved generic
`ResolvedUiConfig`; product aliases and secrets remain in `qualistrata-core` and
consumer configuration.

### Example read-only test

```java
@NonMutatingUiTest
public final class HomeSmokeTest extends ProductUiTest {

    @Test(groups = UiTestGroups.NON_MUTATING)
    public void homePageCanBeOpened() {
        session().open("/");

        new ProductHomePage(session())
                .shouldBeOpened();
    }
}
```

A product page object should extend generic `BasePage` or a product-specific base
page created in the consumer module:

```java
public final class ProductHomePage extends BasePage {

    public ProductHomePage(UiSession session) {
        super(session);
    }

    public ProductHomePage shouldBeOpened() {
        titleShouldContain("Home");
        return this;
    }
}
```

### When not to use the base class

The fixture is optional. Consumer projects can still create their own lifecycle
when they need advanced scenarios, such as multiple users in the same test,
custom storage-state reuse, or product-specific login caching. In that case they
should still use the same primitives:

```text
UiTestRuntime
UiTestSession
UiSessionRegistry
QualiStrataUiTestListener
```

This keeps reporting, failure artifacts, and cleanup behaviour consistent across
simple and advanced UI suites.

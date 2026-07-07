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

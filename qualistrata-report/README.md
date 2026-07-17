# qualistrata-report

`qualistrata-report` is the product-agnostic QualiStrata module for standardized test-run reporting.

## Package boundaries

- `annotation` — framework-neutral reporting annotations.
- `model` — immutable test-run, test-result, attempt, failure, attachment, and coverage models.
- `metadata` — metadata-provider contracts and safe metadata handling.
- `runtime` — framework-neutral event handling and per-run/per-test reporting context.
- `writer` — JSON and HTML report writers.
- `aggregation` — parallel-fork fragment aggregation.
- `junit` — JUnit Platform adapter only.
- `testng` — TestNG adapter only.

The neutral packages must not import JUnit, TestNG, Playwright, Selenium, or consumer-project classes. Framework adapters translate native lifecycle events into the common reporting model.

This initial module skeleton deliberately contains no listener registration or public reporting model. Those APIs will be added incrementally together with their unit tests.

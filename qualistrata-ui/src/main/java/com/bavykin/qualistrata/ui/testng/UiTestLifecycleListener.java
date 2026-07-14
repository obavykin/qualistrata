package com.bavykin.qualistrata.ui.testng;

import com.bavykin.qualistrata.ui.artifact.UiArtifact;
import com.bavykin.qualistrata.ui.artifact.UiArtifactCollector;
import com.bavykin.qualistrata.ui.artifact.UiArtifactCollectors;
import com.bavykin.qualistrata.ui.config.BrowserEngine;
import com.bavykin.qualistrata.ui.config.UiBrowserType;
import com.bavykin.qualistrata.ui.driver.UiSession;
import com.bavykin.qualistrata.ui.driver.UiSessionRegistry;
import com.bavykin.qualistrata.ui.reporting.HtmlUiReportWriter;
import com.bavykin.qualistrata.ui.reporting.JsonUiReportWriter;
import com.bavykin.qualistrata.ui.reporting.UiReportWriter;
import com.bavykin.qualistrata.ui.reporting.UiRunReport;
import com.bavykin.qualistrata.ui.reporting.UiStepReport;
import com.bavykin.qualistrata.ui.reporting.UiTestReport;
import com.bavykin.qualistrata.ui.reporting.UiTestStatus;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TestNG listener that creates local QualiStrata UI reports and failure artifacts.
 *
 * <p>The listener is intentionally lightweight and non-intrusive. It does not
 * create browser sessions by itself. Consumer fixtures still own the decision
 * when to create a driver/context/session. The listener observes sessions
 * registered in {@link UiSessionRegistry}, collects diagnostics on failure, and
 * closes any sessions that were not closed by the test fixture.</p>
 */
public class UiTestLifecycleListener implements ITestListener, ISuiteListener, IInvokedMethodListener {

    /** Runtime property that controls the UI report output directory. */
    public static final String REPORT_DIRECTORY_PROPERTY = "ui.report.dir";

    /** Runtime property that controls behaviour for unclassified UI tests. */
    public static final String UNCLASSIFIED_POLICY_PROPERTY = "ui.mutation.unclassified.policy";

    private final List<UiTestReport> tests = new CopyOnWriteArrayList<>();
    private final UiMutationClassifier mutationClassifier = new UiMutationClassifier();
    private final UiMutationClassificationValidator mutationValidator;
    private final Properties properties;
    private final Path reportDirectory;
    private final UiArtifactCollector artifactCollector;
    private final List<UiReportWriter> reportWriters;
    private final String runId;
    private Instant startedAt;

    /** Creates listener using current system properties and local JSON/HTML writers. */
    public UiTestLifecycleListener() {
        this(System.getProperties());
    }

    /**
     * Creates listener with explicit properties, useful for unit tests.
     *
     * @param properties runtime override properties.
     */
    public UiTestLifecycleListener(Properties properties) {
        this(
                properties,
                outputDirectory(properties),
                UiArtifactCollectors.localFailureCollectors(outputDirectory(properties), properties),
                List.of(new JsonUiReportWriter(), new HtmlUiReportWriter())
        );
    }

    /**
     * Creates listener with explicit collaborators, useful for tests and custom runners.
     *
     * @param properties runtime override properties.
     * @param reportDirectory directory where UI reports are written.
     * @param artifactCollector collector used for test artifacts.
     * @param reportWriters writers used to persist UI reports.
     */
    public UiTestLifecycleListener(
            Properties properties,
            Path reportDirectory,
            UiArtifactCollector artifactCollector,
            List<UiReportWriter> reportWriters
    ) {
        this.properties = properties == null ? new Properties() : properties;
        this.mutationValidator = new UiMutationClassificationValidator(
                mutationClassifier,
                UiUnclassifiedTestPolicy.from(this.properties.getProperty(UNCLASSIFIED_POLICY_PROPERTY))
        );
        this.reportDirectory = reportDirectory == null ? Path.of("build", "reports", "ui") : reportDirectory;
        this.artifactCollector = artifactCollector == null
                ? UiArtifactCollectors.localFailureCollectors(this.reportDirectory, this.properties)
                : artifactCollector;
        this.reportWriters = reportWriters == null ? List.of() : List.copyOf(reportWriters);
        this.runId = UUID.randomUUID().toString();
    }

    @Override
    public void onStart(ISuite suite) {
        startedAt = Instant.now();
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method == null || !method.isTestMethod()) {
            return;
        }

        Method javaMethod = method.getTestMethod() == null
                ? null
                : method.getTestMethod().getConstructorOrMethod().getMethod();
        if (javaMethod != null) {
            mutationValidator.validate(javaMethod, method.getTestMethod().getGroups());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        record(result, UiTestStatus.PASSED, null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        record(result, UiTestStatus.FAILED, result.getThrowable());
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        record(result, UiTestStatus.SKIPPED, result.getThrowable());
    }

    @Override
    public void onFinish(ISuite suite) {
        UiRunReport report = buildReport();
        for (UiReportWriter writer : reportWriters) {
            writer.write(report, reportDirectory);
        }
    }

    /**
     * Provides the tests operation.
     *
     * @return immutable snapshot of test reports collected so far.
     */
    public List<UiTestReport> tests() {
        return List.copyOf(tests);
    }

    /**
     * Provides the buildReport operation.
     *
     * @return aggregated report built from currently collected test entries.
     */
    public UiRunReport buildReport() {
        Instant started = startedAt == null ? Instant.now() : startedAt;
        Instant now = Instant.now();
        List<UiTestReport> orderedTests = tests.stream()
                .sorted(Comparator.comparing(UiTestReport::startedAt))
                .toList();

        return new UiRunReport(
                runId,
                value("target", ""),
                BrowserEngine.from(value("ui.engine", null)),
                UiBrowserType.from(value("ui.browser", null)),
                bool("ui.headless", true),
                started,
                Duration.between(started, now),
                orderedTests
        );
    }

    private void record(ITestResult result, UiTestStatus status, Throwable failure) {
        List<UiArtifact> artifacts = collectArtifacts(status, result, failure);
        RuntimeException cleanupFailure = UiSessionRegistry.closeAllForCurrentThread().orElse(null);

        Throwable finalFailure = failure == null ? cleanupFailure : failure;
        if (finalFailure != null && cleanupFailure != null && finalFailure != cleanupFailure) {
            finalFailure.addSuppressed(cleanupFailure);
        }

        Method method = result.getMethod() == null ? null : result.getMethod().getConstructorOrMethod().getMethod();
        UiMutationType mutationType = method == null
                ? null
                : mutationClassifier.classify(method, result.getMethod() == null ? new String[0] : result.getMethod().getGroups());
        Instant testStartedAt = Instant.ofEpochMilli(result.getStartMillis());
        long endMillis = result.getEndMillis() > 0 ? result.getEndMillis() : System.currentTimeMillis();

        tests.add(new UiTestReport(
                result.getTestClass() == null ? "" : result.getTestClass().getName(),
                result.getMethod() == null ? "" : result.getMethod().getMethodName(),
                status,
                testStartedAt,
                Duration.ofMillis(Math.max(0, endMillis - result.getStartMillis())),
                result.getMethod() == null ? List.of() : Arrays.asList(result.getMethod().getGroups()),
                mutationType == null ? null : mutationType.name(),
                artifacts,
                List.<UiStepReport>of(),
                summarize(finalFailure)
        ));
    }

    private List<UiArtifact> collectArtifacts(UiTestStatus status, ITestResult result, Throwable failure) {
        if (status != UiTestStatus.FAILED) {
            return List.of();
        }

        return UiSessionRegistry.activeSession()
                .map(session -> artifactCollector.collectOnFailure(session, testName(result), failure))
                .orElseGet(List::of);
    }

    private String testName(ITestResult result) {
        String className = result.getTestClass() == null ? "unknown-class" : result.getTestClass().getName();
        String methodName = result.getMethod() == null ? "unknown-method" : result.getMethod().getMethodName();
        return className + "." + methodName;
    }

    private String summarize(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        parts.add(throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        for (Throwable suppressed : throwable.getSuppressed()) {
            parts.add("suppressed " + suppressed.getClass().getSimpleName() + ": " + suppressed.getMessage());
        }
        return String.join(" | ", parts);
    }

    private String value(String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean bool(String key, boolean fallback) {
        String value = value(key, null);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static Path outputDirectory(Properties properties) {
        String configured = properties == null ? null : properties.getProperty(REPORT_DIRECTORY_PROPERTY);
        return configured == null || configured.isBlank()
                ? Path.of("build", "reports", "ui")
                : Path.of(configured);
    }
}

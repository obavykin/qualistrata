package com.bavykin.qualistrata.ui.junit;

import com.bavykin.qualistrata.ui.artifact.UiArtifact;
import com.bavykin.qualistrata.ui.artifact.UiArtifactCollector;
import com.bavykin.qualistrata.ui.artifact.UiArtifactCollectors;
import com.bavykin.qualistrata.ui.config.BrowserEngine;
import com.bavykin.qualistrata.ui.config.UiBrowserType;
import com.bavykin.qualistrata.ui.driver.UiSessionRegistry;
import com.bavykin.qualistrata.ui.reporting.HtmlUiReportWriter;
import com.bavykin.qualistrata.ui.reporting.JsonUiReportWriter;
import com.bavykin.qualistrata.ui.reporting.UiReportWriter;
import com.bavykin.qualistrata.ui.reporting.UiRunReport;
import com.bavykin.qualistrata.ui.reporting.UiStepReport;
import com.bavykin.qualistrata.ui.reporting.UiTestReport;
import com.bavykin.qualistrata.ui.reporting.UiTestStatus;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JUnit 5 extension that integrates UI test reporting, artifacts, and cleanup.
 *
 * <p>The extension mirrors the TestNG listener semantics without forcing JUnit
 * projects to adopt TestNG. It does not create browser sessions by itself.
 * Consumer fixtures own runtime/session creation; the extension observes
 * sessions registered in {@link UiSessionRegistry}, collects diagnostics on
 * failure, closes leaked sessions, and writes an aggregated local UI report.</p>
 */
public class QualiStrataJunitUiExtension implements BeforeAllCallback, BeforeEachCallback,
        BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterEachCallback,
        AfterAllCallback, TestWatcher {

    /** Runtime property that controls the UI report output directory. */
    public static final String REPORT_DIRECTORY_PROPERTY = "ui.report.dir";

    /** Runtime property that controls behaviour for unclassified UI tests. */
    public static final String UNCLASSIFIED_POLICY_PROPERTY = "ui.mutation.unclassified.policy";

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(
            QualiStrataJunitUiExtension.class
    );
    private static final UiJunitResourceLockRegistry RESOURCE_LOCKS =
            new UiJunitResourceLockRegistry();

    private final List<UiTestReport> tests = new CopyOnWriteArrayList<>();
    private final UiJunitMutationClassifier mutationClassifier = new UiJunitMutationClassifier();
    private final UiJunitResourceLockResolver resourceLockResolver = new UiJunitResourceLockResolver();
    private final UiJunitMutationClassificationValidator mutationValidator;
    private final Properties properties;
    private final Path reportDirectory;
    private final UiArtifactCollector artifactCollector;
    private final List<UiReportWriter> reportWriters;
    private final String runId;
    private Instant startedAt;

    /** Creates extension using current system properties and local JSON/HTML writers. */
    public QualiStrataJunitUiExtension() {
        this(System.getProperties());
    }

    /**
     * Creates extension with explicit properties, useful for unit tests.
     *
     * @param properties runtime override properties.
     */
    public QualiStrataJunitUiExtension(Properties properties) {
        this(
                properties,
                outputDirectory(properties),
                UiArtifactCollectors.localFailureCollectors(outputDirectory(properties), properties),
                List.of(new JsonUiReportWriter(), new HtmlUiReportWriter())
        );
    }

    /**
     * Creates extension with explicit collaborators, useful for tests and custom runners.
     *
     * @param properties runtime override properties.
     * @param reportDirectory directory where UI reports are written.
     * @param artifactCollector collector used for test artifacts.
     * @param reportWriters writers used to persist UI reports.
     */
    public QualiStrataJunitUiExtension(
            Properties properties,
            Path reportDirectory,
            UiArtifactCollector artifactCollector,
            List<UiReportWriter> reportWriters
    ) {
        this.properties = properties == null ? new Properties() : properties;
        this.mutationValidator = new UiJunitMutationClassificationValidator(
                mutationClassifier,
                UiJunitUnclassifiedTestPolicy.from(this.properties.getProperty(UNCLASSIFIED_POLICY_PROPERTY))
        );
        this.reportDirectory = reportDirectory == null ? Path.of("build", "reports", "ui") : reportDirectory;
        this.artifactCollector = artifactCollector == null
                ? UiArtifactCollectors.localFailureCollectors(this.reportDirectory, this.properties)
                : artifactCollector;
        this.reportWriters = reportWriters == null ? List.of() : List.copyOf(reportWriters);
        this.runId = UUID.randomUUID().toString();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        startedAt = Instant.now();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        UiJunitMutationType mutationType = mutationClassifier.classify(method, context.getTags());
        boolean exclusiveEnvironment = mutationType == UiJunitMutationType.EXCLUSIVE;

        List<UiJunitResourceLockRegistry.Request> requests = resourceLockResolver.resolve(
                method,
                value("target", ""),
                exclusiveEnvironment
        );
        UiJunitResourceLockRegistry.Handle handle = RESOURCE_LOCKS.acquire(requests);
        context.getStore(NAMESPACE).put(resourceLockKey(context), handle);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        Instant start = Instant.now();
        context.getStore(NAMESPACE).put(startKey(context), start);

        Method method = context.getRequiredTestMethod();
        try {
            mutationValidator.validate(method, context.getTags());
        } catch (RuntimeException exception) {
            record(context, UiTestStatus.FAILED, start, exception);
            UiSessionRegistry.closeAllForCurrentThread().ifPresent(exception::addSuppressed);
            throw exception;
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Throwable failure = context.getExecutionException().orElse(null);
        UiTestStatus status = failure == null ? UiTestStatus.PASSED : statusFor(failure);
        Instant start = context.getStore(NAMESPACE).remove(startKey(context), Instant.class);
        record(context, status, start == null ? Instant.now() : start, failure);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        UiJunitResourceLockRegistry.Handle handle = context.getStore(NAMESPACE)
                .remove(resourceLockKey(context), UiJunitResourceLockRegistry.Handle.class);
        if (handle != null) {
            handle.close();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) {
        UiRunReport report = buildReport();
        for (UiReportWriter writer : reportWriters) {
            writer.write(report, reportDirectory);
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        record(context, UiTestStatus.SKIPPED, Instant.now(), reason.map(DisabledTestException::new).orElse(null));
    }

    /**
     * Returns immutable snapshot of test reports collected so far.
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

    private void record(ExtensionContext context, UiTestStatus status, Instant started, Throwable failure) {
        List<UiArtifact> artifacts = collectArtifacts(status, context, failure);
        RuntimeException cleanupFailure = UiSessionRegistry.closeAllForCurrentThread().orElse(null);

        Throwable finalFailure = failure == null ? cleanupFailure : failure;
        if (finalFailure != null && cleanupFailure != null && finalFailure != cleanupFailure) {
            finalFailure.addSuppressed(cleanupFailure);
        }

        Method method = context.getTestMethod().orElse(null);
        UiJunitMutationType mutationType = method == null
                ? null
                : mutationClassifier.classify(method, context.getTags());
        Instant testStartedAt = started == null ? Instant.now() : started;

        tests.add(new UiTestReport(
                context.getTestClass().map(Class::getName).orElse(""),
                context.getTestMethod().map(Method::getName).orElse(context.getDisplayName()),
                status,
                testStartedAt,
                Duration.between(testStartedAt, Instant.now()),
                context.getTags().stream().sorted().toList(),
                mutationType == null ? null : mutationType.name(),
                artifacts,
                List.<UiStepReport>of(),
                summarize(finalFailure)
        ));
    }

    private List<UiArtifact> collectArtifacts(UiTestStatus status, ExtensionContext context, Throwable failure) {
        if (status != UiTestStatus.FAILED) {
            return List.of();
        }

        return UiSessionRegistry.activeSession()
                .map(session -> artifactCollector.collectOnFailure(session, testName(context), failure))
                .orElseGet(List::of);
    }

    private UiTestStatus statusFor(Throwable throwable) {
        return throwable != null && throwable.getClass().getName().equals("org.opentest4j.TestAbortedException")
                ? UiTestStatus.SKIPPED
                : UiTestStatus.FAILED;
    }

    private String testName(ExtensionContext context) {
        return context.getTestClass().map(Class::getName).orElse("unknown-class")
                + "."
                + context.getTestMethod().map(Method::getName).orElse("unknown-method");
    }

    private String startKey(ExtensionContext context) {
        return context.getUniqueId() + ":start";
    }

    private String resourceLockKey(ExtensionContext context) {
        return context.getUniqueId() + ":shared-resource-locks";
    }

    private String summarize(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
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

    private static final class DisabledTestException extends RuntimeException {
        private DisabledTestException(String reason) {
            super(reason);
        }
    }
}

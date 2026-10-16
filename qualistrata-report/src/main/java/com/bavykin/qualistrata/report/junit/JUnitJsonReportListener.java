package com.bavykin.qualistrata.report.junit;

import com.bavykin.qualistrata.report.model.TestRun;
import com.bavykin.qualistrata.report.writer.HtmlTestRunWriter;
import com.bavykin.qualistrata.report.writer.JsonTestRunWriter;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JUnit Platform listener that captures executions and writes one JSON report.
 *
 * <p>The listener delegates individual test events to
 * {@link JUnitTestRunListener}. When the test plan finishes, it assembles the
 * canonical {@link TestRun} and writes it through {@link JsonTestRunWriter}.
 * Reporting can be configured through {@link JUnitReportConfiguration} system
 * properties.</p>
 */
public final class JUnitJsonReportListener
        implements TestExecutionListener {

    private final JUnitTestRunListener delegate;
    private final JUnitTestRunAssembler assembler;
    private final JsonTestRunWriter writer;
    private final HtmlTestRunWriter htmlWriter;
    private final JUnitReportConfiguration configuration;
    private final Clock clock;
    private final AtomicReference<Instant> startedAt =
            new AtomicReference<>();

    /**
     * Creates an automatically configured JUnit JSON report listener.
     */
    public JUnitJsonReportListener() {
        this(
                new JUnitTestRunListener(),
                JUnitReportConfiguration.fromSystemProperties(),
                new JsonTestRunWriter(),
                Clock.systemUTC()
        );
    }

    JUnitJsonReportListener(
            JUnitTestRunListener delegate,
            JUnitReportConfiguration configuration,
            JsonTestRunWriter writer,
            Clock clock
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration"
        );
        this.writer = Objects.requireNonNull(writer, "writer");
        this.htmlWriter = new HtmlTestRunWriter(writer);
        this.clock = Objects.requireNonNull(clock, "clock");
        this.assembler = new JUnitTestRunAssembler(delegate);
    }

    /**
     * Records the beginning of the JUnit test plan.
     *
     * @param testPlan discovered JUnit test plan
     */
    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        Objects.requireNonNull(testPlan, "testPlan");
        if (configuration.enabled()) {
            startedAt.compareAndSet(null, clock.instant());
        }
    }

    /**
     * Builds and writes the canonical JSON report after normal plan completion.
     *
     * @param testPlan completed JUnit test plan
     */
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        Objects.requireNonNull(testPlan, "testPlan");
        if (!configuration.enabled()) {
            return;
        }

        Instant finishedAt = clock.instant();
        Instant runStartedAt = startedAt.getAndSet(null);
        if (runStartedAt == null) {
            runStartedAt = delegate.executionResults().stream()
                    .map(result -> result.startedAt())
                    .min(Instant::compareTo)
                    .orElse(finishedAt);
        }

        TestRun testRun = assembler.complete(
                configuration.runId(),
                configuration.source(),
                runStartedAt,
                finishedAt
        );
        writer.write(testRun, configuration.outputPath());
        htmlWriter.write(
                testRun,
                HtmlTestRunWriter.siblingPath(configuration.outputPath())
        );
    }

    /**
     * Delegates a test start event to the framework-neutral capture listener.
     *
     * @param testIdentifier started JUnit test or container
     */
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (configuration.enabled()) {
            delegate.executionStarted(testIdentifier);
        }
    }

    /**
     * Delegates a skipped test event to the capture listener.
     *
     * @param testIdentifier skipped JUnit test or container
     * @param reason framework-provided skip reason
     */
    @Override
    public void executionSkipped(
            TestIdentifier testIdentifier,
            String reason
    ) {
        if (configuration.enabled()) {
            delegate.executionSkipped(testIdentifier, reason);
        }
    }

    /**
     * Delegates a completed test event to the capture listener.
     *
     * @param testIdentifier completed JUnit test or container
     * @param testExecutionResult framework execution result
     */
    @Override
    public void executionFinished(
            TestIdentifier testIdentifier,
            TestExecutionResult testExecutionResult
    ) {
        if (configuration.enabled()) {
            delegate.executionFinished(
                    testIdentifier,
                    testExecutionResult
            );
        }
    }
}

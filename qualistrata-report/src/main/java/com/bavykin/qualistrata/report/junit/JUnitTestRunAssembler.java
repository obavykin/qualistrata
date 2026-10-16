package com.bavykin.qualistrata.report.junit;

import com.bavykin.qualistrata.report.metadata.TestRunMetadataCollector;
import com.bavykin.qualistrata.report.model.ExecutionProblem;
import com.bavykin.qualistrata.report.model.TestRun;
import com.bavykin.qualistrata.report.model.TestRunDescriptor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds the canonical QualiStrata test-run model from captured JUnit results.
 *
 * <p>The assembler deliberately does not write report files. It only combines
 * JUnit listener results, run boundaries, consumer metadata, and run-level
 * execution problems into one immutable {@link TestRun}. Writers remain a
 * separate concern and can serialize the returned model to JSON or HTML.</p>
 */
public final class JUnitTestRunAssembler {

    private final JUnitTestRunListener listener;
    private final TestRunMetadataCollector metadataCollector;
    private final JUnitResultAggregator resultAggregator;

    /**
     * Creates an assembler using metadata providers discovered through the
     * thread context class loader.
     *
     * @param listener JUnit listener containing captured execution results
     */
    public JUnitTestRunAssembler(JUnitTestRunListener listener) {
        this(
                listener,
                TestRunMetadataCollector.load(),
                new JUnitResultAggregator()
        );
    }

    /**
     * Creates an assembler with an explicit metadata collector.
     *
     * @param listener JUnit listener containing captured execution results
     * @param metadataCollector collector used for run-level metadata
     */
    public JUnitTestRunAssembler(
            JUnitTestRunListener listener,
            TestRunMetadataCollector metadataCollector
    ) {
        this(listener, metadataCollector, new JUnitResultAggregator());
    }

    JUnitTestRunAssembler(
            JUnitTestRunListener listener,
            TestRunMetadataCollector metadataCollector,
            JUnitResultAggregator resultAggregator
    ) {
        this.listener = Objects.requireNonNull(listener, "listener");
        this.metadataCollector = Objects.requireNonNull(
                metadataCollector,
                "metadataCollector"
        );
        this.resultAggregator = Objects.requireNonNull(
                resultAggregator,
                "resultAggregator"
        );
    }

    /**
     * Builds a complete test run without run-level execution problems.
     *
     * @param runId stable run identifier
     * @param source source task, suite, or execution name
     * @param startedAt run start timestamp
     * @param finishedAt run finish timestamp
     * @return immutable complete test-run model
     */
    public TestRun complete(
            String runId,
            String source,
            Instant startedAt,
            Instant finishedAt
    ) {
        return assemble(
                runId,
                source,
                startedAt,
                finishedAt,
                true,
                List.of()
        );
    }

    /**
     * Builds a canonical complete or partial test run.
     *
     * @param runId stable run identifier
     * @param source source task, suite, or execution name
     * @param startedAt run start timestamp
     * @param finishedAt run finish timestamp
     * @param complete whether normal adapter lifecycle completion was observed
     * @param executionProblems run-level failures outside normal test results
     * @return immutable canonical test-run model
     * @throws com.bavykin.qualistrata.report.metadata.MetadataCollectionException
     *         when metadata providers cannot be loaded or merged safely
     */
    public TestRun assemble(
            String runId,
            String source,
            Instant startedAt,
            Instant finishedAt,
            boolean complete,
            List<ExecutionProblem> executionProblems
    ) {
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(finishedAt, "finishedAt");

        Map<String, Object> metadata = metadataCollector.collect();

        return TestRun.current(
                new TestRunDescriptor(runId, source, startedAt),
                finishedAt,
                complete,
                metadata,
                resultAggregator.aggregate(listener.executionResults()),
                executionProblems
        );
    }
}

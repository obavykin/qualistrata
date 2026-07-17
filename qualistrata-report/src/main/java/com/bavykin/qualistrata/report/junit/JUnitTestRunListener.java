package com.bavykin.qualistrata.report.junit;

import com.bavykin.qualistrata.report.metadata.ReportAnnotationResolver;
import com.bavykin.qualistrata.report.model.FailureDetails;
import com.bavykin.qualistrata.report.model.TestDescriptor;
import com.bavykin.qualistrata.report.model.TestExecutionResult;
import com.bavykin.qualistrata.report.model.TestFramework;
import com.bavykin.qualistrata.report.model.TestIdentifier;
import com.bavykin.qualistrata.report.model.TestMetadata;
import com.bavykin.qualistrata.report.model.TestStatus;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Captures JUnit Platform test lifecycle events as framework-neutral results.
 */
public final class JUnitTestRunListener implements TestExecutionListener {

    private final Clock clock;
    private final ReportAnnotationResolver annotationResolver;
    private final Map<String, Instant> startedAt = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<TestExecutionResult> results =
            new ConcurrentLinkedQueue<>();

    /**
     * Creates a listener using the UTC system clock.
     */
    public JUnitTestRunListener() {
        this(Clock.systemUTC(), new ReportAnnotationResolver());
    }

    JUnitTestRunListener(
            Clock clock,
            ReportAnnotationResolver annotationResolver
    ) {
        this.clock = clock;
        this.annotationResolver = annotationResolver;
    }

    @Override
    public void executionStarted(
            org.junit.platform.launcher.TestIdentifier testIdentifier
    ) {
        if (testIdentifier.isTest()) {
            startedAt.put(testIdentifier.getUniqueId(), clock.instant());
        }
    }

    @Override
    public void executionSkipped(
            org.junit.platform.launcher.TestIdentifier testIdentifier,
            String reason
    ) {
        if (!testIdentifier.isTest()) {
            return;
        }

        Instant now = clock.instant();
        results.add(new TestExecutionResult(
                descriptor(testIdentifier),
                TestStatus.SKIPPED,
                now,
                now,
                reason == null || reason.isBlank()
                        ? null
                        : new FailureDetails(null, reason, null)
        ));
    }

    @Override
    public void executionFinished(
            org.junit.platform.launcher.TestIdentifier testIdentifier,
            org.junit.platform.engine.TestExecutionResult junitResult
    ) {
        if (!testIdentifier.isTest()) {
            return;
        }

        Instant finishedAt = clock.instant();
        Instant started = startedAt.remove(testIdentifier.getUniqueId());
        if (started == null) {
            started = finishedAt;
        }

        results.add(new TestExecutionResult(
                descriptor(testIdentifier),
                status(junitResult.getStatus()),
                started,
                finishedAt,
                junitResult.getThrowable()
                        .map(JUnitTestRunListener::failure)
                        .orElse(null)
        ));
    }

    /**
     * Returns a deterministic immutable snapshot of captured test results.
     *
     * @return captured execution results ordered by start time and unique ID
     */
    public List<TestExecutionResult> executionResults() {
        List<TestExecutionResult> snapshot = new ArrayList<>(results);
        snapshot.sort(Comparator
                .comparing(TestExecutionResult::startedAt)
                .thenComparing(result ->
                        result.test().identifier().uniqueId()
                ));
        return List.copyOf(snapshot);
    }

    private TestDescriptor descriptor(
            org.junit.platform.launcher.TestIdentifier identifier
    ) {
        SourceData source = source(identifier);

        return new TestDescriptor(
                new TestIdentifier(
                        identifier.getUniqueId(),
                        TestFramework.JUNIT,
                        engineId(identifier.getUniqueId()),
                        null,
                        source.className(),
                        source.methodName(),
                        identifier.getUniqueId()
                ),
                identifier.getDisplayName(),
                Map.of(),
                identifier.getTags().stream()
                        .map(org.junit.platform.engine.TestTag::getName)
                        .collect(Collectors.toUnmodifiableSet()),
                source.metadata()
        );
    }

    private SourceData source(
            org.junit.platform.launcher.TestIdentifier identifier
    ) {
        return identifier.getSource()
                .map(source -> {
                    if (source instanceof MethodSource methodSource) {
                        Class<?> testClass = loadClass(
                                methodSource.getClassName()
                        );
                        Method method = findMethod(
                                testClass,
                                methodSource.getMethodName()
                        );
                        return new SourceData(
                                methodSource.getClassName(),
                                methodSource.getMethodName(),
                                annotationResolver.resolve(testClass, method)
                        );
                    }
                    if (source instanceof ClassSource classSource) {
                        Class<?> testClass = loadClass(
                                classSource.getClassName()
                        );
                        return new SourceData(
                                classSource.getClassName(),
                                null,
                                annotationResolver.resolve(testClass, null)
                        );
                    }
                    return SourceData.EMPTY;
                })
                .orElse(SourceData.EMPTY);
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(
                    className,
                    false,
                    Thread.currentThread().getContextClassLoader()
            );
        } catch (ClassNotFoundException exception) {
            return Object.class;
        }
    }

    private static Method findMethod(Class<?> testClass, String methodName) {
        Method match = null;
        for (Method method : testClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (match != null) {
                    return null;
                }
                match = method;
            }
        }
        return match;
    }

    private static TestStatus status(Status status) {
        return switch (status) {
            case SUCCESSFUL -> TestStatus.PASSED;
            case ABORTED -> TestStatus.ABORTED;
            case FAILED -> TestStatus.FAILED;
        };
    }

    private static FailureDetails failure(Throwable throwable) {
        StringWriter buffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(buffer));
        return new FailureDetails(
                throwable.getClass().getName(),
                throwable.getMessage(),
                buffer.toString()
        );
    }

    private static String engineId(String uniqueId) {
        int prefix = uniqueId.indexOf("[engine:");
        if (prefix < 0) {
            return null;
        }
        int start = prefix + "[engine:".length();
        int end = uniqueId.indexOf(']', start);
        return end < 0 ? null : uniqueId.substring(start, end);
    }

    private record SourceData(
            String className,
            String methodName,
            TestMetadata metadata
    ) {
        private static final SourceData EMPTY =
                new SourceData(null, null, TestMetadata.EMPTY);
    }
}

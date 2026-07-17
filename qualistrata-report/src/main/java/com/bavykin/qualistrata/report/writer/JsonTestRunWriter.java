package com.bavykin.qualistrata.report.writer;

import com.bavykin.qualistrata.report.model.ExecutionProblem;
import com.bavykin.qualistrata.report.model.FailureDetails;
import com.bavykin.qualistrata.report.model.LogicalTestResult;
import com.bavykin.qualistrata.report.model.TestAttempt;
import com.bavykin.qualistrata.report.model.TestDescriptor;
import com.bavykin.qualistrata.report.model.TestExecutionResult;
import com.bavykin.qualistrata.report.model.TestIdentifier;
import com.bavykin.qualistrata.report.model.TestRun;
import com.bavykin.qualistrata.report.model.TestRunSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 * Deterministic JSON writer for the QualiStrata test-run schema.
 *
 * <p>The writer maps the domain model explicitly instead of annotating model
 * records with Jackson-specific details. Files are written through a temporary
 * sibling and then replaced atomically where the file system supports it.</p>
 */
public final class JsonTestRunWriter implements TestRunWriter {

    private final ObjectMapper objectMapper;

    /**
     * Creates a writer with deterministic map and property ordering.
     */
    public JsonTestRunWriter() {
        objectMapper = JsonMapper.builder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    /**
     * Serializes a test run to a deterministic formatted JSON string.
     *
     * @param testRun immutable test-run model
     * @return JSON representation ending with the platform-independent newline
     */
    public String toJson(TestRun testRun) {
        Objects.requireNonNull(testRun, "testRun");

        try {
            return objectMapper.writeValueAsString(toRootNode(testRun)) + "\n";
        } catch (JsonProcessingException exception) {
            throw new ReportWriteException(
                    "Unable to serialize test run " + testRun.descriptor().runId(),
                    exception
            );
        }
    }

    @Override
    public void write(TestRun testRun, Path outputPath) {
        Objects.requireNonNull(outputPath, "outputPath");

        Path absoluteOutput = outputPath.toAbsolutePath().normalize();
        Path parent = absoluteOutput.getParent();

        if (parent == null) {
            throw new IllegalArgumentException(
                    "Output path must have a parent directory"
            );
        }

        Path temporary = parent.resolve(
                "." + absoluteOutput.getFileName() + ".tmp"
        );

        try {
            Files.createDirectories(parent);
            Files.writeString(
                    temporary,
                    toJson(testRun),
                    StandardCharsets.UTF_8
            );
            replace(temporary, absoluteOutput);
        } catch (IOException exception) {
            deleteQuietly(temporary);
            throw new ReportWriteException(
                    "Unable to write JSON test-run report to " + absoluteOutput,
                    exception
            );
        }
    }

    private ObjectNode toRootNode(TestRun testRun) {
        ObjectNode root = objectMapper.createObjectNode();
        TestRunSummary summary = testRun.summary();

        root.put("schemaVersion", testRun.schemaVersion());
        root.put("runId", testRun.descriptor().runId());
        root.put("source", testRun.descriptor().source());
        root.put("startedAt", testRun.descriptor().startedAt().toString());
        root.put("finishedAt", testRun.finishedAt().toString());
        root.put("durationMs", testRun.duration().toMillis());
        root.put("complete", testRun.complete());
        root.set("metadata", sortedObject(testRun.metadata()));
        root.set("summary", summaryNode(summary));

        ArrayNode tests = root.putArray("tests");
        testRun.tests().stream()
                .sorted(Comparator.comparing(
                        result -> result.test().identifier().uniqueId()
                ))
                .map(this::logicalTestNode)
                .forEach(tests::add);

        ArrayNode problems = root.putArray("executionProblems");
        testRun.executionProblems().stream()
                .sorted(Comparator
                        .comparing(ExecutionProblem::occurredAt)
                        .thenComparing(ExecutionProblem::phase))
                .map(this::executionProblemNode)
                .forEach(problems::add);

        return root;
    }

    private ObjectNode summaryNode(TestRunSummary summary) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("total", summary.total());
        node.put("passed", summary.passed());
        node.put("failed", summary.failed());
        node.put("skipped", summary.skipped());
        node.put("aborted", summary.aborted());
        node.put("retried", summary.retried());
        return node;
    }

    private ObjectNode logicalTestNode(LogicalTestResult logicalTest) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("test", descriptorNode(logicalTest.test()));
        node.put("finalStatus", logicalTest.finalStatus().name());
        node.put("retried", logicalTest.retried());
        node.put("totalDurationMs", logicalTest.totalDuration().toMillis());

        ArrayNode attempts = node.putArray("attempts");
        logicalTest.attempts().stream()
                .sorted(Comparator.comparingInt(TestAttempt::number))
                .map(this::attemptNode)
                .forEach(attempts::add);

        return node;
    }

    private ObjectNode attemptNode(TestAttempt attempt) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("number", attempt.number());
        node.set("result", executionResultNode(attempt.result()));
        return node;
    }

    private ObjectNode executionResultNode(TestExecutionResult result) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("status", result.status().name());
        node.put("startedAt", result.startedAt().toString());
        node.put("finishedAt", result.finishedAt().toString());
        node.put("durationMs", result.duration().toMillis());

        if (result.failure() == null) {
            node.putNull("failure");
        } else {
            node.set("failure", failureNode(result.failure()));
        }

        return node;
    }

    private ObjectNode descriptorNode(TestDescriptor descriptor) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("identifier", identifierNode(descriptor.identifier()));
        node.put("displayName", descriptor.displayName());
        node.set("parameters", sortedObject(descriptor.parameters()));

        ArrayNode tags = node.putArray("tags");
        descriptor.tags().stream()
                .sorted()
                .forEach(tags::add);

        return node;
    }

    private ObjectNode identifierNode(TestIdentifier identifier) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("uniqueId", identifier.uniqueId());
        node.put("framework", identifier.framework().name());
        putNullable(node, "engineId", identifier.engineId());
        putNullable(node, "suiteName", identifier.suiteName());
        putNullable(node, "className", identifier.className());
        putNullable(node, "methodName", identifier.methodName());
        putNullable(node, "invocationId", identifier.invocationId());
        return node;
    }

    private ObjectNode executionProblemNode(ExecutionProblem problem) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("phase", problem.phase());
        node.put("occurredAt", problem.occurredAt().toString());
        node.set("failure", failureNode(problem.failure()));
        return node;
    }

    private ObjectNode failureNode(FailureDetails failure) {
        ObjectNode node = objectMapper.createObjectNode();
        putNullable(node, "exceptionType", failure.exceptionType());
        putNullable(node, "message", failure.message());
        putNullable(node, "stackTrace", failure.stackTrace());
        return node;
    }

    private ObjectNode sortedObject(Map<?, ?> values) {
        ObjectNode node = objectMapper.createObjectNode();
        values.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> {
                    JsonNode value = objectMapper.valueToTree(entry.getValue());
                    node.set(entry.getKey().toString(), value);
                });
        return node;
    }

    private static void putNullable(
            ObjectNode node,
            String fieldName,
            String value
    ) {
        if (value == null) {
            node.putNull(fieldName);
        } else {
            node.put(fieldName, value);
        }
    }

    private static void replace(Path temporary, Path output) throws IOException {
        try {
            Files.move(
                    temporary,
                    output,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(
                    temporary,
                    output,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The original write failure remains the primary diagnostic.
        }
    }
}

package com.bavykin.qualistrata.report.annotation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportAnnotationsTest {

    @Test
    void exposesRuntimeMetadataAnnotations() throws Exception {
        Method method = AnnotatedFixture.class.getDeclaredMethod("reports");

        assertEquals(
                "Checks monthly utilization",
                method.getAnnotation(TestDescription.class).value()
        );
        assertEquals(
                "TN Utilization",
                method.getAnnotation(Feature.class).value()
        );
        assertEquals(
                "Reports",
                method.getAnnotation(Component.class).value()
        );
    }

    @Test
    void supportsRepeatedExternalTestCaseReferences() throws Exception {
        Method method = AnnotatedFixture.class.getDeclaredMethod("reports");

        List<String> references = Stream.of(
                        method.getAnnotationsByType(CoversTestCase.class)
                )
                .map(annotation ->
                        annotation.system() + ":" + annotation.value()
                )
                .toList();

        assertEquals(
                List.of("qTest:TC-101", "qTest:TC-102"),
                references
        );
    }

    private static final class AnnotatedFixture {

        @TestDescription("Checks monthly utilization")
        @Feature("TN Utilization")
        @Component("Reports")
        @CoversTestCase(value = "TC-101", system = "qTest")
        @CoversTestCase(value = "TC-102", system = "qTest")
        private void reports() {
        }
    }
}

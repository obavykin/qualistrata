package com.bavykin.qualistrata.report.metadata;

import com.bavykin.qualistrata.report.annotation.Component;
import com.bavykin.qualistrata.report.annotation.CoversTestCase;
import com.bavykin.qualistrata.report.annotation.Feature;
import com.bavykin.qualistrata.report.annotation.TestDescription;
import com.bavykin.qualistrata.report.model.TestMetadata;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportAnnotationResolverTest {

    @Test
    void resolvesMethodOverridesAndCombinedCoverage() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("reports");

        TestMetadata metadata =
                new ReportAnnotationResolver().resolve(Fixture.class, method);

        assertEquals("method description", metadata.description());
        assertEquals("method feature", metadata.feature());
        assertEquals("class component", metadata.component());
        assertEquals(2, metadata.coveredTestCases().size());
        assertEquals("CLASS-1", metadata.coveredTestCases().getFirst().id());
        assertEquals("METHOD-1", metadata.coveredTestCases().getLast().id());
    }

    @TestDescription("class description")
    @Feature("class feature")
    @Component("class component")
    @CoversTestCase("CLASS-1")
    private static final class Fixture {

        @TestDescription("method description")
        @Feature("method feature")
        @CoversTestCase(value = "METHOD-1", system = "qTest")
        private void reports() {
        }
    }
}

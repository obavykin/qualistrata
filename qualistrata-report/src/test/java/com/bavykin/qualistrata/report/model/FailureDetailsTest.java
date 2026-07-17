package com.bavykin.qualistrata.report.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FailureDetailsTest {

    @Test
    void normalizesTextualFailureData() {
        FailureDetails details = new FailureDetails(
                " java.lang.AssertionError ",
                " expected value ",
                " stack trace "
        );

        assertEquals("java.lang.AssertionError", details.exceptionType());
        assertEquals("expected value", details.message());
        assertEquals("stack trace", details.stackTrace());
    }

    @Test
    void allowsPartialFailureData() {
        FailureDetails details = new FailureDetails(null, "failure", null);

        assertNull(details.exceptionType());
        assertEquals("failure", details.message());
        assertNull(details.stackTrace());
    }

    @Test
    void rejectsCompletelyEmptyFailureData() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FailureDetails(" ", null, "")
        );
    }
}

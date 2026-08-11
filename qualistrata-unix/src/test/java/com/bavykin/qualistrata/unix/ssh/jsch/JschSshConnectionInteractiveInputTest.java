package com.bavykin.qualistrata.unix.ssh.jsch;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JschSshConnectionInteractiveInputTest {

    @Test
    void shouldCloseInteractiveInputExactlyOnceAfterAllResponsesAreAnswered() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        ByteArrayOutputStream stdin = new ByteArrayOutputStream() {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
            }
        };

        boolean closed = JschSshConnection.closeInteractiveInputIfComplete(
                stdin,
                2,
                Set.of(0, 1),
                false
        );
        closed = JschSshConnection.closeInteractiveInputIfComplete(
                stdin,
                2,
                Set.of(0, 1),
                closed
        );

        assertTrue(closed);
        assertEquals(1, closeCalls.get());
    }

    @Test
    void shouldKeepInteractiveInputOpenUntilAllResponsesAreAnswered() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        ByteArrayOutputStream stdin = new ByteArrayOutputStream() {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
            }
        };

        boolean closed = JschSshConnection.closeInteractiveInputIfComplete(
                stdin,
                2,
                Set.of(0),
                false
        );

        assertFalse(closed);
        assertEquals(0, closeCalls.get());
    }

    @Test
    void shouldKeepInteractiveInputOpenWhenNoResponsesAreConfigured() throws IOException {
        AtomicInteger closeCalls = new AtomicInteger();
        ByteArrayOutputStream stdin = new ByteArrayOutputStream() {
            @Override
            public void close() {
                closeCalls.incrementAndGet();
            }
        };

        boolean closed = JschSshConnection.closeInteractiveInputIfComplete(
                stdin,
                0,
                Set.of(),
                false
        );

        assertFalse(closed);
        assertEquals(0, closeCalls.get());
    }
}

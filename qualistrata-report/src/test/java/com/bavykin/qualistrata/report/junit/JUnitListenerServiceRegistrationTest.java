package com.bavykin.qualistrata.report.junit;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.TestExecutionListener;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitListenerServiceRegistrationTest {

    @Test
    void registersListenerThroughJUnitPlatformServiceLoader() {
        boolean registered = ServiceLoader
                .load(TestExecutionListener.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .anyMatch(JUnitTestRunListener.class::equals);

        assertTrue(registered);
    }
}

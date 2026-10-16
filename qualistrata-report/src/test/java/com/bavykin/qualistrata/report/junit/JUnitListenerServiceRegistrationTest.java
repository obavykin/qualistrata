package com.bavykin.qualistrata.report.junit;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TestExecutionListener;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitListenerServiceRegistrationTest {

    @Test
    void registersLauncherSessionListenerThroughServiceLoader() {
        boolean registered = ServiceLoader
                .load(LauncherSessionListener.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .anyMatch(
                        JUnitJsonReportLauncherSessionListener.class::equals
                );

        assertTrue(registered);
    }

    @Test
    void doesNotRegisterJsonListenerTwiceThroughDirectServiceLoader() {
        boolean directlyRegistered = ServiceLoader
                .load(TestExecutionListener.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .anyMatch(JUnitJsonReportListener.class::equals);

        assertFalse(directlyRegistered);
    }
}

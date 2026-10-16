package com.bavykin.qualistrata.ui.junit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UiJunitResourceLockResolverTest {

    private final UiJunitResourceLockResolver resolver = new UiJunitResourceLockResolver();

    @Test
    void combinesClassAndMethodLocksAndPromotesWriteAccess() throws Exception {
        List<UiJunitResourceLockRegistry.Request> resolved = resolver.resolve(
                method("writesPivotSize"),
                "release_6.6/pt-a",
                false
        );

        assertEquals(2, resolved.size());
        assertEquals(
                new UiJunitResourceLockRegistry.Request(
                        "00|TARGET|release_6.6/pt-a|__environment__",
                        SharedResourceAccessMode.READ
                ),
                resolved.get(0)
        );
        assertEquals(
                new UiJunitResourceLockRegistry.Request(
                        "10|TARGET|release_6.6/pt-a|ADMIN_OPTIONS:PIVOT_SZ",
                        SharedResourceAccessMode.WRITE
                ),
                resolved.get(1)
        );
    }

    @Test
    void exclusiveEnvironmentPromotesImplicitEnvironmentLockToWrite() throws Exception {
        List<UiJunitResourceLockRegistry.Request> resolved = resolver.resolve(
                method("readsPivotSize"),
                "release_6.6/pt-a",
                true
        );

        assertEquals(SharedResourceAccessMode.WRITE, resolved.get(0).mode());
    }

    @Test
    void targetScopedResourcesUseDifferentKeysForDifferentTargets() throws Exception {
        String first = resolver.resolve(method("readsPivotSize"), "release_6.6/pt-a", false)
                .get(1).key();
        String second = resolver.resolve(method("readsPivotSize"), "release_6.6/pt-b", false)
                .get(1).key();

        org.junit.jupiter.api.Assertions.assertNotEquals(first, second);
    }

    @Test
    void globalResourceKeyDoesNotDependOnTarget() throws Exception {
        String first = resolver.resolve(method("writesGlobalSlot"), "release_6.6/pt-a", false)
                .get(1).key();
        String second = resolver.resolve(method("writesGlobalSlot"), "release_6.6/pt-b", false)
                .get(1).key();

        assertEquals(first, second);
        assertEquals("10|GLOBAL|LOCAL_BROWSER_INSTALL", first);
    }

    @Test
    void rejectsBlankResourceNames() throws Exception {
        assertThrows(
                IllegalStateException.class,
                () -> resolver.resolve(method("blankResource"), "release_6.6/pt-a", false)
        );
    }

    private Method method(String name) throws NoSuchMethodException {
        return Fixtures.class.getDeclaredMethod(name);
    }

    @SharedResourceLock("ADMIN_OPTIONS:PIVOT_SZ")
    private static final class Fixtures {

        void readsPivotSize() {
            // Fixture method.
        }

        @SharedResourceLock(value = "ADMIN_OPTIONS:PIVOT_SZ", mode = SharedResourceAccessMode.WRITE)
        void writesPivotSize() {
            // Fixture method.
        }

        @SharedResourceLock(
                value = "LOCAL_BROWSER_INSTALL",
                mode = SharedResourceAccessMode.WRITE,
                scope = SharedResourceScope.GLOBAL
        )
        void writesGlobalSlot() {
            // Fixture method.
        }

        @SharedResourceLock(" ")
        void blankResource() {
            // Fixture method.
        }
    }
}

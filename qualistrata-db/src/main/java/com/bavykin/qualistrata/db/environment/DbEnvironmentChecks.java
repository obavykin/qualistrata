package com.bavykin.qualistrata.db.environment;

/**
 * Shared constants for database environment checks.
 */
final class DbEnvironmentChecks {

    /**
     * Stable identifier for the generic database connection check.
     */
    static final String CONNECTION_CHECK_ID = "db.connection";

    /**
     * Stable identifier for the generic database version check.
     */
    static final String VERSION_CHECK_ID = "db.version";

    private DbEnvironmentChecks() {
        // Utility class.
    }
}

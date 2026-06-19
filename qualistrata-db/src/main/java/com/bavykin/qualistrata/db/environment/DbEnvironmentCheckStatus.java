package com.bavykin.qualistrata.db.environment;

/**
 * Represents the execution status of a database environment check.
 *
 * <p>The status is intentionally small for the first implementation increment:
 * a check either passes or fails. More statuses, such as warning or skipped, can
 * be introduced later if QualiStrata core environment reporting requires them.</p>
 */
public enum DbEnvironmentCheckStatus {

    /**
     * The database environment check completed successfully.
     */
    PASSED,

    /**
     * The database environment check failed because the check logic, provider,
     * connection, SQL execution, or value comparison failed.
     */
    FAILED
}

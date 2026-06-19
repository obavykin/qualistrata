package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.DbType;

import java.time.Duration;
import java.util.Map;

/**
 * Immutable result produced by database environment checks.
 *
 * <p>The model is local to {@code qualistrata-db} and deliberately does not
 * depend on project-specific reporting classes. Consumer projects can map this
 * result to their own JSON/HTML reports or to the common QualiStrata environment
 * reporting model when that integration layer is introduced.</p>
 *
 * @param id stable technical check identifier.
 * @param name human-readable check name.
 * @param dbAlias logical database alias from the resolved connection config.
 * @param dbType database engine type used by the check.
 * @param status final check status.
 * @param message safe diagnostic message without secrets.
 * @param duration time spent executing the check.
 * @param details additional safe structured details.
 */
public record DbEnvironmentCheckResult(
        String id,
        String name,
        String dbAlias,
        DbType dbType,
        DbEnvironmentCheckStatus status,
        String message,
        Duration duration,
        Map<String, String> details
) {

    /**
     * Creates a database environment check result and normalizes optional values.
     */
    public DbEnvironmentCheckResult {
        details = details == null ? Map.of() : Map.copyOf(details);
    }

    /**
     * Creates a successful database environment check result.
     *
     * @param id stable technical check identifier.
     * @param name human-readable check name.
     * @param dbAlias logical database alias.
     * @param dbType database engine type.
     * @param message safe diagnostic message.
     * @param duration execution duration.
     * @param details safe structured details.
     * @return successful check result.
     */
    public static DbEnvironmentCheckResult passed(
            String id,
            String name,
            String dbAlias,
            DbType dbType,
            String message,
            Duration duration,
            Map<String, String> details
    ) {
        return new DbEnvironmentCheckResult(
                id,
                name,
                dbAlias,
                dbType,
                DbEnvironmentCheckStatus.PASSED,
                message,
                duration,
                details
        );
    }

    /**
     * Creates a failed database environment check result.
     *
     * @param id stable technical check identifier.
     * @param name human-readable check name.
     * @param dbAlias logical database alias.
     * @param dbType database engine type.
     * @param message safe diagnostic message.
     * @param duration execution duration.
     * @param details safe structured details.
     * @return failed check result.
     */
    public static DbEnvironmentCheckResult failed(
            String id,
            String name,
            String dbAlias,
            DbType dbType,
            String message,
            Duration duration,
            Map<String, String> details
    ) {
        return new DbEnvironmentCheckResult(
                id,
                name,
                dbAlias,
                dbType,
                DbEnvironmentCheckStatus.FAILED,
                message,
                duration,
                details
        );
    }

    /**
     * Returns {@code true} when the check passed.
     *
     * @return {@code true} for successful check results.
     */
    public boolean passed() {
        return status == DbEnvironmentCheckStatus.PASSED;
    }
}

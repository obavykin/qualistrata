package com.bavykin.qualistrata.db.exception;

import com.bavykin.qualistrata.db.DbType;

/**
 * Base runtime exception for the QualiStrata database module.
 *
 * <p>The first increment uses a single exception type to keep the public API
 * small. More specific subclasses such as {@code DbConnectionException} or
 * {@code DbQueryException} can be introduced later without changing the core
 * client contracts.</p>
 *
 * <p>Messages must remain safe for logs and reports. Do not include passwords,
 * secret values, or full JDBC URLs with embedded credentials.</p>
 */
public class DbException extends RuntimeException {

    /** Logical DB alias related to the failure, when such alias is known. */
    private final String dbAlias;

    /** DB engine type related to the failure, when such type is known. */
    private final DbType dbType;

    /**
     * Creates a DB exception without DB alias or engine context.
     *
     * @param message safe diagnostic message.
     */
    public DbException(String message) {
        this(null, null, message, null);
    }

    /**
     * Creates a DB exception without DB alias or engine context, preserving the
     * original cause.
     *
     * @param message safe diagnostic message.
     * @param cause original failure cause.
     */
    public DbException(String message, Throwable cause) {
        this(null, null, message, cause);
    }

    /**
     * Creates a DB exception with logical DB context.
     *
     * @param dbAlias logical DB alias related to the failure.
     * @param dbType DB engine type related to the failure.
     * @param message safe diagnostic message.
     */
    public DbException(String dbAlias, DbType dbType, String message) {
        this(dbAlias, dbType, message, null);
    }

    /**
     * Creates a DB exception with logical DB context and the original cause.
     *
     * @param dbAlias logical DB alias related to the failure.
     * @param dbType DB engine type related to the failure.
     * @param message safe diagnostic message.
     * @param cause original failure cause.
     */
    public DbException(String dbAlias, DbType dbType, String message, Throwable cause) {
        super(message, cause);
        this.dbAlias = dbAlias;
        this.dbType = dbType;
    }

    /**
     * Returns the logical DB alias related to the failure.
     *
     * @return logical DB alias, or {@code null} when the failure happened before
     * an alias was known.
     */
    public String dbAlias() {
        return dbAlias;
    }

    /**
     * Returns the DB engine type related to the failure.
     *
     * @return DB engine type, or {@code null} when the failure happened before
     * an engine was known.
     */
    public DbType dbType() {
        return dbType;
    }
}

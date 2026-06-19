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

    private final String dbAlias;
    private final DbType dbType;

    public DbException(String message) {
        this(null, null, message, null);
    }

    public DbException(String message, Throwable cause) {
        this(null, null, message, cause);
    }

    public DbException(String dbAlias, DbType dbType, String message) {
        this(dbAlias, dbType, message, null);
    }

    public DbException(String dbAlias, DbType dbType, String message, Throwable cause) {
        super(message, cause);
        this.dbAlias = dbAlias;
        this.dbType = dbType;
    }

    /**
     * @return logical DB alias related to the failure, or {@code null} when the
     * failure happened before an alias was known.
     */
    public String dbAlias() {
        return dbAlias;
    }

    /**
     * @return DB engine type related to the failure, or {@code null} when the
     * failure happened before an engine was known.
     */
    public DbType dbType() {
        return dbType;
    }
}

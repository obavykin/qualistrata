package com.bavykin.qualistrata.db;

/**
 * Identifies a database engine supported by the QualiStrata database module.
 *
 * <p>The core DB API is intentionally engine-neutral. This enum is used only as
 * a stable engine selector for the first implementation increment. If the module
 * later moves to a plugin model, this enum can be replaced or complemented by a
 * string-based engine id without changing the higher-level DB client contracts.</p>
 */
public enum DbType {
    /** Oracle Database engine. */
    ORACLE,

    /** PostgreSQL database engine. */
    POSTGRES
}

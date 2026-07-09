/**
 * PostgreSQL-specific provider implementation for the QualiStrata database
 * module.
 *
 * <p>This package owns PostgreSQL JDBC URL construction, PostgreSQL health-check
 * SQL, and PostgreSQL database version SQL. Generic tests should still depend on
 * the common DB API rather than on PostgreSQL-specific classes.</p>
 */
package com.bavykin.qualistrata.db.postgres;

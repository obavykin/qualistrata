package com.bavykin.qualistrata.db.provider;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;

/**
 * Engine-specific provider used to create DB clients and expose standard SQL
 * snippets for health/version checks.
 *
 * <p>Provider implementations are the correct place for Oracle/PostgreSQL URL
 * rules, validation SQL, version SQL, and driver-specific quirks. The core DB
 * client API must remain database-engine-neutral.</p>
 */
public interface DbProvider {

    /** @return database engine supported by this provider. */
    DbType type();

    /**
     * Creates a client for the supplied connection configuration.
     *
     * @param config resolved connection configuration.
     * @return DB client.
     */
    DbClient createClient(DbConnectionConfig config);

    /**
     * @return SQL used to validate that a connection can execute a simple query.
     */
    String defaultHealthCheckSql();

    /**
     * @return SQL used to retrieve the database server version.
     */
    String databaseVersionSql();
}

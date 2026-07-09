/**
 * Database provider contracts and registry implementation.
 *
 * <p>Providers isolate database-engine-specific behavior behind a common API:
 * client creation, health-check SQL, version SQL, and JDBC URL construction.
 * Consumer projects may register custom providers or override built-in providers
 * through the registry.</p>
 */
package com.bavykin.qualistrata.db.provider;

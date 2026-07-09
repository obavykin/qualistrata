/**
 * Client contracts used by consumer tests to execute SQL without depending on
 * JDBC directly.
 *
 * <p>The package exposes the high-level database client API. Implementations may
 * use JDBC or another transport internally, but tests should depend only on the
 * contracts from this package.</p>
 */
package com.bavykin.qualistrata.db.client;

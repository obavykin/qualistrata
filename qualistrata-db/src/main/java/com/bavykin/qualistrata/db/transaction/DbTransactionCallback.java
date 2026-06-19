package com.bavykin.qualistrata.db.transaction;

/**
 * Callback executed inside a DB transaction.
 *
 * <p>The callback receives a transaction-scoped client. Implementations must not
 * expose the underlying JDBC connection to consumer tests.</p>
 */
@FunctionalInterface
public interface DbTransactionCallback {

    /**
     * Executes transactional work.
     *
     * @param transaction transaction-scoped DB operations.
     */
    void execute(DbTransaction transaction);
}

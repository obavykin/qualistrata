package com.bavykin.qualistrata.db.environment;

import com.bavykin.qualistrata.db.DbType;
import com.bavykin.qualistrata.db.client.DbClient;
import com.bavykin.qualistrata.db.config.DbConnectionConfig;
import com.bavykin.qualistrata.db.exception.DbException;
import com.bavykin.qualistrata.db.provider.DbProvider;
import com.bavykin.qualistrata.db.result.DbRows;
import com.bavykin.qualistrata.db.transaction.DbTransactionCallback;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;

/**
 * Test fixtures for DB environment check unit tests.
 */
final class DbEnvironmentCheckTestSupport {

    private DbEnvironmentCheckTestSupport() {
        // Utility class.
    }

    static DbConnectionConfig config() {
        return config(DbType.POSTGRES);
    }

    static DbConnectionConfig config(DbType type) {
        return new DbConnectionConfig(
                "main",
                type,
                "localhost",
                5432,
                "reporting",
                "public",
                "tester",
                "secret",
                null,
                Duration.ofSeconds(5),
                Duration.ofSeconds(5),
                Map.of()
        );
    }

    static final class FakeProvider implements DbProvider {

        private final DbType type;
        private final String healthSql;
        private final String versionSql;
        private final DbClient client;

        FakeProvider(DbType type, String healthSql, String versionSql, DbClient client) {
            this.type = type;
            this.healthSql = healthSql;
            this.versionSql = versionSql;
            this.client = client;
        }

        @Override
        public DbType type() {
            return type;
        }

        @Override
        public DbClient createClient(DbConnectionConfig config) {
            return client;
        }

        @Override
        public String defaultHealthCheckSql() {
            return healthSql;
        }

        @Override
        public String databaseVersionSql() {
            return versionSql;
        }
    }

    static final class FakeClient implements DbClient {

        private final Object scalarResult;
        private final RuntimeException failure;
        private boolean closed;
        private String executedSql;

        FakeClient(Object scalarResult) {
            this.scalarResult = scalarResult;
            this.failure = null;
        }

        FakeClient(RuntimeException failure) {
            this.scalarResult = null;
            this.failure = failure;
        }

        @Override
        public DbRows query(String sql, Object... parameters) {
            throw new UnsupportedOperationException("Not needed by these tests.");
        }

        @Override
        public int update(String sql, Object... parameters) {
            throw new UnsupportedOperationException("Not needed by these tests.");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T queryValue(String sql, Class<T> type, Object... parameters) {
            executedSql = sql;
            if (failure != null) {
                throw failure;
            }
            return (T) scalarResult;
        }

        @Override
        public boolean exists(String sql, Object... parameters) {
            throw new UnsupportedOperationException("Not needed by these tests.");
        }

        @Override
        public void transaction(DbTransactionCallback callback) {
            throw new UnsupportedOperationException("Not needed by these tests.");
        }

        @Override
        public void close() {
            closed = true;
        }

        boolean closed() {
            return closed;
        }

        String executedSql() {
            return executedSql;
        }
    }

    static RuntimeException failure(String message) {
        return new DbException("main", DbType.POSTGRES, message);
    }

    static RuntimeException sqlFailure(String message, String sqlState, int errorCode) {
        SQLException sqlException = new SQLException(message, sqlState, errorCode);
        return new DbException("main", DbType.POSTGRES, "DB operation failed.", sqlException);
    }
}

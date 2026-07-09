package com.bavykin.qualistrata.unix.session.factory;

import com.bavykin.qualistrata.unix.session.DirectUnixSessionProvider;
import com.bavykin.qualistrata.unix.session.PooledUnixSessionProvider;
import com.bavykin.qualistrata.unix.session.UnixHostSessionFactory;
import com.bavykin.qualistrata.unix.session.UnixSessionException;
import com.bavykin.qualistrata.unix.session.UnixSessionProvider;
import com.bavykin.qualistrata.unix.session.pool.SimpleUnixHostSessionPool;
import com.bavykin.qualistrata.unix.ssh.SshConnectionFactory;
import com.bavykin.qualistrata.unix.ssh.SshUnixHostSessionFactory;
import com.bavykin.qualistrata.unix.ssh.jsch.JschSshConnectionFactory;

/**
 * Factory for creating ready-to-use Unix session providers.
 *
 * <p>This factory hides the low-level wiring required to build direct or pooled
 * Unix session providers. It allows consuming projects to depend on the generic
 * {@link UnixSessionProvider} contract instead of manually assembling SSH
 * connection factories, Unix host session factories, pools, and providers.</p>
 *
 * <p>The default factory uses the JSch-based SSH transport implementation.</p>
 */
public class UnixSessionProviderFactory {

    private final UnixHostSessionFactory hostSessionFactory;

    /**
     * Creates a Unix session provider factory backed by the default JSch SSH
     * implementation.
     */
    public UnixSessionProviderFactory() {
        this(new JschSshConnectionFactory());
    }

    /**
     * Creates a Unix session provider factory backed by a custom SSH connection
     * factory.
     *
     * <p>This constructor is useful when a consumer wants to use the standard
     * SSH-backed Unix session implementation but provide its own
     * {@link SshConnectionFactory}, for example for testing or for an alternative
     * SSH transport implementation.</p>
     *
     * @param connectionFactory SSH connection factory.
     */
    public UnixSessionProviderFactory(SshConnectionFactory connectionFactory) {
        this(new SshUnixHostSessionFactory(connectionFactory));
    }

    /**
     * Creates a Unix session provider factory backed by a custom Unix host
     * session factory.
     *
     * <p>This is the most flexible constructor. It allows consumers and tests to
     * provide any implementation of {@link UnixHostSessionFactory}, including
     * local, fake, embedded, or alternative SSH-based factories.</p>
     *
     * @param hostSessionFactory Unix host session factory.
     */
    public UnixSessionProviderFactory(UnixHostSessionFactory hostSessionFactory) {
        if (hostSessionFactory == null) {
            throw new UnixSessionException("Unix host session factory must not be null.");
        }

        this.hostSessionFactory = hostSessionFactory;
    }

    /**
     * Creates a Unix session provider of the requested type.
     *
     * @param type provider type.
     * @return configured Unix session provider.
     */
    public UnixSessionProvider create(UnixSessionProviderType type) {
        if (type == null) {
            throw new UnixSessionException("Unix session provider type must not be null.");
        }

        return switch (type) {
            case DIRECT -> createDirect();
            case POOLED -> createPooled();
        };
    }

    /**
     * Creates a direct Unix session provider.
     *
     * @return direct Unix session provider.
     */
    public UnixSessionProvider createDirect() {
        return new DirectUnixSessionProvider(hostSessionFactory);
    }

    /**
     * Creates a pooled Unix session provider.
     *
     * @return pooled Unix session provider.
     */
    public UnixSessionProvider createPooled() {
        return new PooledUnixSessionProvider(
                new SimpleUnixHostSessionPool(hostSessionFactory)
        );
    }
}
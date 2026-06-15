package com.bavykin.qualistrata.unix.ssh;

/**
 * Factory for opening low-level SSH connections.
 *
 * <p>This contract intentionally does not expose any concrete SSH library
 * classes. Higher-level Unix session factories can depend on this abstraction
 * and remain independent from the selected SSH implementation.</p>
 */
public interface SshConnectionFactory {

    /**
     * Opens a new SSH connection.
     *
     * @param request SSH connection request.
     * @return opened SSH connection.
     * @throws SshConnectionException if the connection cannot be opened.
     */
    SshConnection open(SshConnectionRequest request);
}
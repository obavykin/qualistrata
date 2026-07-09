/**
 * High-level Unix automation API.
 *
 * <p>This package contains the recommended consumer-facing entry point for Unix
 * automation: {@link com.bavykin.qualistrata.unix.facade.UnixFacade}.</p>
 *
 * <p>The facade allows consuming projects to open Unix sessions and execute
 * commands by logical host alias from resolved target configuration. It hides
 * lower-level request creation, provider creation, SSH transport wiring, and
 * pooling details.</p>
 *
 * <p>Most consumers should start from this package unless they need advanced
 * control over session providers, pools, or transport implementations.</p>
 */
package com.bavykin.qualistrata.unix.facade;
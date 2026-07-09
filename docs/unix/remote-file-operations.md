# Unix remote file operations

`qualistrata-unix` provides generic remote file primitives used by higher-level integration layers.

The API is intentionally consumer-project agnostic. TNT-specific dataset upload, archive verification, batch utility execution, and DB validation must be implemented in the TNT integration layer on top of these primitives.

## Core contract

`RemoteFileClient` currently supports:

```text
uploadFile(localFile, remotePath)
exists(remotePath)
size(remotePath)
checksumSha256(remotePath)
list(remoteDirectory)
cleanDirectory(remoteDirectory, globPattern)
isDirectoryEmpty(remoteDirectory)
```

## Implementations

Current implementations:

```text
LocalRemoteFileClient
  Local filesystem implementation for unit tests and local diagnostics.

JschSftpRemoteFileClient
  Real SFTP-backed implementation using the existing JSch SSH dependency.
```

## Cleanup safety

The generic layer rejects obviously unsafe cleanup directories such as:

```text
/
.
..
paths containing /../
```

Consumer projects still own their environment-specific cleanup policy. TNT automation does not target production environments, but cleanup should remain explicit and reportable.

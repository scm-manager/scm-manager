# Repository mirrors

The core mirror command synchronizes an existing source repository into a destination of the same
repository type. Check `RepositoryService.isSupported(Command.MIRROR)` before obtaining the
`MirrorCommandBuilder` with `RepositoryService.getMirrorCommand()`.

## Command lifecycle and configuration

Use `initialCall()` for the initial synchronization into an empty destination and `update()` for
subsequent synchronizations. Supply the source URL with `setSourceUrl()` on every invocation.
Credentials, a per-command proxy configuration and a progress callback can also be supplied through
the builder. The result reports the synchronization status, elapsed time and command log.
Mirror configuration, scheduling and REST resources belong to the repository mirror plugin.

## Source URL changes

A repository advertising `Feature.MIRROR_SOURCE_URL_CHANGE` supports using a different URL on
subsequent updates. Check this with `RepositoryService.isSupported(Feature.MIRROR_SOURCE_URL_CHANGE)`.
Git advertises this capability; Subversion does not. Callers must check the capability rather than
infer support from the repository type.

## Git

### Synchronization and filtering

Changing the URL uses ordinary synchronization: Git fetches branches and tags from the supplied
source, including unrelated histories, and applies the configured filters. Filters can reject updates
or deletions and retain destination refs. Credentials, proxy settings and LFS options still apply.
No separate reset or initial synchronization is needed, and the Git implementation does not persist
the source URL.

### Default branch

Every successful synchronization follows the source's advertised HEAD branch if that branch exists
in the effective result after filtering. This also applies when only the source HEAD changes.
If source HEAD is missing, detached or unavailable after filtering, Git retains the destination's
current default branch if it survives. Otherwise it selects the alphabetically first remaining branch.
Changing the default does not delete the previous default branch unless synchronization removes it.

All original branches may be replaced in one update, provided at least one branch remains after
filtering. An update leaving no branches is rejected before publishing refs or changing destination
HEAD. Empty and tags-only mirrors remain unsupported.

### Temporary ref cleanup

Refs excluded by the filter or rejected on creation are removed from the temporary working copy
before LFS scanning and publishing. Rejected updates to existing refs restore their previous value.
If JGit refuses deletion of the current branch, cleanup is retried after moving HEAD to the selected branch. Failed ref deletion aborts synchronization before changing destination refs
or default-branch metadata.

### Source HEAD discovery

When a fetch omits HEAD from its advertised refs, synchronization queries the source's refs separately.
This is necessary for subsequent fetches over Git protocol v2, which may advertise only branches and tags.
The lookup uses the same credentials, proxy and HTTP settings as the fetch. A transport failure aborts
synchronization before publishing changes. A missing or detached HEAD still uses the fallback above.
A branch retained after a rejected update remains eligible as the default, pointing to its retained commit.

## Subversion (SVN)

The initial synchronization uses SVNKit's complete synchronization to copy the source revision
history into an empty destination. Subsequent updates resume synchronization from the destination's
recorded progress and copy new revisions. If `update()` is called on a destination that has not yet
been initialized for synchronization, it falls back to complete synchronization.

SVN records the source in the destination's synchronization metadata. Updates use that recorded
source, so changing `setSourceUrl()` does not relocate an initialized mirror. Keep supplying the
original source URL; it is also used to configure authentication and query the source revision.
`Feature.MIRROR_SOURCE_URL_CHANGE` is not supported.

SVN synchronization copies repository revisions rather than selecting Git refs. Directories such
as `trunk`, `branches` and `tags` are part of that history. Git branch/tag filters, LFS options and
symbolic HEAD/default-branch selection do not apply to SVN mirrors.

Username/password credentials, PKCS#12 client certificates and proxy settings are supported.
Progress callbacks report revision synchronization, and the result log reports the destination's
revision before and after the update. SVN errors produce a failed result with an error message.

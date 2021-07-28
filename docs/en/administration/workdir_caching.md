---
title: Caching for Working Directories
---

SCM-Manager offers commands to modify repositories on the server side. For example this is used by the
[Editor Plugin](https://www.scm-manager.org/plugins/scm-editor-plugin/) and the
[Review Plugin](https://www.scm-manager.org/plugins/scm-review-plugin/). Without further configuration, this is done
by cloning/checking out the repository temporarily, performing the change, creating a commit and pushing the changes
back to the central repository. The larger the repositories, the longer this may take.

To speed up such changes a lot, SCM-Manager offers a strategy where the local clones will be cached and reused for
subsequent requests. This strategy caches up to a configurable amount of clones (but at most one per repository).
To enable this strategy, add the system property `scm.workingCopyPoolStrategy` to the value 
`sonia.scm.repository.work.SimpleCachingWorkingCopyPool`:

```bash
-Dscm.workingCopyPoolStrategy=sonia.scm.repository.work.SimpleCachingWorkingCopyPool
```

The maximum capacity of the cache can be set using the property `scm.workingCopyPoolSize` (the default is 5).

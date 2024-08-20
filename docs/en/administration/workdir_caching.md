---
title: Caching for Working Directories
---

SCM-Manager offers commands to modify repositories on the server side. For example this is used by the
[Editor Plugin](https://scm-manager.org/plugins/scm-editor-plugin/) and the
[Review Plugin](https://scm-manager.org/plugins/scm-review-plugin/). Without further configuration, this is done
by cloning/checking out the repository temporarily, performing the change, creating a commit and pushing the changes
back to the central repository. The larger the repositories, the longer this may take.

To speed up such changes a lot, SCM-Manager offers a strategy where the local clones will be cached and reused for
subsequent requests. This strategy caches up to a configurable amount of clones (but at most one per repository).
To enable this strategy you can change the `config.yml` or set the corresponding environment variable.
The maximum capacity of the cache can also be set via `config.yml` or corresponding environment variable (the default value is 5).

**config.yml**

```yaml
webapp:
  # ...
  workingCopyPoolStrategy: sonia.scm.repository.work.SimpleCachingWorkingCopyPool
  workingCopyPoolSize: 5
  # ...
```

```bash
export SCM_WEBAPP_WORKINGCOPYPOOLSTRATEGY=sonia.scm.repository.work.SimpleCachingWorkingCopyPool
export SCM_WEBAPP_WORKINGCOPYPOOLSIZE=5
```

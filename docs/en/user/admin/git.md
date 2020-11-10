---
title: Administration
subtitle: Git
---
In the git section there are the following git specific settings:

- GC Cron Expression

    If this is set, SCM-Manager will execute a git garbage collection matching the given
    [cron expression](https://en.wikipedia.org/wiki/Cron#CRON_expression).

- Disable Non Fast-Forward

    Activate this to reject forced pushes that are not fast forwards.

- Default Branch

    The branch name configured here will be used for the initialization of new repositories.
    Please mind, that due to git internals this cannot work for empty repositories (here git
    will always use its internal default branch, so at the time being `master`).

![Administration-Plugins-Installed](assets/administration-settings-git.png)

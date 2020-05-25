---
title: Known Issues
---

## Asnychronous PreReceiveRepositoryHooks do not work with subversion

The following example will fail to log the changesets. 

```java
import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.api.HookChangesetBuilder;

@Extension
@EagerSingleton
public class DemoHook {

  private static final Logger LOG = LoggerFactory.getLogger(DemoHook.class);

  @Subscribe
  public void handleEvent(PreReceiveRepositoryHookEvent event) {
    HookChangesetBuilder changesetProvider = event.getContext().getChangesetProvider();
    for (Changeset c : changesetProvider.getChangesets()) {
      LOG.warn("received {} hook for changeset: {}", event.getType(), c.getId());
    }
  }

}
```

This is because of the transaction management of subversion. The scm-manager subversion provider becomes a transaction id for the changes of the current push, but the transaction has finished before scm-manager can resolve the incoming commit. To solve the issue, we could use a synchronous subscription instead e.g.:

```java
import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.api.HookChangesetBuilder;

@Extension
@EagerSingleton
public class DemoHook {

  private static final Logger LOG = LoggerFactory.getLogger(DemoHook.class);

  @Subscribe(async = false)
  public void handleEvent(PreReceiveRepositoryHookEvent event) {
    HookChangesetBuilder changesetProvider = event.getContext().getChangesetProvider();
    for (Changeset c : changesetProvider.getChangesets()) {
      LOG.warn("received {} hook for changeset: {}", event.getType(), c.getId());
    }
  }

}
```

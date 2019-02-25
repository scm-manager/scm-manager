package sonia.scm.api.v2.resources;

import com.github.legman.Subscribe;
import sonia.scm.EagerSingleton;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.ClearRepositoryCacheEvent;

import java.util.Objects;

@EagerSingleton @Extension
public class GitRepositoryConfigChangeClearRepositoryCacheListener {
  @Subscribe
  public void sendClearRepositoryCacheEvent(GitRepositoryConfigChangedEvent event) {
    if (!Objects.equals(event.getOldConfig().getDefaultBranch(), event.getNewConfig().getDefaultBranch())) {
      ScmEventBus.getInstance().post(new ClearRepositoryCacheEvent(event.getRepository()));
    }
  }
}

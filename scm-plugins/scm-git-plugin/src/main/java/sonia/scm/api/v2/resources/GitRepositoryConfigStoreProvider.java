package sonia.scm.api.v2.resources;

import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;

public class GitRepositoryConfigStoreProvider {

  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public GitRepositoryConfigStoreProvider(ConfigurationStoreFactory configurationStoreFactory) {
    this.configurationStoreFactory = configurationStoreFactory;
  }

  public ConfigurationStore<GitRepositoryConfig> get(Repository repository) {
    return new StoreWrapper(configurationStoreFactory.withType(GitRepositoryConfig.class).withName("gitConfig").forRepository(repository).build(), repository);
  }

  private static class StoreWrapper implements ConfigurationStore<GitRepositoryConfig> {

    private final ConfigurationStore<GitRepositoryConfig> delegate;
    private final Repository repository;

    private StoreWrapper(ConfigurationStore<GitRepositoryConfig> delegate, Repository repository) {
      this.delegate = delegate;
      this.repository = repository;
    }

    @Override
    public GitRepositoryConfig get() {
      GitRepositoryConfig config = delegate.get();
      if (config == null) {
        return new GitRepositoryConfig();
      }
      return config;
    }

    @Override
    public void set(GitRepositoryConfig newConfig) {
      GitRepositoryConfig oldConfig = get();
      delegate.set(newConfig);
      ScmEventBus.getInstance().post(new GitRepositoryConfigChangedEvent(repository, oldConfig, newConfig));
    }
  }
}

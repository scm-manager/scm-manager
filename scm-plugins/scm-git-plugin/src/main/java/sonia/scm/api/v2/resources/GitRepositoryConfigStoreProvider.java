package sonia.scm.api.v2.resources;

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
    return configurationStoreFactory.withType(GitRepositoryConfig.class).withName("gitConfig").forRepository(repository).build();
  }
}

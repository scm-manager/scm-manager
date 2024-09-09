/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

public class GitRepositoryConfigStoreProvider {

  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public GitRepositoryConfigStoreProvider(ConfigurationStoreFactory configurationStoreFactory) {
    this.configurationStoreFactory = configurationStoreFactory;
  }

  public ConfigurationStore<GitRepositoryConfig> get(Repository repository) {
    return new StoreWrapper(createStore(repository.getId()), repository);
  }

  public GitRepositoryConfig getGitRepositoryConfig(String repositoryId) {
    return getFromStore(createStore(repositoryId));
  }

  public void setDefaultBranch(Repository repository, String newDefaultBranch) {
    ConfigurationStore<GitRepositoryConfig> configStore = get(repository);
    GitRepositoryConfig gitRepositoryConfig = configStore
      .getOptional()
      .orElse(new GitRepositoryConfig());
    gitRepositoryConfig.setDefaultBranch(newDefaultBranch);
    configStore.set(gitRepositoryConfig);
  }

  private static GitRepositoryConfig getFromStore(ConfigurationStore<GitRepositoryConfig> store) {
    return store.getOptional().orElse(new GitRepositoryConfig());
  }

  private ConfigurationStore<GitRepositoryConfig> createStore(String id) {
    return configurationStoreFactory
      .withType(GitRepositoryConfig.class)
      .withName("gitConfig")
      .forRepository(id).build();
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
      return getFromStore(delegate);
    }

    @Override
    public void set(GitRepositoryConfig newConfig) {
      GitRepositoryConfig oldConfig = get();
      delegate.set(newConfig);
      ScmEventBus.getInstance().post(new GitRepositoryConfigChangedEvent(repository, oldConfig, newConfig));
    }
  }
}

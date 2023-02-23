/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

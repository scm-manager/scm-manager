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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

/**
 * @author Sebastian Sdorra
 */
@Extension
public class GitRepositoryServiceResolver implements RepositoryServiceResolver {

  private final GitRepositoryHandler handler;
  private final GitRepositoryConfigStoreProvider storeProvider;
  private final LfsBlobStoreFactory lfsBlobStoreFactory;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;
  private final SyncAsyncExecutorProvider executorProvider;
  private final CacheManager cacheManager;

  @Inject
  public GitRepositoryServiceResolver(GitRepositoryHandler handler, GitRepositoryConfigStoreProvider storeProvider, LfsBlobStoreFactory lfsBlobStoreFactory, HookContextFactory hookContextFactory, ScmEventBus eventBus, SyncAsyncExecutorProvider executorProvider, CacheManager cacheManager) {
    this.handler = handler;
    this.storeProvider = storeProvider;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.executorProvider = executorProvider;
    this.cacheManager = cacheManager;
  }

  @Override
  public GitRepositoryServiceProvider resolve(Repository repository) {
    GitRepositoryServiceProvider provider = null;

    if (GitRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType())) {
      provider = new GitRepositoryServiceProvider(handler, repository, storeProvider, lfsBlobStoreFactory, hookContextFactory, eventBus, executorProvider, cacheManager);
    }

    return provider;
  }
}

/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

/**
 *
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

  @Inject
  public GitRepositoryServiceResolver(GitRepositoryHandler handler, GitRepositoryConfigStoreProvider storeProvider, LfsBlobStoreFactory lfsBlobStoreFactory, HookContextFactory hookContextFactory, ScmEventBus eventBus, SyncAsyncExecutorProvider executorProvider) {
    this.handler = handler;
    this.storeProvider = storeProvider;
    this.lfsBlobStoreFactory = lfsBlobStoreFactory;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
    this.executorProvider = executorProvider;
  }

  @Override
  public GitRepositoryServiceProvider resolve(Repository repository) {
    GitRepositoryServiceProvider provider = null;

    if (GitRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType())) {
      provider = new GitRepositoryServiceProvider(handler, repository, storeProvider, lfsBlobStoreFactory, hookContextFactory, eventBus, executorProvider);
    }

    return provider;
  }
}

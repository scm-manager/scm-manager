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

package sonia.scm.protocolcommand.git;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.web.CollectingPackParserListener;
import sonia.scm.web.GitReceiveHook;

public abstract class BaseReceivePackFactory<T> implements ReceivePackFactory<T> {

  private final GitChangesetConverterFactory converterFactory;
  private final GitRepositoryHandler handler;
  private final HookEventFacade hookEventFacade;
  private final GitRepositoryConfigStoreProvider storeProvider;

  protected BaseReceivePackFactory(GitChangesetConverterFactory converterFactory,
                                   GitRepositoryHandler handler,
                                   HookEventFacade hookEventFacade,
                                   GitRepositoryConfigStoreProvider storeProvider) {
    this.converterFactory = converterFactory;
    this.handler = handler;
    this.hookEventFacade = hookEventFacade;
    this.storeProvider = storeProvider;
  }

  @Override
  public final ReceivePack create(T connection, Repository repository) throws ServiceNotAuthorizedException, ServiceNotEnabledException {
    ReceivePack receivePack = createBasicReceivePack(connection, repository);
    receivePack.setAllowNonFastForwards(isNonFastForwardAllowed(repository));

    GitReceiveHook hook = new GitReceiveHook(converterFactory, hookEventFacade, handler);
    receivePack.setPreReceiveHook(hook);
    receivePack.setPostReceiveHook(hook);
    // apply collecting listener, to be able to check which commits are new
    CollectingPackParserListener.set(receivePack, hook);

    return receivePack;
  }

  protected abstract ReceivePack createBasicReceivePack(T request, Repository repository)
    throws ServiceNotEnabledException, ServiceNotAuthorizedException;

  private boolean isNonFastForwardAllowed(Repository repository) {
    String repositoryId = handler.getRepositoryId(repository.getConfig());
    GitRepositoryConfig gitRepositoryConfig = storeProvider.getGitRepositoryConfig(repositoryId);
    return !(handler.getConfig().isNonFastForwardDisallowed() || gitRepositoryConfig.isNonFastForwardDisallowed());
  }
}

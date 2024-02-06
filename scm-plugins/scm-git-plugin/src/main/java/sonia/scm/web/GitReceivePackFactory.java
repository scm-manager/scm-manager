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

package sonia.scm.web;


import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.http.server.resolver.DefaultReceivePackFactory;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.protocolcommand.git.BaseReceivePackFactory;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;

/**
 * GitReceivePackFactory creates {@link ReceivePack} objects and assigns the required
 * Hook components.
 *
 */
public class GitReceivePackFactory extends BaseReceivePackFactory<HttpServletRequest>
{

  private ReceivePackFactory<HttpServletRequest> wrapped;

  @Inject
  public GitReceivePackFactory(GitChangesetConverterFactory converterFactory,
                               GitRepositoryHandler handler,
                               HookEventFacade hookEventFacade,
                               GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider) {
    super(converterFactory, handler, hookEventFacade, gitRepositoryConfigStoreProvider);
    this.wrapped = new DefaultReceivePackFactory();
  }

  @Override
  protected ReceivePack createBasicReceivePack(HttpServletRequest request, Repository repository)
    throws ServiceNotEnabledException, ServiceNotAuthorizedException {
    return wrapped.create(request, repository);
  }
}

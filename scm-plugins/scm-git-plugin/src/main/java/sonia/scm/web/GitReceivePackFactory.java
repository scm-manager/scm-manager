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

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

package sonia.scm.repository.spi;

import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.junit.rules.ExternalResource;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;

import static com.google.inject.util.Providers.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BindTransportProtocolRule extends ExternalResource {

  private ScmTransportProtocol scmTransportProtocol;

  RepositoryManager repositoryManager = mock(RepositoryManager.class);
  HookEventFacade hookEventFacade;

  @Override
  protected void before() {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    hookEventFacade = new HookEventFacade(of(repositoryManager), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    scmTransportProtocol = new ScmTransportProtocol(of(GitTestHelper.createConverterFactory()), of(hookEventFacade), of(gitRepositoryHandler));

    Transport.register(scmTransportProtocol);

    when(gitRepositoryHandler.getRepositoryId(any())).thenReturn("1");
    when(repositoryManager.get("1")).thenReturn(new sonia.scm.repository.Repository());
  }

  @Override
  protected void after() {
    Transport.unregister(scmTransportProtocol);
  }
}

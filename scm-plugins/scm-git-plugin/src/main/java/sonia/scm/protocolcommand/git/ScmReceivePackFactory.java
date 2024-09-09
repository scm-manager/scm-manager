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

package sonia.scm.protocolcommand.git;

import com.google.inject.Inject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.protocolcommand.RepositoryContext;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;

public class ScmReceivePackFactory extends BaseReceivePackFactory<RepositoryContext> {

  @Inject
  public ScmReceivePackFactory(GitChangesetConverterFactory converterFactory,
                               GitRepositoryHandler handler,
                               HookEventFacade hookEventFacade,
                               GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider) {
    super(converterFactory, handler, hookEventFacade, gitRepositoryConfigStoreProvider);
  }

  @Override
  protected ReceivePack createBasicReceivePack(RepositoryContext repositoryContext, Repository repository) {
    return new ReceivePack(repository);
  }
}

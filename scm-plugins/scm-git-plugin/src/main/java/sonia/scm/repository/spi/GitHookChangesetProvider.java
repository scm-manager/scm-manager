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

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.api.HookChangesetProvider;

import java.util.List;


public class GitHookChangesetProvider implements HookChangesetProvider {

  private final GitChangesetConverterFactory converterFactory;
  private final ReceivePack receivePack;
  private final List<ReceiveCommand> receiveCommands;

  private HookChangesetResponse response;

  public GitHookChangesetProvider(GitChangesetConverterFactory converterFactory, ReceivePack receivePack,
                                  List<ReceiveCommand> receiveCommands) {
    this.converterFactory = converterFactory;
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
  }

  @Override
  public synchronized HookChangesetResponse handleRequest(HookChangesetRequest request) {
    if (response == null) {
      GitHookChangesetCollector collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, receivePack);
      response = new HookChangesetResponse(collector.getAddedChangesets(), collector.getRemovedChangesets());
    }
    return response;
  }
}

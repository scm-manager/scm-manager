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

package sonia.scm.repository.api;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.spi.GitLogComputer;
import sonia.scm.repository.spi.HookMergeDetectionProvider;
import sonia.scm.repository.spi.LogCommandRequest;

import java.util.List;

public class GitReceiveHookMergeDetectionProvider implements HookMergeDetectionProvider {
  private final Repository repository;
  private final String repositoryId;
  private final List<ReceiveCommand> receiveCommands;
  private final GitChangesetConverterFactory converterFactory;

  public GitReceiveHookMergeDetectionProvider(Repository repository, String repositoryId, List<ReceiveCommand> receiveCommands, GitChangesetConverterFactory converterFactory) {
    this.repository = repository;
    this.repositoryId = repositoryId;
    this.receiveCommands = receiveCommands;
    this.converterFactory = converterFactory;
  }

  @Override
  public boolean branchesMerged(String target, String branch) {
    LogCommandRequest request = new LogCommandRequest();
    request.setBranch(findRelevantRevisionForBranchIfToBeUpdated(branch));
    request.setAncestorChangeset(findRelevantRevisionForBranchIfToBeUpdated(target));
    request.setPagingLimit(1);

    return new GitLogComputer(repositoryId, repository, converterFactory).compute(request).getTotal() == 0;
  }

  private String findRelevantRevisionForBranchIfToBeUpdated(String branch) {
    return receiveCommands
      .stream()
      .filter(receiveCommand -> isReceiveCommandForBranch(branch, receiveCommand))
      .map(this::getRelevantRevision)
      .map(AnyObjectId::getName)
      .findFirst()
      .orElse(branch);
  }

  private boolean isReceiveCommandForBranch(String branch, ReceiveCommand receiveCommand) {
    return receiveCommand.getType() != ReceiveCommand.Type.CREATE
      && GitUtil.getBranch(receiveCommand.getRef()).equals(branch);
  }

  private ObjectId getRelevantRevision(ReceiveCommand receiveCommand) {
    if (receiveCommand.getType() == ReceiveCommand.Type.DELETE) {
      return receiveCommand.getOldId();
    } else {
      return receiveCommand.getNewId();
    }
  }
}

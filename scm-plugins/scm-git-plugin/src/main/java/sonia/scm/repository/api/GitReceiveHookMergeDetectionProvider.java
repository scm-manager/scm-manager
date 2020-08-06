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

package sonia.scm.repository.api;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.spi.GitLogComputer;
import sonia.scm.repository.spi.HookMergeDetectionProvider;
import sonia.scm.repository.spi.LogCommandRequest;

import java.util.List;

public class GitReceiveHookMergeDetectionProvider implements HookMergeDetectionProvider {
  private final Repository repository;
  private final String repositoryId;
  private final List<ReceiveCommand> receiveCommands;

  public GitReceiveHookMergeDetectionProvider(Repository repository, String repositoryId, List<ReceiveCommand> receiveCommands) {
    this.repository = repository;
    this.repositoryId = repositoryId;
    this.receiveCommands = receiveCommands;
  }

  @Override
  public boolean branchesMerged(String target, String branch) {
    LogCommandRequest request = new LogCommandRequest();
    request.setBranch(findRelevantRevisionForBranchIfToBeUpdated(branch));
    request.setAncestorChangeset(findRelevantRevisionForBranchIfToBeUpdated(target));
    request.setPagingLimit(1);

    return new GitLogComputer(repositoryId, repository).compute(request).getTotal() == 0;
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

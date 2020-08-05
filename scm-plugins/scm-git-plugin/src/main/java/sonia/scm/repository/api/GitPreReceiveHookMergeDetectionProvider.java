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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.spi.GitLogComputer;
import sonia.scm.repository.spi.HookMergeDetectionProvider;
import sonia.scm.repository.spi.LogCommandRequest;

import java.util.List;

public class GitPreReceiveHookMergeDetectionProvider implements HookMergeDetectionProvider {
  private final List<ReceiveCommand> receiveCommands;
  private final Repository repository;
  private final String repositoryId;

  public GitPreReceiveHookMergeDetectionProvider(List<ReceiveCommand> receiveCommands, Repository repository, String repositoryId) {
    this.receiveCommands = receiveCommands;
    this.repository = repository;
    this.repositoryId = repositoryId;
  }

  @Override
  public boolean branchesMerged(String target, String branch) {

    String sourceToUse = receiveCommands.stream().filter(receiveCommand -> GitUtil.getBranch(receiveCommand.getRef()).equals(branch)).findFirst().map(ReceiveCommand::getNewId).map(AnyObjectId::getName).orElse(branch);
    String targetToUse = receiveCommands.stream().filter(receiveCommand -> GitUtil.getBranch(receiveCommand.getRef()).equals(target)).findFirst().map(ReceiveCommand::getNewId).map(AnyObjectId::getName).orElse(target);

    LogCommandRequest request = new LogCommandRequest();
    request.setBranch(sourceToUse);
    request.setAncestorChangeset(targetToUse);
    request.setPagingLimit(1);

    ChangesetPagingResult changesets = new GitLogComputer(repositoryId, repository).compute(request);
    return changesets.getTotal() == 0;
  }
}

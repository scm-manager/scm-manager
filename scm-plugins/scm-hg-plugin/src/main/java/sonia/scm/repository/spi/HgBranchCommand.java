/**
 * Copyright (c) 2014, Sebastian Sdorra
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

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.CommitCommand;
import com.aragost.javahg.commands.UpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.util.WorkingCopy;

import java.io.IOException;

/**
 * Mercurial implementation of the {@link BranchCommand}.
 * Note that this creates an empty commit to "persist" the new branch.
 */
public class HgBranchCommand extends AbstractCommand implements BranchCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgBranchCommand.class);

  private final HgWorkdirFactory workdirFactory;

  HgBranchCommand(HgCommandContext context, Repository repository, HgWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public Branch branch(BranchRequest request) throws IOException {
    try (WorkingCopy<RepositoryCloseableWrapper> workingCopy = workdirFactory.createWorkingCopy(getContext())) {
      com.aragost.javahg.Repository repository = workingCopy.get().get();
      if (request.getParentBranch() != null) {
        UpdateCommand.on(repository).rev(request.getParentBranch()).execute();
      }
      com.aragost.javahg.commands.BranchCommand.on(repository).set(request.getNewBranch());

      Changeset emptyChangeset = CommitCommand
        .on(repository)
        .user("SCM-Manager")
        .message("Create new branch " + request.getNewBranch())
        .execute();

      LOG.debug("Created new branch '{}' in repository {} with changeset {}",
        request.getNewBranch(), getRepository().getNamespaceAndName(), emptyChangeset.getNode());

      return Branch.normalBranch(request.getNewBranch(), emptyChangeset.getNode());
    }
  }
}

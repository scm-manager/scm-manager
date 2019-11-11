/**
 * Copyright (c) 2010, Sebastian Sdorra
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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import sonia.scm.repository.Branch;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.util.WorkingCopy;

import java.io.IOException;
import java.util.stream.StreamSupport;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class GitBranchCommand extends AbstractGitCommand implements BranchCommand {

  private final GitWorkdirFactory workdirFactory;

  GitBranchCommand(GitContext context, Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public Branch branch(BranchRequest request) {
    try (WorkingCopy<org.eclipse.jgit.lib.Repository, org.eclipse.jgit.lib.Repository> workingCopy = workdirFactory.createWorkingCopy(context, request.getParentBranch())) {
      Git clone = new Git(workingCopy.getWorkingRepository());
      Ref ref = clone.branchCreate().setName(request.getNewBranch()).call();
      Iterable<PushResult> call = clone.push().add(request.getNewBranch()).call();
      StreamSupport.stream(call.spliterator(), false)
        .flatMap(pushResult -> pushResult.getRemoteUpdates().stream())
        .filter(remoteRefUpdate -> remoteRefUpdate.getStatus() != RemoteRefUpdate.Status.OK)
        .findFirst()
        .ifPresent(r -> this.handlePushError(r, request, context.getRepository()));
      return Branch.normalBranch(request.getNewBranch(), GitUtil.getId(ref.getObjectId()));
    } catch (GitAPIException ex) {
      throw new InternalRepositoryException(repository, "could not create branch " + request.getNewBranch(), ex);
    }
  }

  @Override
  public void delete(String branchName) {
    try (Git gitRepo = new Git(context.open())) {
      gitRepo
        .branchDelete()
        .setBranchNames(branchName)
        .setForce(true)
        .call();
    } catch (GitAPIException | IOException ex) {
      throw new InternalRepositoryException(entity(context.getRepository()), String.format("Could not delete branch: %s", branchName));
    }
  }

  private void handlePushError(RemoteRefUpdate remoteRefUpdate, BranchRequest request, Repository repository) {
    if (remoteRefUpdate.getStatus() != RemoteRefUpdate.Status.OK) {
      // TODO handle failed remote update
      throw new IntegrateChangesFromWorkdirException(repository,
        String.format("Could not push new branch '%s' into central repository", request.getNewBranch()));
    }
  }
}

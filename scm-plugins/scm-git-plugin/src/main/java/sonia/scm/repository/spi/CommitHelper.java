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

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Signer;
import org.eclipse.jgit.lib.Signers;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Slf4j
class CommitHelper {

  private final Repository repository;
  private final GitContext context;
  private final RepositoryManager repositoryManager;
  private final GitRepositoryHookEventFactory eventFactory;

  CommitHelper(GitContext context, RepositoryManager repositoryManager, GitRepositoryHookEventFactory eventFactory) {
    this.repository = context.open();
    this.context = context;
    this.repositoryManager = repositoryManager;
    this.eventFactory = eventFactory;
  }

  ObjectId createCommit(ObjectId treeId,
                        Person author,
                        Person committer,
                        String message,
                        boolean sign,
                        ObjectId... parentCommitIds) throws IOException, CanceledException, UnsupportedSigningFormatException {
    log.trace("create commit for tree {} and parent ids {} in repository {}", treeId, parentCommitIds, context.getRepository());
    try (ObjectInserter inserter = repository.newObjectInserter()) {
      CommitBuilder commitBuilder = new CommitBuilder();
      commitBuilder.setTreeId(treeId);
      commitBuilder.setParentIds(parentCommitIds);
      commitBuilder.setAuthor(createPersonIdent(author));
      commitBuilder.setCommitter(createPersonIdent(committer));
      commitBuilder.setMessage(message);
      if (sign) {
        sign(commitBuilder, createPersonIdent(committer));
      }
      ObjectId commitId = inserter.insert(commitBuilder);
      inserter.flush();
      log.trace("created commit with id {}", commitId);
      return commitId;
    }
  }

  private PersonIdent createPersonIdent(Person person) {
    if (person == null) {
      User currentUser = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
      return new PersonIdent(currentUser.getDisplayName(), currentUser.getMail());
    }
    return new PersonIdent(person.getName(), person.getMail());
  }

  private void sign(CommitBuilder commit, PersonIdent committer)
    throws CanceledException, IOException, UnsupportedSigningFormatException {
    log.trace("sign commit");
    GpgConfig gpgConfig = new GpgConfig(repository.getConfig());
    Signer signer = Signers.get(gpgConfig.getKeyFormat());
    signer.signObject(repository, gpgConfig, commit, committer, "SCM-MANAGER-DEFAULT-KEY", CredentialsProvider.getDefault());
  }

  void updateBranch(String branchName, ObjectId newCommitId, ObjectId expectedOldObjectId) {
    log.trace("update branch {} with new commit id {} in repository {}", branchName, newCommitId, context.getRepository());
    try {
      RevCommit newCommit = findNewCommit(newCommitId);
      firePreCommitHook(branchName, newCommit);
      doUpdate(branchName, newCommitId, expectedOldObjectId);
      firePostCommitHook(branchName, newCommit);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not update branch " + branchName, e);
    }
  }

  private RevCommit findNewCommit(ObjectId newCommitId) throws IOException {
    RevCommit newCommit;
    try (RevWalk revWalk = new RevWalk(repository)) {
      newCommit = revWalk.parseCommit(newCommitId);
    }
    return newCommit;
  }

  private void firePreCommitHook(String branchName, RevCommit newCommit) {
    repositoryManager.fireHookEvent(
      eventFactory.createPreReceiveEvent(
        context,
        List.of(branchName),
        emptyList(),
        () -> List.of(newCommit)
      )
    );
  }

  private void doUpdate(String branchName, ObjectId newCommitId, ObjectId expectedOldObjectId) throws IOException {
    RefUpdate refUpdate = repository.updateRef(GitUtil.getRevString(branchName));
    if (newCommitId == null) {
      refUpdate.setExpectedOldObjectId(ObjectId.zeroId());
    } else {
      refUpdate.setExpectedOldObjectId(expectedOldObjectId);
    }
    refUpdate.setNewObjectId(newCommitId);
    refUpdate.setForceUpdate(false);
    RefUpdate.Result result = refUpdate.update();

    if (isSuccessfulUpdate(expectedOldObjectId, result)) {
      throw new ConcurrentModificationException(entity("branch", branchName).in(context.getRepository()).build());
    }
  }

  private void firePostCommitHook(String branchName, RevCommit newCommit) {
    repositoryManager.fireHookEvent(
      eventFactory.createPostReceiveEvent(
        context,
        List.of(branchName),
        emptyList(),
        () -> List.of(newCommit)
      )
    );
  }

  private boolean isSuccessfulUpdate(ObjectId expectedOldObjectId, RefUpdate.Result result) {
    return result != RefUpdate.Result.FAST_FORWARD && !(expectedOldObjectId == null && result == RefUpdate.Result.NEW);
  }
}

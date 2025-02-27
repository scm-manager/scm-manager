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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.RecursiveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.MultipleParentsNotAllowedException;
import sonia.scm.repository.NoParentException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.RevertCommandResult;

import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Slf4j
public class GitRevertCommand extends AbstractGitCommand implements RevertCommand {

  private final RepositoryManager repositoryManager;
  private final GitRepositoryHookEventFactory eventFactory;

  @Inject
  GitRevertCommand(@Assisted GitContext context, RepositoryManager repositoryManager, GitRepositoryHookEventFactory eventFactory) {
    super(context);
    this.repositoryManager = repositoryManager;
    this.eventFactory = eventFactory;
  }

  @Override
  public RevertCommandResult revert(RevertCommandRequest request) {
    log.debug("revert {} on {} in repository {}",
      request.getRevision(),
      request.getBranch().orElse("default branch"),
      repository.getName());

    try (Repository jRepository = context.open();
         RevWalk revWalk = new RevWalk(jRepository)) {

      ObjectId sourceRevision = getSourceRevision(request, jRepository, repository);
      ObjectId targetRevision = getTargetRevision(request, jRepository, repository);

      RevCommit parent = getParentRevision(revWalk, sourceRevision, jRepository);

      RecursiveMerger merger = (RecursiveMerger) MergeStrategy.RECURSIVE.newMerger(jRepository, true);
      merger.setBase(sourceRevision);

      boolean mergeSucceeded = merger.merge(targetRevision, parent);

      if (!mergeSucceeded) {
        log.info("revert merge fail: {} on {} in repository {}",
          sourceRevision.getName(), targetRevision.getName(), repository.getName());
        return RevertCommandResult.failure(MergeHelper.getFailingPaths(merger));
      }

      ObjectId oldTreeId = revWalk.parseCommit(targetRevision).getTree().toObjectId();
      ObjectId newTreeId = merger.getResultTreeId();
      if (oldTreeId.equals(newTreeId)) {
        throw new NoChangesMadeException(repository);
      }

      log.debug("revert {} on {} in repository {} successful, preparing commit",
        sourceRevision.getName(), targetRevision.getName(), repository.getName());
      CommitHelper commitHelper = new CommitHelper(context, repositoryManager, eventFactory);
      ObjectId commitId = commitHelper.createCommit(
        newTreeId,
        request.getAuthor(),
        request.getAuthor(),
        determineMessage(request, GitUtil.getCommit(jRepository, revWalk, sourceRevision)),
        request.isSign(),
        targetRevision
      );

      commitHelper.updateBranch(
        request.getBranch().orElseGet(() -> context.getConfig().getDefaultBranch()), commitId, targetRevision
      );

      return RevertCommandResult.success(commitId.getName());

    } catch (CanceledException | IOException | UnsupportedSigningFormatException e) {
      throw new RuntimeException(e);
    }
  }

  private ObjectId getSourceRevision(RevertCommandRequest request,
                                     Repository jRepository,
                                     sonia.scm.repository.Repository sRepository) throws IOException {
    ObjectId sourceRevision = GitUtil.getRevisionId(jRepository, request.getRevision());

    if (sourceRevision == null) {
      log.error("source revision not found!");
      throw NotFoundException.notFound(entity(ObjectId.class, request.getRevision()).in(sRepository));
    }

    log.debug("got source revision {} for repository {}", sourceRevision.getName(), jRepository.getIdentifier());
    return sourceRevision;
  }

  private ObjectId getTargetRevision(RevertCommandRequest request,
                                     Repository jRepository,
                                     sonia.scm.repository.Repository sRepository) throws IOException {
    if (request.getBranch().isEmpty() || request.getBranch().get().isEmpty()) {
      ObjectId targetRevision = GitUtil.getRepositoryHead(jRepository);
      log.debug("given target branch is empty, returning HEAD revision for repository {}", jRepository.getIdentifier());
      return targetRevision;
    }

    ObjectId targetRevision = GitUtil.getRevisionId(jRepository, request.getBranch().get());
    if (targetRevision == null) {
      log.error("target revision not found!");
      throw NotFoundException.notFound(entity(ObjectId.class, request.getBranch().get()).in(sRepository));
    }

    log.debug("got target revision {} for repository {}", targetRevision.getName(), jRepository.getIdentifier());
    return targetRevision;
  }

  private RevCommit getParentRevision(RevWalk revWalk, ObjectId sourceRevision, Repository jRepository) throws IOException {
    RevCommit source = revWalk.parseCommit(sourceRevision);
    int sourceParents = source.getParentCount();

    if (sourceParents == 0) {
      throw new NoParentException(sourceRevision.getName());
    } else if (sourceParents > 1) {
      throw new MultipleParentsNotAllowedException(sourceRevision.getName());
    }

    RevCommit parent = source.getParent(0);

    log.debug("got parent revision {} of revision {} for repository {}", parent.getName(), sourceRevision.getName(), jRepository.getIdentifier());
    return parent;
  }

  private String determineMessage(RevertCommandRequest request, RevCommit revertedCommit) {
    return request.getMessage().orElseGet(() -> {
      log.debug("no custom message given, choose default message");
      return String.format("""
      Revert "%s"
      
      This reverts commit %s.""", revertedCommit.getShortMessage(), revertedCommit.getId().getName());
    });
  }

  public interface Factory {
    RevertCommand create(GitContext context);
  }
}

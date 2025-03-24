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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.MergePreventReason;
import sonia.scm.repository.api.MergePreventReasonType;
import sonia.scm.repository.api.MergeStrategy;
import sonia.scm.repository.api.MergeStrategyNotSupportedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private final GitWorkingCopyFactory workingCopyFactory;
  private final AttributeAnalyzer attributeAnalyzer;
  private final RepositoryManager repositoryManager;
  private final GitRepositoryHookEventFactory eventFactory;

  private static final Set<MergeStrategy> STRATEGIES = Set.of(
    MergeStrategy.MERGE_COMMIT,
    MergeStrategy.FAST_FORWARD_IF_POSSIBLE,
    MergeStrategy.SQUASH,
    MergeStrategy.REBASE
  );

  @Inject
  GitMergeCommand(@Assisted GitContext context,
                  GitRepositoryHandler handler,
                  AttributeAnalyzer attributeAnalyzer,
                  RepositoryManager repositoryManager,
                  GitRepositoryHookEventFactory eventFactory) {
    this(context, handler.getWorkingCopyFactory(), attributeAnalyzer, repositoryManager, eventFactory);
  }

  GitMergeCommand(@Assisted GitContext context,
                  GitWorkingCopyFactory workingCopyFactory,
                  AttributeAnalyzer attributeAnalyzer,
                  RepositoryManager repositoryManager,
                  GitRepositoryHookEventFactory eventFactory) {
    super(context);
    this.workingCopyFactory = workingCopyFactory;
    this.attributeAnalyzer = attributeAnalyzer;
    this.repositoryManager = repositoryManager;
    this.eventFactory = eventFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    return mergeWithStrategy(request);
  }

  @Override
  public MergeConflictResult computeConflicts(MergeCommandRequest request) {
    return inClone(git -> new ConflictWorker(git, request), workingCopyFactory, request.getTargetBranch());
  }

  private MergeCommandResult mergeWithStrategy(MergeCommandRequest request) {
    return switch (request.getMergeStrategy()) {
      case SQUASH -> new GitMergeWithSquash(request, context, repositoryManager, eventFactory).run();
      case FAST_FORWARD_IF_POSSIBLE ->
        new GitFastForwardIfPossible(request, context, repositoryManager, eventFactory).run();
      case MERGE_COMMIT -> new GitMergeCommit(request, context, repositoryManager, eventFactory).run();
      case REBASE -> new GitMergeRebase(request, context, repositoryManager, eventFactory).run();
      default -> throw new MergeStrategyNotSupportedException(repository, request.getMergeStrategy());
    };
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try (Repository repository = context.open()) {
      List<MergePreventReason> mergePreventReasons = new ArrayList<>(2);
      if (attributeAnalyzer.hasExternalMergeToolConflicts(request.getBranchToMerge(), request.getTargetBranch())) {
        mergePreventReasons.add(new MergePreventReason(MergePreventReasonType.EXTERNAL_MERGE_TOOL));
      }

      checkMergeableWithoutFileConflicts(repository, request.getBranchToMerge(), request.getTargetBranch())
        .ifPresent(mergePreventReasons::add);

      return new MergeDryRunCommandResult(mergePreventReasons.isEmpty(), mergePreventReasons);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
    }
  }

  private Optional<MergePreventReason> checkMergeableWithoutFileConflicts(Repository repository, String sourceRevision, String targetRevision) throws IOException {
    ResolveMerger merger = (ResolveMerger) RECURSIVE.newMerger(repository, true);
    if (!merger.merge(
      resolveRevisionOrThrowNotFound(repository, sourceRevision),
      resolveRevisionOrThrowNotFound(repository, targetRevision)
    )) {
      return of(new MergePreventReason(MergePreventReasonType.FILE_CONFLICTS, merger.getUnmergedPaths()));
    } else {
      return empty();
    }
  }

  @Override
  public boolean isSupported(MergeStrategy strategy) {
    return STRATEGIES.contains(strategy);
  }

  @Override
  public Set<MergeStrategy> getSupportedMergeStrategies() {
    return STRATEGIES;
  }

  private class ConflictWorker extends GitCloneWorker<MergeConflictResult> {
    private final String theirs;
    private final String ours;
    private final CanonicalTreeParser treeParser;
    private final ObjectId treeId;
    private final ByteArrayOutputStream diffBuffer;

    private final MergeConflictResult result = new MergeConflictResult();


    private ConflictWorker(Git git, MergeCommandRequest request) {
      super(git, context, repository);
      theirs = request.getBranchToMerge();
      ours = request.getTargetBranch();

      treeParser = new CanonicalTreeParser();
      diffBuffer = new ByteArrayOutputStream();
      try {
        treeId = git.getRepository().resolve(ours + "^{tree}");
      } catch (IOException e) {
        throw notFound(entity("branch", ours).in(repository));
      }
    }

    @Override
    MergeConflictResult run() throws IOException {
      MergeResult mergeResult = doTemporaryMerge();
      if (mergeResult.getConflicts() != null) {
        getStatus().getConflictingStageState().forEach(this::computeConflict);
      }
      return result;
    }

    private void computeConflict(String path, IndexDiff.StageState stageState) {
      switch (stageState) {
        case BOTH_MODIFIED:
          diffBuffer.reset();
          try (ObjectReader reader = getClone().getRepository().newObjectReader()) {
            treeParser.reset(reader, treeId);
            getClone()
              .diff()
              .setOldTree(treeParser)
              .setPathFilter(PathFilter.create(path))
              .setOutputStream(diffBuffer)
              .call();
            result.addBothModified(path, diffBuffer.toString());
          } catch (GitAPIException | IOException e) {
            throw new InternalRepositoryException(repository, "could not calculate diff for path " + path, e);
          }
          break;
        case BOTH_ADDED:
          result.addAddedByBoth(path);
          break;
        case DELETED_BY_THEM:
          result.addDeletedByUs(path);
          break;
        case DELETED_BY_US:
          result.addDeletedByThem(path);
          break;
        default:
          throw new InternalRepositoryException(context.getRepository(), "unexpected conflict type: " + stageState);
      }
    }

    private MergeResult doTemporaryMerge() throws IOException {
      ObjectId sourceRevision = resolveRevision(theirs);
      try {
        return getClone().merge()
          .setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.NO_FF)
          .setCommit(false)
          .include(theirs, sourceRevision)
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + theirs + " into " + ours, e);
      }
    }

    private Status getStatus() {
      Status status;
      try {
        status = getClone().status().call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not get status", e);
      }
      return status;
    }
  }

  public interface Factory {
    MergeCommand create(GitContext context);
  }

}

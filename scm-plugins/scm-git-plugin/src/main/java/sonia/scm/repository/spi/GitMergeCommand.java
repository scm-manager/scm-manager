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

package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
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
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.InternalRepositoryException;
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
import java.util.Set;

import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private final GitWorkingCopyFactory workingCopyFactory;
  private final AttributeAnalyzer attributeAnalyzer;
  private static final Set<MergeStrategy> STRATEGIES = ImmutableSet.of(
    MergeStrategy.MERGE_COMMIT,
    MergeStrategy.FAST_FORWARD_IF_POSSIBLE,
    MergeStrategy.SQUASH,
    MergeStrategy.REBASE
  );

  @Inject
  GitMergeCommand(@Assisted GitContext context, GitRepositoryHandler handler, AttributeAnalyzer attributeAnalyzer) {
    this(context, handler.getWorkingCopyFactory(), attributeAnalyzer);
  }

  GitMergeCommand(@Assisted GitContext context, GitWorkingCopyFactory workingCopyFactory, AttributeAnalyzer attributeAnalyzer) {
    super(context);
    this.workingCopyFactory = workingCopyFactory;
    this.attributeAnalyzer = attributeAnalyzer;
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
    switch (request.getMergeStrategy()) {
      case SQUASH:
        return inClone(clone -> new GitMergeWithSquash(clone, request, context, repository), workingCopyFactory, request.getTargetBranch());

      case FAST_FORWARD_IF_POSSIBLE:
        return inClone(clone -> new GitFastForwardIfPossible(clone, request, context, repository), workingCopyFactory, request.getTargetBranch());

      case MERGE_COMMIT:
        return inClone(clone -> new GitMergeCommit(clone, request, context, repository), workingCopyFactory, request.getTargetBranch());

      case REBASE:
        return inClone(clone -> new GitMergeRebase(clone, request, context, repository), workingCopyFactory, request.getTargetBranch());

      default:
        throw new MergeStrategyNotSupportedException(repository, request.getMergeStrategy());
    }
  }
  
  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try (Repository repository = context.open()) {
      List<MergePreventReason> mergePreventReasons = new ArrayList<>(2);
      if (attributeAnalyzer.hasExternalMergeToolConflicts(request.getBranchToMerge(), request.getTargetBranch())) {
        mergePreventReasons.add(new MergePreventReason(MergePreventReasonType.EXTERNAL_MERGE_TOOL));
      }

      if (!isMergeableWithoutFileConflicts(repository, request.getBranchToMerge(), request.getTargetBranch())) {
        mergePreventReasons.add(new MergePreventReason(MergePreventReasonType.FILE_CONFLICTS));
      }

      return new MergeDryRunCommandResult(mergePreventReasons.isEmpty(), mergePreventReasons);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
    }
  }

  private boolean isMergeableWithoutFileConflicts(Repository repository, String sourceRevision, String targetRevision) throws IOException {
    return RECURSIVE.newMerger(repository, true).merge(
      resolveRevisionOrThrowNotFound(repository,sourceRevision),
      resolveRevisionOrThrowNotFound(repository, targetRevision)
    );
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

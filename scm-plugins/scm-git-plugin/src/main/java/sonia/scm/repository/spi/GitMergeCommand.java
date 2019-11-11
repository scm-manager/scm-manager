package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.MergeStrategy;
import sonia.scm.repository.api.MergeStrategyNotSupportedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private final GitWorkdirFactory workdirFactory;

  private static final Set<MergeStrategy> STRATEGIES = ImmutableSet.of(
    MergeStrategy.MERGE_COMMIT,
    MergeStrategy.FAST_FORWARD_IF_POSSIBLE,
    MergeStrategy.SQUASH
  );

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    return mergeWithStrategy(request);
  }

  @Override
  public MergeConflictResult computeConflicts(MergeCommandRequest request) {
    WorkingCopyCloser closer = new WorkingCopyCloser();
    return inClone(git -> new ConflictWorker(git, request, closer), workdirFactory, request.getTargetBranch());
  }

  private MergeCommandResult mergeWithStrategy(MergeCommandRequest request) {
    switch(request.getMergeStrategy()) {
      case SQUASH:
        return inClone(clone -> new GitMergeWithSquash(clone, request, context, repository), workdirFactory, request.getTargetBranch());

      case FAST_FORWARD_IF_POSSIBLE:
        return inClone(clone -> new GitFastForwardIfPossible(clone, request, context, repository), workdirFactory, request.getTargetBranch());

      case MERGE_COMMIT:
        return inClone(clone -> new GitMergeCommit(clone, request, context, repository), workdirFactory, request.getTargetBranch());

      default:
        throw new MergeStrategyNotSupportedException(repository, request.getMergeStrategy());
    }
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try {
      Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(
        merger.merge(
          resolveRevisionOrThrowNotFound(repository, request.getBranchToMerge()),
          resolveRevisionOrThrowNotFound(repository, request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
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
    private final Git git;
    private final MergeCommandRequest request;
    private final WorkingCopyCloser closer;
    private final CanonicalTreeParser treeParser;
    private final ObjectId treeId;
    private final ByteArrayOutputStream diffBuffer;

    private ConflictWorker(Git git, MergeCommandRequest request, WorkingCopyCloser closer) {
      super(git, context, repository);
      this.git = git;
      this.request = request;
      this.closer = closer;

      treeParser = new CanonicalTreeParser();
      treeId = git.getRepository().resolve(request.getTargetBranch() + "^{tree}");
      diffBuffer = new ByteArrayOutputStream();
    }

    @Override
    MergeConflictResult run() throws IOException {
      MergeResult mergeResult = doTemporaryMerge();
      if (mergeResult.getConflicts() == null) {
        return new MergeConflictResult();
      }

      Status status = getStatus();

      MergeConflictResult result = new MergeConflictResult();


      status.getConflictingStageState().forEach((path, value) -> {
        switch (value) {
          case BOTH_MODIFIED:
            diffBuffer.reset();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
              treeParser.reset(reader, treeId);
              git
                .diff()
                .setOldTree(treeParser)
                .setPathFilter(PathFilter.create(path))
                .setOutputStream(diffBuffer)
                .call();
              result.addBothModified(path, diffBuffer.toString());
            } catch (GitAPIException | IOException e) {
              throw new InternalRepositoryException(repository, "could not calculate diff for path " + path, e);
            } finally {
              closer.close();
            }
            break;
          case DELETED_BY_THEM:
            result.addDeletedByThem(path);
            break;
          case DELETED_BY_US:
            result.addDeletedByUs(path);
            break;
          default:
            throw new InternalRepositoryException(context.getRepository(), "unexpected conflict type: " + value);
        }
      });
      return result;
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

    private MergeResult doTemporaryMerge() throws IOException {
      ObjectId sourceRevision = resolveRevision(request.getBranchToMerge());
      MergeResult mergeResult;
      try {
        mergeResult = getClone().merge()
          .setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.NO_FF)
          .setCommit(false)
          .include(request.getBranchToMerge(), sourceRevision)
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + request.getBranchToMerge() + " into " + request.getTargetBranch(), e);
      }
      return mergeResult;
    }
  }
}

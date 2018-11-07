package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;

import java.io.IOException;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeCommand.class);

  private final GitWorkdirFactory workdirFactory;

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    try (WorkingCopy workingCopy = workdirFactory.createWorkingCopy(context)) {
      Repository repository = workingCopy.get();
      return new MergeWorker(repository).merge(request);
    } catch (IOException e) {
      throw new InternalRepositoryException(e);
    }
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try {
      Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(merger.merge(repository.resolve(request.getBranchToMerge()), repository.resolve(request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException(e);
    }
  }

  private static class MergeWorker {

    private final Repository clone;
    private MergeWorker(Repository clone) {
      this.clone = clone;
    }

    private MergeCommandResult merge(MergeCommandRequest request) throws IOException {
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(clone);
      boolean mergeResult = merger.merge(
        resolveRevision(clone, request.getTargetBranch()),
        resolveRevision(clone, request.getBranchToMerge())
      );
      if (mergeResult) {
        logger.info("Merged branch {} into {}", request.getBranchToMerge(), request.getTargetBranch());
        // TODO commit, push and verify push was successful
      }
      return new MergeCommandResult(mergeResult);
    }

    private ObjectId resolveRevision(Repository repository, String branchToMerge) throws IOException {
      ObjectId resolved = repository.resolve(branchToMerge);
      if (resolved == null) {
        return repository.resolve("origin/" + branchToMerge);
      } else {
        return resolved;
      }
    }
  }
}

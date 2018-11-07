package sonia.scm.repository.spi;

import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.lib.Repository;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;

import java.io.IOException;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private final GitWorkdirFactory workdirPool;

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirPool) {
    super(context, repository);
    this.workdirPool = workdirPool;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    try (WorkingCopy workingCopy = workdirPool.createWorkingCopy(context)) {
      Repository repository = workingCopy.get();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository);
      boolean mergeResult = merger.merge(repository.resolve(request.getBranchToMerge()), repository.resolve(request.getTargetBranch()));
      if (mergeResult) {
        // TODO push and verify push was successful
      }
      return new MergeCommandResult(mergeResult);
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
}

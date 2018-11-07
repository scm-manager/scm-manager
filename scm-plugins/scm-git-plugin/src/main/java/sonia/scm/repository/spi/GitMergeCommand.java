package sonia.scm.repository.spi;

import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import sonia.scm.repository.CloseableWrapper;
import sonia.scm.repository.GitWorkdirPool;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;

import java.io.IOException;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private final GitWorkdirPool workdirPool;

  GitMergeCommand(GitContext context, Repository repository, GitWorkdirPool workdirPool) {
    super(context, repository);
    this.workdirPool = workdirPool;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    try (CloseableWrapper<org.eclipse.jgit.lib.Repository> workingCopy = workdirPool.getWorkingCopy(context.open().getDirectory())) {
      org.eclipse.jgit.lib.Repository repository = workingCopy.get();
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
      org.eclipse.jgit.lib.Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(merger.merge(repository.resolve(request.getBranchToMerge()), repository.resolve(request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException(e);
    }
  }
}

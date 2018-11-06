package sonia.scm.repository.spi;

import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.IOException;

public class GitMergeDryRunCommand extends AbstractGitCommand implements MergeDryRunCommand {
  GitMergeDryRunCommand(GitContext context, Repository repository) {
    super(context, repository);
  }

  @Override
  public boolean isMergeable(MergeDryRunCommandRequest request) {
    try {
      org.eclipse.jgit.lib.Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
      return merger.merge(repository.resolve(request.getBranchToMerge()), repository.resolve(request.getTargetBranch()));
    } catch (IOException e) {
      throw new InternalRepositoryException(e);
    }
  }
}

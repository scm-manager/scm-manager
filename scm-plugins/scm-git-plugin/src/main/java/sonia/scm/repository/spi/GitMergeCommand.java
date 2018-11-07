package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
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

    private final Git clone;
    private MergeWorker(Repository clone) {
      this.clone = new Git(clone);
    }

    private MergeCommandResult merge(MergeCommandRequest request) throws IOException {
      try {
        clone.checkout().setName(request.getTargetBranch()).call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException("could not checkout target branch " + request.getTargetBranch(), e);
      }
      MergeResult result;
      try {
        result = clone.merge().include(request.getBranchToMerge(), resolveRevision(request.getBranchToMerge())).setCommit(true).call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException("could not merge branch " + request.getBranchToMerge() + " into " + request.getTargetBranch(), e);
      }
      if (result.getMergeStatus().isSuccessful()) {
        logger.info("Merged branch {} into {}", request.getBranchToMerge(), request.getTargetBranch());
        try {
          clone.push().call();
        } catch (GitAPIException e) {
          throw new InternalRepositoryException("could not push merged branch " + request.getBranchToMerge() + " to origin", e);
        }
        return new MergeCommandResult(true);
      } else {
        logger.info("Could not merged branch {} into {}", request.getBranchToMerge(), request.getTargetBranch());
        return new MergeCommandResult(false);
      }
    }

    private ObjectId resolveRevision(String branchToMerge) throws IOException {
      ObjectId resolved = clone.getRepository().resolve(branchToMerge);
      if (resolved == null) {
        return clone.getRepository().resolve("origin/" + branchToMerge);
      } else {
        return resolved;
      }
    }
  }
}

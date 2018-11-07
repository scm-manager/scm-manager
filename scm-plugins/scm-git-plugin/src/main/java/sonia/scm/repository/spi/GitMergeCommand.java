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
  public static final String MERGE_COMMIT_MESSAGE_TEMPLATE =
    "Merge of branch %s into %s\n" +
    "\n" +
    "Automatic merge by SCM-Manager.";

  private final GitWorkdirFactory workdirFactory;

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    try (WorkingCopy workingCopy = workdirFactory.createWorkingCopy(context)) {
      Repository repository = workingCopy.get();
      logger.debug("cloned repository to folder {}", repository.getWorkTree());
      return new MergeWorker(repository).merge(request);
    } catch (IOException e) {
      throw new InternalRepositoryException("could not clone repository for merge", e);
    }
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try {
      Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(merger.merge(repository.resolve(request.getBranchToMerge()), repository.resolve(request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException("could not clone repository for merge", e);
    }
  }

  private static class MergeWorker {

    private final Git clone;
    private MergeWorker(Repository clone) {
      this.clone = new Git(clone);
    }

    private MergeCommandResult merge(MergeCommandRequest request) throws IOException {
      String target = request.getTargetBranch();
      String toMerge = request.getBranchToMerge();
      try {
        clone.checkout().setName(target).call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException("could not checkout target branch for merge: " + target, e);
      }
      MergeResult result;
      try {
        result = clone.merge()
          .setCommit(true)
          .setMessage(String.format(MERGE_COMMIT_MESSAGE_TEMPLATE, toMerge, target))
          .include(toMerge, resolveRevision(toMerge))
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException("could not merge branch " + toMerge + " into " + target, e);
      }
      if (result.getMergeStatus().isSuccessful()) {
        logger.debug("merged branch {} into {}", toMerge, target);
        try {
          clone.push().call();
        } catch (GitAPIException e) {
          throw new InternalRepositoryException("could not push merged branch " + toMerge + " to origin", e);
        }
        logger.debug("pushed merged branch {}", target);
        return MergeCommandResult.success();
      } else {
        logger.info("could not merged branch {} into {} due to conflict in paths {}", toMerge, target, result.getConflicts().keySet());
        return MergeCommandResult.failure(result.getConflicts().keySet());
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

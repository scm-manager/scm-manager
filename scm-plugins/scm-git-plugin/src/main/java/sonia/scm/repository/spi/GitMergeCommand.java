package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeCommand.class);

  private static final String MERGE_COMMIT_MESSAGE_TEMPLATE = String.join("\n",
    "Merge of branch {0} into {1}",
    "",
    "Automatic merge by SCM-Manager.");

  private final GitWorkdirFactory workdirFactory;

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    return inClone(clone -> new MergeWorker(clone, request), workdirFactory, request.getTargetBranch());
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try {
      Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) MergeStrategy.RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(
        merger.merge(
          resolveRevisionOrThrowNotFound(repository, request.getBranchToMerge()),
          resolveRevisionOrThrowNotFound(repository, request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
    }
  }

  @Override
  public MergeConflictResult computeConflicts(MergeCommandRequest request) {
    WorkingCopyCloser closer = new WorkingCopyCloser();
    return inClone(git -> new ConflictWorker(git, request, closer), workdirFactory, request.getTargetBranch());
  }

  private class MergeWorker extends GitCloneWorker<MergeCommandResult> {

    private final String target;
    private final String toMerge;
    private final Person author;
    private final String messageTemplate;

    private MergeWorker(Git clone, MergeCommandRequest request) {
      super(clone);
      this.target = request.getTargetBranch();
      this.toMerge = request.getBranchToMerge();
      this.author = request.getAuthor();
      this.messageTemplate = request.getMessageTemplate();
    }

    @Override
    MergeCommandResult run() throws IOException {
      MergeResult result = doMergeInClone();
      if (result.getMergeStatus().isSuccessful()) {
        doCommit();
        push();
        return MergeCommandResult.success();
      } else {
        return analyseFailure(result);
      }
    }

    private MergeResult doMergeInClone() throws IOException {
      MergeResult result;
      try {
        ObjectId sourceRevision = resolveRevision(toMerge);
        result = getClone().merge()
          .setFastForward(FastForwardMode.NO_FF)
          .setCommit(false) // we want to set the author manually
          .include(toMerge, sourceRevision)
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + toMerge + " into " + target, e);
      }
      return result;
    }

    private void doCommit() {
      logger.debug("merged branch {} into {}", toMerge, target);
      doCommit(MessageFormat.format(determineMessageTemplate(), toMerge, target), author);
    }

    private String determineMessageTemplate() {
      if (Strings.isNullOrEmpty(messageTemplate)) {
        return MERGE_COMMIT_MESSAGE_TEMPLATE;
      } else {
        return messageTemplate;
      }
    }

    private MergeCommandResult analyseFailure(MergeResult result) {
      logger.info("could not merged branch {} into {} due to conflict in paths {}", toMerge, target, result.getConflicts().keySet());
      return MergeCommandResult.failure(result.getConflicts().keySet());
    }
  }

  private class ConflictWorker extends GitCloneWorker<MergeConflictResult> {
    private final Git git;
    private final MergeCommandRequest request;
    private final WorkingCopyCloser closer;

    private ConflictWorker(Git git, MergeCommandRequest request, WorkingCopyCloser closer) {
      super(git);
      this.git = git;
      this.request = request;
      this.closer = closer;
    }

    @Override
    MergeConflictResult run() throws IOException {
      ObjectId sourceRevision = resolveRevision(request.getBranchToMerge());
      MergeResult mergeResult;
      try {
        mergeResult = getClone().merge()
          .setFastForward(FastForwardMode.NO_FF)
          .setCommit(false)
          .include(request.getBranchToMerge(), sourceRevision)
          .call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + request.getBranchToMerge() + " into " + request.getTargetBranch(), e);
      }

      if (mergeResult.getConflicts() == null) {
        return new MergeConflictResult();
      }
      Status status;
      try {
        status = getClone().status().call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not get status", e);
      }

      MergeConflictResult result = new MergeConflictResult();

      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      ObjectId treeId = git.getRepository().resolve(request.getTargetBranch() + "^{tree}");

      ByteArrayOutputStream diffBuffer = new ByteArrayOutputStream();

      status.getConflictingStageState().entrySet().forEach(conflictEntry -> {

        String path = conflictEntry.getKey();
        switch (conflictEntry.getValue()) {
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
            throw new InternalRepositoryException(context.getRepository(), "unexpected conflict type: " + conflictEntry.getValue());
        }
      });
      return result;
    }
  }
}

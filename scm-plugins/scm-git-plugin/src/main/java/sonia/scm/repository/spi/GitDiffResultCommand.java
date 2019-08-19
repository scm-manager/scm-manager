package sonia.scm.repository.spi;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

public class GitDiffResultCommand extends AbstractGitCommand implements DiffResultCommand {

  GitDiffResultCommand(GitContext context, Repository repository) {
    super(context, repository);
  }

  public DiffResult getDiffResult(DiffCommandRequest diffCommandRequest) throws IOException {
    org.eclipse.jgit.lib.Repository repository = open();
    return new GitDiffResult(repository, Differ.diff(repository, diffCommandRequest));
  }

  private class GitDiffResult implements DiffResult {

    private final org.eclipse.jgit.lib.Repository repository;
    private final Differ.Diff diff;

    private GitDiffResult(org.eclipse.jgit.lib.Repository repository, Differ.Diff diff) {
      this.repository = repository;
      this.diff = diff;
    }

    @Override
    public String getOldRevision() {
      return GitUtil.getId(diff.getCommit().getParent(0).getId());
    }

    @Override
    public String getNewRevision() {
      return GitUtil.getId(diff.getCommit().getId());
    }

    @Override
    public Iterator<DiffFile> iterator() {
      return diff.getEntries()
        .stream()
        .map(diffEntry -> new GitDiffFile(repository, diffEntry))
        .collect(Collectors.<DiffFile>toList())
        .iterator();
    }
  }

  private class GitDiffFile implements DiffFile {

    private final org.eclipse.jgit.lib.Repository repository;
    private final DiffEntry diffEntry;

    private GitDiffFile(org.eclipse.jgit.lib.Repository repository, DiffEntry diffEntry) {
      this.repository = repository;
      this.diffEntry = diffEntry;
    }

    @Override
    public String getOldRevision() {
      return GitUtil.getId(diffEntry.getOldId().toObjectId());
    }

    @Override
    public String getNewRevision() {
      return GitUtil.getId(diffEntry.getNewId().toObjectId());
    }

    @Override
    public String getOldPath() {
      return diffEntry.getOldPath();
    }

    @Override
    public String getNewPath() {
      return diffEntry.getNewPath();
    }

    @Override
    public Iterator<Hunk> iterator() {
      String content = format(repository, diffEntry);
      GitHunkParser parser = new GitHunkParser();
      return parser.parse(content).iterator();
    }

    private String format(org.eclipse.jgit.lib.Repository repository, DiffEntry entry) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DiffFormatter formatter = new DiffFormatter(baos)) {
        formatter.setRepository(repository);
        formatter.format(entry);
        return baos.toString();
      } catch (IOException ex) {
        throw new InternalRepositoryException(GitDiffResultCommand.this.repository, "failed to format diff entry", ex);
      }
    }

  }

}

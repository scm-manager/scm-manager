package sonia.scm.repository.spi;

import org.eclipse.jgit.diff.DiffEntry;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

public class GitDiffResultCommand extends AbstractGitCommand implements DiffResultCommand {

  GitDiffResultCommand(GitContext context, Repository repository) {
    super(context, repository);
  }

  public DiffResult getDiffResult(DiffCommandRequest diffCommandRequest) throws IOException {
    try (Differ differ = Differ.create(open(), diffCommandRequest)) {
      GitDiffResult result = new GitDiffResult();
      differ.process(result::process);
      return result;
    }
  }

  private class GitDiffResult implements DiffResult {

    private Differ.Diff diff;

    void process(Differ.Diff diff) {
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
      return diff.getEntries().stream().map(GitDiffFile::new).collect(Collectors.<DiffFile>toList()).iterator();
    }
  }

  private static class GitDiffFile implements DiffFile {

    private final DiffEntry diffEntry;

    private GitDiffFile(DiffEntry diffEntry) {
      this.diffEntry = diffEntry;
    }

    @Override
    public String getOldRevision() {
      return null;
    }

    @Override
    public String getNewRevision() {
      return null;
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
      return null;
    }
  }
}

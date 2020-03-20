/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
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

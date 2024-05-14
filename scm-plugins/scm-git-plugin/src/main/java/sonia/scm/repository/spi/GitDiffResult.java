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
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class GitDiffResult implements DiffResult {

  private final Repository scmRepository;
  private final org.eclipse.jgit.lib.Repository repository;
  private final Differ.Diff diff;
  private final List<DiffEntry> diffEntries;

  private final IgnoreWhitespaceLevel ignoreWhitespaceLevel;
  private final int offset;
  private final Integer limit;

  public GitDiffResult(Repository scmRepository,
                       org.eclipse.jgit.lib.Repository repository,
                       Differ.Diff diff,
                       IgnoreWhitespaceLevel ignoreWhitespaceLevel,
                       int offset,
                       Integer limit) {
    this.scmRepository = scmRepository;
    this.repository = repository;
    this.diff = diff;
    this.offset = offset;
    this.ignoreWhitespaceLevel = ignoreWhitespaceLevel;
    this.limit = limit;
    this.diffEntries = diff.getEntries();
  }

  @Override
  public String getOldRevision() {
    ObjectId commonAncestor = diff.getCommonAncestor();
    if (commonAncestor != null) {
      return commonAncestor.name();
    }
    return diff.getCommit().getParentCount() > 0 ? GitUtil.getId(diff.getCommit().getParent(0).getId()) : null;
  }

  @Override
  public String getNewRevision() {
    return GitUtil.getId(diff.getCommit().getId());
  }

  @Override
  public boolean isPartial() {
    return limit != null && limit + offset < diffEntries.size();
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public Optional<Integer> getLimit() {
    return ofNullable(limit);
  }

  @Override
  public Iterator<DiffFile> iterator() {
    Stream<DiffEntry> diffEntryStream = diffEntries
      .stream()
      .skip(offset);
    if (limit != null) {
      diffEntryStream = diffEntryStream.limit(limit);
    }
    return diffEntryStream
      .map(diffEntry -> new GitDiffFile(repository, diffEntry))
      .map(DiffFile.class::cast)
      .iterator();
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
      return GitDiffResult.this.getOldRevision();
    }

    @Override
    public String getNewRevision() {
      return GitDiffResult.this.getNewRevision();
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
    public ChangeType getChangeType() {
      switch (diffEntry.getChangeType()) {
        case ADD:
          return ChangeType.ADD;
        case MODIFY:
          return ChangeType.MODIFY;
        case RENAME:
          return ChangeType.RENAME;
        case DELETE:
          return ChangeType.DELETE;
        case COPY:
          return ChangeType.COPY;
        default:
          throw new IllegalArgumentException("Unknown change type: " + diffEntry.getChangeType());
      }
    }

    @Override
    public Iterator<Hunk> iterator() {
      String content = format(repository, diffEntry);
      GitHunkParser parser = new GitHunkParser();
      return parser.parse(content).iterator();
    }

    private String format(org.eclipse.jgit.lib.Repository repository, DiffEntry entry) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DiffFormatter formatter = new DiffFormatter(baos)) {
        if (ignoreWhitespaceLevel == IgnoreWhitespaceLevel.ALL) {
          formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        }
        formatter.setRepository(repository);
        formatter.format(entry);
        return baos.toString(StandardCharsets.UTF_8);
      } catch (IOException ex) {
        throw new InternalRepositoryException(scmRepository, "failed to format diff entry", ex);
      }
    }
  }

  @Override
  public IgnoreWhitespaceLevel getIgnoreWhitespace() {
    return ignoreWhitespaceLevel;
  }
}

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

import com.google.common.base.Strings;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import sonia.scm.repository.GitUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

final class Differ implements AutoCloseable {

  private final RevWalk walk;
  private final TreeWalk treeWalk;
  private final RevCommit commit;
  private final PathFilter pathFilter;

  private Differ(RevCommit commit, RevWalk walk, TreeWalk treeWalk, PathFilter pathFilter) {
    this.commit = commit;
    this.walk = walk;
    this.treeWalk = treeWalk;
    this.pathFilter = pathFilter;
  }

  static Diff diff(Repository repository, DiffCommandRequest request) throws IOException {
    try (Differ differ = create(repository, request)) {
      return differ.diff(repository);
    }
  }

  private static Differ create(Repository repository, DiffCommandRequest request) throws IOException {
    RevWalk walk = new RevWalk(repository);

    ObjectId revision = repository.resolve(request.getRevision());
    if (revision == null) {
      throw notFound(entity("Revision", request.getRevision()));
    }
    RevCommit commit;
    try {
      commit = walk.parseCommit(revision);
    } catch (MissingObjectException ex) {
      throw notFound(entity("Revision", request.getRevision()));
    }

    walk.markStart(commit);
    commit = walk.next();
    TreeWalk treeWalk = new TreeWalk(repository);
    treeWalk.reset();
    treeWalk.setRecursive(true);

    PathFilter pathFilter = null;
    if (Util.isNotEmpty(request.getPath())) {
      pathFilter = PathFilter.create(request.getPath());
    }

    if (!Strings.isNullOrEmpty(request.getAncestorChangeset())) {
      ObjectId otherRevision = repository.resolve(request.getAncestorChangeset());
      if (otherRevision == null) {
        throw notFound(entity("Revision", request.getAncestorChangeset()));
      }
      ObjectId ancestorId = GitUtil.computeCommonAncestor(repository, revision, otherRevision);
      RevTree tree = walk.parseCommit(ancestorId).getTree();
      treeWalk.addTree(tree);
    } else if (commit.getParentCount() > 0) {
      RevTree tree = commit.getParent(0).getTree();

      if (tree != null) {
        treeWalk.addTree(tree);
      } else {
        treeWalk.addTree(new EmptyTreeIterator());
      }
    } else {
      treeWalk.addTree(new EmptyTreeIterator());
    }

    treeWalk.addTree(commit.getTree());

    return new Differ(commit, walk, treeWalk, pathFilter);
  }

  private Diff diff(Repository repository) throws IOException {
    List<DiffEntry> entries = scanWithRename(repository, pathFilter, treeWalk);
    return new Diff(commit, entries);
  }

  static List<DiffEntry> scanWithRename(Repository repository, PathFilter pathFilter, TreeWalk treeWalk) throws IOException {
    List<DiffEntry> entries;
    try (DiffFormatter diffFormatter = new DiffFormatter(null)) {
      diffFormatter.setRepository(repository);
      diffFormatter.setDetectRenames(true);
      if (pathFilter != null) {
        diffFormatter.setPathFilter(pathFilter);
      }
      entries = diffFormatter.scan(
        treeWalk.getTree(0, AbstractTreeIterator.class),
        treeWalk.getTree(1, AbstractTreeIterator.class));
    }
    return entries;
  }

  @Override
  public void close() {
    GitUtil.release(walk);
    GitUtil.release(treeWalk);
  }

  public static class Diff {

    private final RevCommit commit;
    private final List<DiffEntry> entries;

    private Diff(RevCommit commit, List<DiffEntry> entries) {
      this.commit = commit;
      this.entries = entries;
    }

    public RevCommit getCommit() {
      return commit;
    }

    public List<DiffEntry> getEntries() {
      return entries;
    }
  }

}

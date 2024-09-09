/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

public final class Differ implements AutoCloseable {

  private final RevWalk walk;
  private final TreeWalk treeWalk;
  private final RevCommit commit;
  private final PathFilter pathFilter;
  private final ObjectId commonAncestor;

  private Differ(RevCommit commit, RevWalk walk, TreeWalk treeWalk, PathFilter pathFilter, ObjectId commonAncestor) {
    this.commit = commit;
    this.walk = walk;
    this.treeWalk = treeWalk;
    this.pathFilter = pathFilter;
    this.commonAncestor = commonAncestor;
  }

  public static Diff diff(Repository repository, DiffCommandRequest request) throws IOException {
    try (Differ differ = create(repository, request)) {
      return differ.diff(repository, differ.commonAncestor);
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

    ObjectId ancestorId = null;
    if (!Strings.isNullOrEmpty(request.getAncestorChangeset())) {
      ObjectId otherRevision = repository.resolve(request.getAncestorChangeset());
      if (otherRevision == null) {
        throw notFound(entity("Revision", request.getAncestorChangeset()));
      }
      ancestorId = GitUtil.computeCommonAncestor(repository, revision, otherRevision);
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

    return new Differ(commit, walk, treeWalk, pathFilter, ancestorId);
  }

  private Diff diff(Repository repository, ObjectId ancestorChangeset) throws IOException {
    List<DiffEntry> entries = scanWithRename(repository, pathFilter, treeWalk);
    return new Diff(commit, entries, ancestorChangeset);
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
    private final ObjectId commonAncestor;

    private Diff(RevCommit commit, List<DiffEntry> entries, ObjectId commonAncestor) {
      this.commit = commit;
      this.entries = entries;
      this.commonAncestor = commonAncestor;
    }

    public RevCommit getCommit() {
      return commit;
    }

    public List<DiffEntry> getEntries() {
      return entries;
    }

    public ObjectId getCommonAncestor() {
      return commonAncestor;
    }
  }

}

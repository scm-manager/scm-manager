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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import sonia.scm.repository.Added;
import sonia.scm.repository.Copied;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class ModificationsComputer {

  private final Repository gitRepository;

  public ModificationsComputer(Repository gitRepository) {
    this.gitRepository = gitRepository;
  }

  public Modifications compute(String baseRevision, String revision) throws IOException {
    RevWalk revWalk = null;
    if (!gitRepository.getAllRefs().isEmpty()) {
      try {
        revWalk = new RevWalk(gitRepository);
        RevCommit commit = getCommit(revision, gitRepository, revWalk);
        TreeWalk treeWalk = createTreeWalk(gitRepository);
        if (baseRevision == null) {
          determineParentAsBase(treeWalk, commit, revWalk);
        } else {
          RevCommit baseCommit = getCommit(baseRevision, gitRepository, revWalk);
          treeWalk.addTree(baseCommit.getTree());
        }
        return new Modifications(baseRevision, revision, createModifications(treeWalk, commit));
      } finally {
        GitUtil.release(revWalk);
      }
    }
    return null;
  }

  private RevCommit getCommit(String revision, Repository gitRepository, RevWalk revWalk) throws IOException {
    ObjectId id = GitUtil.getRevisionId(gitRepository, revision);
    return revWalk.parseCommit(id);
  }

  private TreeWalk createTreeWalk(Repository gitRepository) {
    TreeWalk treeWalk = new TreeWalk(gitRepository);
    treeWalk.reset();
    treeWalk.setRecursive(true);
    return treeWalk;
  }

  private Collection<Modification> createModifications(TreeWalk treeWalk, RevCommit commit)
    throws IOException {
    treeWalk.addTree(commit.getTree());
    List<DiffEntry> entries = Differ.scanWithRename(gitRepository, null, treeWalk);
    Collection<Modification> modifications = new ArrayList<>();
    for (DiffEntry e : entries) {
      if (!e.getOldId().equals(e.getNewId()) || !e.getOldPath().equals(e.getNewPath())) {
        modifications.add(asModification(e));
      }
    }
    return modifications;
  }

  private void determineParentAsBase(TreeWalk treeWalk, RevCommit commit, RevWalk revWalk) throws IOException {
    if (commit.getParentCount() > 0) {
      RevCommit parent = commit.getParent(0);
      RevTree tree = parent.getTree();
      if ((tree == null) && (revWalk != null)) {
        revWalk.parseHeaders(parent);
        tree = parent.getTree();
      }
      if (tree != null) {
        treeWalk.addTree(tree);
      } else {
        log.trace("no parent tree at position 0 for commit {}", commit.getName());
        treeWalk.addTree(new EmptyTreeIterator());
      }
    } else {
      log.trace("no parent available for commit {}", commit.getName());
      treeWalk.addTree(new EmptyTreeIterator());
    }
  }

  private Modification asModification(DiffEntry entry) {
    DiffEntry.ChangeType type = entry.getChangeType();
    switch (type) {
      case ADD:
        return new Added(entry.getNewPath());
      case MODIFY:
        return new Modified(entry.getNewPath());
      case DELETE:
        return new Removed(entry.getOldPath());
      case RENAME:
        return new Renamed(entry.getOldPath(), entry.getNewPath());
      case COPY:
        return new Copied(entry.getOldPath(), entry.getNewPath());
      default:
        throw new IllegalArgumentException(MessageFormat.format("The modification type: {0} is not supported.", type));
    }
  }
}

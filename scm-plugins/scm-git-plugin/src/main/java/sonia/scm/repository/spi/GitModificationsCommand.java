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
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modification;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;

import javax.inject.Inject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


@Slf4j
public class GitModificationsCommand extends AbstractGitCommand implements ModificationsCommand {

  @Inject
  GitModificationsCommand(GitContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("java:S2093")
  public Modifications getModifications(String baseRevision, String revision) {
    org.eclipse.jgit.lib.Repository gitRepository = null;
    RevWalk revWalk = null;
    try {
      gitRepository = open();
      if (!gitRepository.getAllRefs().isEmpty()) {
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
      }
    } catch (IOException ex) {
      log.error("could not open repository: " + repository.getNamespaceAndName(), ex);
      throw new InternalRepositoryException(entity(repository), "could not open repository: " + repository.getNamespaceAndName(), ex);
    } finally {
      GitUtil.release(revWalk);
    }
    return null;
  }

  private RevCommit getCommit(String revision, Repository gitRepository, RevWalk revWalk) throws IOException {
    ObjectId id = GitUtil.getRevisionId(gitRepository, revision);
    return revWalk.parseCommit(id);
  }

  @Override
  public Modifications getModifications(String revision) {
    return getModifications(null, revision);
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
    List<DiffEntry> entries = Differ.scanWithRename(context.open(), null, treeWalk);
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

  private Modification asModification(DiffEntry entry) throws UnsupportedModificationTypeException {
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
        throw new UnsupportedModificationTypeException(entity(repository), MessageFormat.format("The modification type: {0} is not supported.", type));
    }
  }
}

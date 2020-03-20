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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;


@Slf4j
public class GitModificationsCommand extends AbstractGitCommand implements ModificationsCommand {

  protected GitModificationsCommand(GitContext context, Repository repository) {
    super(context, repository);
  }

  private Modifications createModifications(TreeWalk treeWalk, RevCommit commit, RevWalk revWalk, String revision)
    throws IOException {
    treeWalk.reset();
    treeWalk.setRecursive(true);
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
    treeWalk.addTree(commit.getTree());
    List<DiffEntry> entries = DiffEntry.scan(treeWalk);
    Modifications modifications = new Modifications();
    for (DiffEntry e : entries) {
      if (!e.getOldId().equals(e.getNewId())) {
        appendModification(modifications, e);
      }
    }
    modifications.setRevision(revision);
    return modifications;
  }

  @Override
  public Modifications getModifications(String revision) {
    org.eclipse.jgit.lib.Repository gitRepository = null;
    RevWalk revWalk = null;
    try {
      gitRepository = open();
      if (!gitRepository.getAllRefs().isEmpty()) {
        revWalk = new RevWalk(gitRepository);
        ObjectId id = GitUtil.getRevisionId(gitRepository, revision);
        RevCommit commit = revWalk.parseCommit(id);
        TreeWalk treeWalk = new TreeWalk(gitRepository);
        return createModifications(treeWalk, commit, revWalk, revision);
      }
    } catch (IOException ex) {
      log.error("could not open repository", ex);
      throw new InternalRepositoryException(entity(repository), "could not open repository", ex);
    } finally {
      GitUtil.release(revWalk);
      GitUtil.close(gitRepository);
    }
    return null;
  }

  @Override
  public Modifications getModifications(ModificationsCommandRequest request) {
    return getModifications(request.getRevision());
  }

  private void appendModification(Modifications modifications, DiffEntry entry) throws UnsupportedModificationTypeException {
    DiffEntry.ChangeType type = entry.getChangeType();
    if (type == DiffEntry.ChangeType.ADD) {
      modifications.getAdded().add(entry.getNewPath());
    } else if (type == DiffEntry.ChangeType.MODIFY) {
      modifications.getModified().add(entry.getNewPath());
    } else if (type == DiffEntry.ChangeType.DELETE) {
      modifications.getRemoved().add(entry.getOldPath());
    } else {
      throw new UnsupportedModificationTypeException(entity(repository), MessageFormat.format("The modification type: {0} is not supported.", type));
    }
  }
}

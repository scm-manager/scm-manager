/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitChangesetConverter implements Closeable
{

  /**
   * the logger for GitChangesetConverter
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitChangesetConverter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param idLength
   */
  public GitChangesetConverter(org.eclipse.jgit.lib.Repository repository,
    int idLength)
  {
    this(repository, null, idLength);
  }

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param revWalk
   * @param idLength
   */
  public GitChangesetConverter(org.eclipse.jgit.lib.Repository repository,
    RevWalk revWalk, int idLength)
  {
    this.idLength = idLength;
    this.revWalk = revWalk;
    createTagMap(repository);
    treeWalk = new TreeWalk(repository);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void close()
  {
    GitUtil.release(treeWalk);
  }

  /**
   * Method description
   *
   *
   *
   * @param commit
   *
   * @return
   *
   * @throws IOException
   */
  public Changeset createChangeset(RevCommit commit) throws IOException
  {
    String id = commit.getId().abbreviate(idLength).name();
    List<String> parentList = null;
    RevCommit[] parents = commit.getParents();

    if (Util.isNotEmpty(parents))
    {
      parentList = new ArrayList<String>();

      for (RevCommit parent : parents)
      {
        parentList.add(parent.getId().abbreviate(idLength).name());
      }
    }

    long date = GitUtil.getCommitTime(commit);
    PersonIdent authorIndent = commit.getAuthorIdent();
    Person author = new Person(authorIndent.getName(),
                      authorIndent.getEmailAddress());
    String message = commit.getShortMessage();
    Changeset changeset = new Changeset(id, date, author, message);

    if (parentList != null)
    {
      changeset.setParents(parentList);
    }

    Modifications modifications = createModifications(treeWalk, commit);

    if (modifications != null)
    {
      changeset.setModifications(modifications);
    }

    Collection<String> tagCollection = tags.get(commit.getId());

    if (tagCollection != null)
    {
      changeset.getTags().addAll(tagCollection);
    }

    return changeset;
  }

  /**
   * TODO: copy and rename
   *
   *
   * @param modifications
   * @param entry
   */
  private void appendModification(Modifications modifications, DiffEntry entry)
  {
    switch (entry.getChangeType())
    {
      case ADD :
        modifications.getAdded().add(entry.getNewPath());

        break;

      case MODIFY :
        modifications.getModified().add(entry.getNewPath());

        break;

      case DELETE :
        modifications.getRemoved().add(entry.getOldPath());

        break;
    }
  }

  /**
   * Method description
   *
   *
   * @param treeWalk
   * @param commit
   *
   * @return
   *
   * @throws IOException
   */
  private Modifications createModifications(TreeWalk treeWalk, RevCommit commit)
    throws IOException
  {
    Modifications modifications = null;

    treeWalk.reset();
    treeWalk.setRecursive(true);

    if (commit.getParentCount() > 0)
    {
      RevCommit parent = commit.getParent(0);
      RevTree tree = parent.getTree();

      if ((tree == null) && (revWalk != null))
      {
        revWalk.parseHeaders(parent);
        tree = parent.getTree();
      }

      if (tree != null)
      {
        treeWalk.addTree(tree);
      }
      else
      {
        if (logger.isTraceEnabled())
        {
          logger.trace("no parent at position 0 for commit {}", commit);
        }

        treeWalk.addTree(new EmptyTreeIterator());
      }
    }
    else
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("no parent available for commit {}", commit);
      }

      treeWalk.addTree(new EmptyTreeIterator());
    }

    treeWalk.addTree(commit.getTree());

    List<DiffEntry> entries = DiffEntry.scan(treeWalk);

    for (DiffEntry e : entries)
    {
      if (!e.getOldId().equals(e.getNewId()))
      {
        if (modifications == null)
        {
          modifications = new Modifications();
        }

        appendModification(modifications, e);
      }
    }

    return modifications;
  }

  /**
   * TODO cache
   *
   *
   * @param repository
   *
   */
  private void createTagMap(org.eclipse.jgit.lib.Repository repository)
  {
    tags = ArrayListMultimap.create();

    Map<String, Ref> tagMap = repository.getTags();

    if (tagMap != null)
    {
      for (Map.Entry<String, Ref> e : tagMap.entrySet())
      {
        try
        {

          RevCommit c = getCommit(repository, e.getValue());

          if (c != null)
          {
            tags.put(c.getId(), e.getKey());
          }
          else if (logger.isWarnEnabled())
          {
            logger.warn("could not find commit for tag {}", e.getKey());
          }

        }
        catch (IOException ex)
        {
          logger.error("could not read commit for ref", ex);
        }

      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param ref
   *
   * @return
   *
   * @throws IOException
   */
  private RevCommit getCommit(org.eclipse.jgit.lib.Repository repository,
    Ref ref)
    throws IOException
  {
    RevCommit commit = null;
    ObjectId id = ref.getPeeledObjectId();

    if (id == null)
    {
      id = ref.getObjectId();
    }

    if (id != null)
    {
      if (revWalk == null)
      {
        revWalk = new RevWalk(repository);
      }

      commit = revWalk.parseCommit(id);
    }

    return commit;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private int idLength;

  /** Field description */
  private RevWalk revWalk;

  /** Field description */
  private Multimap<ObjectId, String> tags;

  /** Field description */
  private TreeWalk treeWalk;
}

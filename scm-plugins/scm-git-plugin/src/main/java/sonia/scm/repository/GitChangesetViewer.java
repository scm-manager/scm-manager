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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitChangesetViewer implements ChangesetViewer
{

  /** Field description */
  public static final int ID_LENGTH = 20;

  /** the logger for GitChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(GitChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public GitChangesetViewer(GitRepositoryHandler handler, Repository repository)
  {
    this.handler = handler;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param start
   * @param max
   *
   * @return
   */
  @Override
  public ChangesetPagingResult getChangesets(int start, int max)
  {
    ChangesetPagingResult changesets = null;
    File directory = handler.getDirectory(repository);
    org.eclipse.jgit.lib.Repository gr = null;
    TreeWalk treeWalk = null;

    try
    {
      gr = GitUtil.open(directory);

      int counter = 0;
      List<Changeset> changesetList = new ArrayList<Changeset>();

      if (!gr.getAllRefs().isEmpty())
      {
        Git git = new Git(gr);

        treeWalk = new TreeWalk(gr);

        Map<ObjectId, String> tags = createTagMap(gr);

        for (RevCommit commit : git.log().call())
        {
          if ((counter >= start) && (counter < start + max))
          {
            changesetList.add(createChangeset(treeWalk, tags, commit));
          }

          counter++;
        }
      }

      changesets = new ChangesetPagingResult(counter, changesetList);
    }
    catch (NoHeadException ex)
    {
      logger.error("could not read changesets", ex);
    }
    catch (IOException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      GitUtil.release(treeWalk);
      GitUtil.close(gr);
    }

    return changesets;
  }

  //~--- methods --------------------------------------------------------------

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
   *
   * @param treeWalk
   * @param tags
   * @param commit
   *
   * @return
   *
   * @throws IOException
   */
  private Changeset createChangeset(TreeWalk treeWalk,
                                    Map<ObjectId, String> tags,
                                    RevCommit commit)
          throws IOException
  {
    String id = commit.getId().abbreviate(ID_LENGTH).name();
    long date = GitUtil.getCommitTime(commit);
    PersonIdent authorIndent = commit.getCommitterIdent();
    Person author = new Person(authorIndent.getName(),
                               authorIndent.getEmailAddress());
    String message = commit.getShortMessage();
    Changeset changeset = new Changeset(id, date, author, message);
    Modifications modifications = createModifications(treeWalk, commit);

    if (modifications != null)
    {
      changeset.setModifications(modifications);
    }

    String tag = tags.get(commit.getId());

    if (tag != null)
    {
      changeset.getTags().add(tag);
    }

    return changeset;
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
      treeWalk.addTree(commit.getParent(0).getTree());
    }
    else
    {
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
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private Map<ObjectId,
              String> createTagMap(org.eclipse.jgit.lib.Repository repository)
  {
    Map<ObjectId, String> tagMap = new HashMap<ObjectId, String>();
    Map<String, Ref> tags = repository.getTags();

    if (tags != null)
    {
      for (Map.Entry<String, Ref> e : tags.entrySet())
      {
        tagMap.put(e.getValue().getObjectId(), e.getKey());
      }
    }

    return tagMap;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}

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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.Util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

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
   */
  public GitChangesetConverter(org.eclipse.jgit.lib.Repository repository)
  {
    this(repository, null);
  }

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param revWalk
   */
  public GitChangesetConverter(org.eclipse.jgit.lib.Repository repository,
    RevWalk revWalk)
  {
    this.repository = repository;

    if (revWalk != null)
    {
      this.revWalk = revWalk;

    }
    else
    {
      this.revWalk = new RevWalk(repository);
    }

    this.tags = GitUtil.createTagMap(repository, revWalk);
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
   * @param commit
   *
   * @return
   *
   * @throws IOException
   */
  public Changeset createChangeset(RevCommit commit) throws IOException
  {
    List<String> branches = Lists.newArrayList();
    Set<Ref> refs = repository.getAllRefsByPeeledObjectId().get(commit.getId());

    if (Util.isNotEmpty(refs))
    {

      for (Ref ref : refs)
      {
        String branch = GitUtil.getBranch(ref);

        if (branch != null)
        {
          branches.add(branch);
        }
      }

    }

    return createChangeset(commit, branches);
  }

  /**
   * Method description
   *
   *
   * @param commit
   * @param branch
   *
   * @return
   *
   * @throws IOException
   */
  public Changeset createChangeset(RevCommit commit, String branch)
    throws IOException
  {
    return createChangeset(commit, Lists.newArrayList(branch));
  }

  /**
   * Method description
   *
   *
   *
   * @param commit
   * @param branches
   *
   * @return
   *
   * @throws IOException
   */
  public Changeset createChangeset(RevCommit commit, List<String> branches)
    throws IOException
  {
    String id = commit.getId().name();
    List<String> parentList = null;
    RevCommit[] parents = commit.getParents();

    if (Util.isNotEmpty(parents))
    {
      parentList = new ArrayList<String>();

      for (RevCommit parent : parents)
      {
        parentList.add(parent.getId().name());
      }
    }

    long date = GitUtil.getCommitTime(commit);
    PersonIdent authorIndent = commit.getAuthorIdent();
    Person author = new Person(authorIndent.getName(),
                      authorIndent.getEmailAddress());
    String message = commit.getFullMessage();

    if (message != null)
    {
      message = message.trim();
    }

    Changeset changeset = new Changeset(id, date, author, message);

    if (parentList != null)
    {
      changeset.setParents(parentList);
    }

    Collection<String> tagCollection = tags.get(commit.getId());

    if (Util.isNotEmpty(tagCollection))
    {

      // create a copy of the tag collection to reduce memory on caching
      changeset.getTags().addAll(Lists.newArrayList(tagCollection));
    }

    changeset.setBranches(branches);

    return changeset;
  }



  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private org.eclipse.jgit.lib.Repository repository;

  /** Field description */
  private RevWalk revWalk;

  /** Field description */
  private Multimap<ObjectId, String> tags;

  /** Field description */
  private TreeWalk treeWalk;
}

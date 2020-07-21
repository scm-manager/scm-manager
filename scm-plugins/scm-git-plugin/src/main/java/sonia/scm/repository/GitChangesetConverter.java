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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
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
import java.util.Collections;
import java.util.List;

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
  GitChangesetConverter(org.eclipse.jgit.lib.Repository repository)
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
  GitChangesetConverter(org.eclipse.jgit.lib.Repository repository,
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
  public Changeset createChangeset(RevCommit commit)
  {
    return createChangeset(commit, Collections.emptyList());
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
    PersonIdent committerIdent = commit.getCommitterIdent();
    Person author = createPersonFor(authorIndent);
    String message = commit.getFullMessage();

    if (message != null)
    {
      message = message.trim();
    }

    Changeset changeset = new Changeset(id, date, author, message);
    if (!committerIdent.equals(authorIndent)) {
      changeset.addContributor(new Contributor("Committed-by", createPersonFor(committerIdent)));
    }

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

  public Person createPersonFor(PersonIdent personIndent) {
    return new Person(personIndent.getName(), personIndent.getEmailAddress());
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

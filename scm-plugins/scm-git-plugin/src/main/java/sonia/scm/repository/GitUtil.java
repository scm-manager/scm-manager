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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
public final class GitUtil
{

  /** Field description */
  public static final String REF_HEAD = "HEAD";

  /** Field description */
  public static final String REF_HEAD_PREFIX = "refs/heads/";

  /** Field description */
  public static final String REF_MASTER = "master";

  /** Field description */
  private static final String PREFIX_HEADS = "refs/heads/";

  /** Field description */
  private static final String PREFIX_TAG = "refs/tags/";

  /** Field description */
  private static final String REFSPEC = "+refs/heads/*:refs/remote/scm/%s/*";

  /** Field description */
  private static final String REMOTE_REF = "refs/remote/scm/%s/%s";

  /** Field description */
  private static final int TIMEOUT = 5;

  /** the logger for GitUtil */
  private static final Logger logger = LoggerFactory.getLogger(GitUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private GitUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repo
   */
  public static void close(org.eclipse.jgit.lib.Repository repo)
  {
    if (repo != null)
    {
      repo.close();
    }
  }

  /**
   * TODO cache
   *
   *
   * @param repository
   * @param revWalk
   *
   *
   * @return
   */
  public static Multimap<ObjectId,
    String> createTagMap(org.eclipse.jgit.lib.Repository repository,
      RevWalk revWalk)
  {
    Multimap<ObjectId, String> tags = ArrayListMultimap.create();

    Map<String, Ref> tagMap = repository.getTags();

    if (tagMap != null)
    {
      for (Map.Entry<String, Ref> e : tagMap.entrySet())
      {
        try
        {

          RevCommit c = getCommit(repository, revWalk, e.getValue());

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

    return tags;
  }

  /**
   * Method description
   *
   *
   * @param git
   * @param directory
   * @param remoteRepository
   *
   * @return
   *
   * @throws GitAPIException
   *
   * @throws RepositoryException
   */
  public static FetchResult fetch(Git git, File directory,
    Repository remoteRepository)
    throws RepositoryException
  {
    try
    {
      FetchCommand fetch = git.fetch();

      fetch.setRemote(directory.getAbsolutePath());
      fetch.setRefSpecs(createRefSpec(remoteRepository));
      fetch.setTimeout((int) TimeUnit.MINUTES.toSeconds(TIMEOUT));

      return fetch.call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryException("could not fetch", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   *
   * @throws IOException
   */
  public static org.eclipse.jgit.lib.Repository open(File directory)
    throws IOException
  {
    return RepositoryCache.open(RepositoryCache.FileKey.lenient(directory,
      FS.DETECTED), true);
  }

  /**
   * Method description
   *
   *
   * @param formatter
   */
  public static void release(DiffFormatter formatter)
  {
    if (formatter != null)
    {
      formatter.release();
    }
  }

  /**
   * Method description
   *
   *
   * @param walk
   */
  public static void release(TreeWalk walk)
  {
    if (walk != null)
    {
      walk.release();
    }
  }

  /**
   * Method description
   *
   *
   * @param walk
   */
  public static void release(RevWalk walk)
  {
    if (walk != null)
    {
      walk.release();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ref
   *
   * @return
   */
  public static String getBranch(Ref ref)
  {
    String branch = null;

    if (ref != null)
    {
      branch = getBranch(ref.getName());
    }

    return branch;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public static String getBranch(String name)
  {
    String branch = null;

    if (Util.isNotEmpty(name) && name.startsWith(PREFIX_HEADS))
    {
      branch = name.substring(PREFIX_HEADS.length());
    }

    return branch;
  }

  /**
   * Method description
   *
   *
   * @param repo
   * @param branchName
   *
   * @return
   *
   * @throws IOException
   */
  public static ObjectId getBranchId(org.eclipse.jgit.lib.Repository repo,
    String branchName)
    throws IOException
  {
    ObjectId branchId = null;

    if (!branchName.startsWith(REF_HEAD))
    {
      branchName = PREFIX_HEADS.concat(branchName);
    }

    checkBranchName(repo, branchName);

    try
    {
      Ref ref = repo.getRef(branchName);

      if (ref != null)
      {
        branchId = ref.getObjectId();
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("could not find branch for {}", branchName);
      }

    }
    catch (Exception ex)
    {
      logger.warn("error occured during resolve of branch id", ex);
    }

    return branchId;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param revWalk
   * @param ref
   *
   * @return
   *
   * @throws IOException
   */
  public static RevCommit getCommit(org.eclipse.jgit.lib.Repository repository,
    RevWalk revWalk, Ref ref)
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

  /**
   * Method description
   *
   *
   * @param commit
   *
   * @return
   */
  public static long getCommitTime(RevCommit commit)
  {
    long date = commit.getCommitTime();

    date = date * 1000;

    return date;
  }

  /**
   * Method description
   *
   *
   * @param objectId
   *
   * @return
   */
  public static String getId(ObjectId objectId)
  {
    String id = Util.EMPTY_STRING;

    if (objectId != null)
    {
      id = objectId.name();
    }

    return id;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param id
   *
   * @return
   *
   * @throws IncorrectObjectTypeException
   * @throws MissingObjectException
   *
   * @throws IOException
   */
  public static Ref getRefForCommit(org.eclipse.jgit.lib.Repository repository,
    ObjectId id)
    throws IOException
  {
    Ref ref = null;
    RevWalk walk = null;

    try
    {
      walk = new RevWalk(repository);

      RevCommit commit = walk.parseCommit(id);

      for (Map.Entry<String, Ref> e : repository.getAllRefs().entrySet())
      {
        if (e.getKey().startsWith(Constants.R_HEADS))
        {
          if (walk.isMergedInto(commit,
            walk.parseCommit(e.getValue().getObjectId())))
          {
            ref = e.getValue();
          }
        }
      }

    }
    finally
    {
      release(walk);
    }

    return ref;
  }

  /**
   * Method description
   *
   *
   * @param repo
   *
   * @return
   *
   * @throws IOException
   */
  public static ObjectId getRepositoryHead(org.eclipse.jgit.lib.Repository repo)
    throws IOException
  {
    ObjectId id = null;
    String head = null;
    Map<String, Ref> refs = repo.getAllRefs();

    for (Map.Entry<String, Ref> e : refs.entrySet())
    {
      String key = e.getKey();

      if (REF_HEAD.equals(key))
      {
        head = REF_HEAD;
        id = e.getValue().getObjectId();

        break;
      }
      else if (key.startsWith(REF_HEAD_PREFIX))
      {
        id = e.getValue().getObjectId();
        head = key.substring(REF_HEAD_PREFIX.length());

        if (REF_MASTER.equals(head))
        {
          break;
        }
      }
    }

    if (id == null)
    {
      id = repo.resolve(Constants.HEAD);
    }

    if (logger.isDebugEnabled())
    {
      if ((head != null) && (id != null))
      {
        logger.debug("use {}:{} as repository head", head, id.name());
      }
      else if (id != null)
      {
        logger.debug("use {} as repository head", id.name());
      }
      else
      {
        logger.warn("could not find repository head");
      }
    }

    return id;
  }

  /**
   * Method description
   *
   *
   * @param repo
   * @param revision
   *
   * @return
   *
   * @throws IOException
   */
  public static ObjectId getRevisionId(org.eclipse.jgit.lib.Repository repo,
    String revision)
    throws IOException
  {
    ObjectId revId = null;

    if (Util.isNotEmpty(revision))
    {
      revId = repo.resolve(revision);
    }
    else
    {
      revId = getRepositoryHead(repo);
    }

    return revId;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param localBranch
   *
   * @return
   */
  public static String getScmRemoteRefName(Repository repository,
    Ref localBranch)
  {
    return getScmRemoteRefName(repository, localBranch.getName());
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param localBranch
   *
   * @return
   */
  public static String getScmRemoteRefName(Repository repository,
    String localBranch)
  {
    return String.format(REMOTE_REF, repository.getId(), localBranch);
  }

  /**
   * Method description
   *
   *
   * @param ref
   *
   * @return
   */
  public static String getTagName(Ref ref)
  {
    String name = ref.getName();

    if (name.startsWith(PREFIX_TAG))
    {
      name = name.substring(PREFIX_TAG.length());
    }

    return name;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repo
   * @param branchName
   *
   * @throws IOException
   */
  @VisibleForTesting
  static void checkBranchName(org.eclipse.jgit.lib.Repository repo,
    String branchName)
    throws IOException
  {
    if (branchName.contains(".."))
    {
      File repoDirectory = repo.getDirectory();
      File branchFile = new File(repoDirectory, branchName);

      if (!branchFile.getCanonicalPath().startsWith(
        repoDirectory.getCanonicalPath()))
      {
        logger.error(
          "branch \"{}\" is outside of the repository. It looks like path traversal attack",
          branchName);

        throw new IllegalArgumentException(
          branchName.concat(" is an invalid branch name"));
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private static RefSpec createRefSpec(Repository repository)
  {
    return new RefSpec(String.format(REFSPEC, repository.getId()));
  }
}

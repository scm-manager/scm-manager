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
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.attributes.Attribute;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.LfsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.GitUserAgentProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class GitUtil
{
  
  private static final GitUserAgentProvider GIT_USER_AGENT_PROVIDER = new GitUserAgentProvider();

  /** Field description */
  public static final String REF_HEAD = "HEAD";

  /** Field description */
  public static final String REF_HEAD_PREFIX = "refs/heads/";

  /** Field description */
  public static final String REF_MASTER = "master";

  /** Field description */
  private static final String DIRECTORY_DOTGIT = ".git";

  /** Field description */
  private static final String DIRECTORY_OBJETCS = "objects";

  /** Field description */
  private static final String DIRECTORY_REFS = "refs";

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

  /** Field description */
  private static final String USERAGENT_GIT = "git/";

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

  public static FetchResult fetch(Git git, File directory, Repository remoteRepository) {
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
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity("Remote", directory.toString()).in(remoteRepository), "could not fetch", ex);
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
    FS fs = FS.DETECTED;
    FileRepositoryBuilder builder = new FileRepositoryBuilder();

    builder.setFS(fs);

    if (isGitDirectory(fs, directory))
    {

      // bare repository
      builder.setGitDir(directory).setBare();
    }
    else
    {
      builder.setWorkTree(directory);
    }

    return builder.build();
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
      formatter.close();
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
      walk.close();
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
      walk.close();
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
   * Returns {@code true} if the provided reference name is a branch name.
   * 
   * @param refName reference name
   * 
   * @return {@code true} if the name is a branch name
   * 
   * @since 1.50
   */
  public static boolean isBranch(String refName)
  {
    return Strings.nullToEmpty(refName).startsWith(PREFIX_HEADS);
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
  public static Ref getBranchId(org.eclipse.jgit.lib.Repository repo,
    String branchName)
    throws IOException
  {
    Ref ref = null;
    if (!branchName.startsWith(REF_HEAD))
    {
      branchName = PREFIX_HEADS.concat(branchName);
    }

    checkBranchName(repo, branchName);

    try
    {
      ref = repo.findRef(branchName);

      if (ref == null)
      {
        logger.warn("could not find branch for {}", branchName);
      }
    }
    catch (IOException ex)
    {
      logger.warn("error occured during resolve of branch id", ex);
    }

    return ref;
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
  public static String getId(AnyObjectId objectId)
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

  public static ObjectId getRepositoryHead(org.eclipse.jgit.lib.Repository repo) {
    return getRepositoryHeadRef(repo).map(Ref::getObjectId).orElse(null);
  }

  public static Optional<Ref> getRepositoryHeadRef(org.eclipse.jgit.lib.Repository repo) {
    Optional<Ref> foundRef = findMostAppropriateHead(repo.getAllRefs());

    if (foundRef.isPresent()) {
      if (logger.isDebugEnabled()) {
        logger.debug("use {}:{} as repository head for directory {}",
          foundRef.map(GitUtil::getBranch).orElse(null),
          foundRef.map(Ref::getObjectId).map(ObjectId::name).orElse(null),
          repo.getDirectory());
      }
    } else {
      logger.warn("could not find repository head in directory {}", repo.getDirectory());
    }

    return foundRef;
  }

  private static Optional<Ref> findMostAppropriateHead(Map<String, Ref> refs) {
    Ref refHead = refs.get(REF_HEAD);
    if (refHead != null && refHead.isSymbolic() && isBranch(refHead.getTarget().getName())) {
      return of(refHead.getTarget());
    }

    Ref master = refs.get(REF_HEAD_PREFIX + REF_MASTER);
    if (master != null) {
      return of(master);
    }

    Ref develop = refs.get(REF_HEAD_PREFIX + "develop");
    if (develop != null) {
      return of(develop);
    }

    return refs.entrySet()
      .stream()
      .filter(e -> e.getKey().startsWith(REF_HEAD_PREFIX))
      .map(Map.Entry::getValue)
      .findFirst();
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
    ObjectId revId;

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
    String branch = localBranch;

    if (localBranch.startsWith(REF_HEAD_PREFIX))
    {
      branch = localBranch.substring(REF_HEAD_PREFIX.length());
    }

    return String.format(REMOTE_REF, repository.getId(), branch);
  }

  /**
   * Returns the name of the tag or {@code null} if the the ref is not a tag.
   * 
   * @param refName ref name
   * 
   * @return name of tag or {@link null}
   * 
   * @since 1.50
   */
  public static String getTagName(String refName)
  {
    String tagName = null;
    if (refName.startsWith(PREFIX_TAG))
    {
      tagName = refName.substring(PREFIX_TAG.length());
    }

    return tagName;
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

  /**
   * Returns true if the request comes from a git client.
   *
   *
   * @param request servlet request
   *
   * @return true if the client is git
   */
  public static boolean isGitClient(HttpServletRequest request)
  {
    return GIT_USER_AGENT_PROVIDER.parseUserAgent(request.getHeader(HttpUtil.HEADER_USERAGENT)) != null;
  }

  /**
   * Method description
   *
   *
   * @param dir
   *
   * @return
   */
  public static boolean isGitDirectory(File dir)
  {
    return isGitDirectory(FS.DETECTED, dir);
  }

  /**
   * Method description
   *
   *
   * @param fs
   * @param dir
   *
   * @return
   */
  public static boolean isGitDirectory(FS fs, File dir)
  {
    //J-
    return fs.resolve(dir, DIRECTORY_OBJETCS).exists()
      && fs.resolve(dir, DIRECTORY_REFS).exists() 
      &&!fs.resolve(dir, DIRECTORY_DOTGIT).exists();
    //J+
  }

  /**
   * Method description
   *
   *
   * @param ref
   *
   * @return
   */
  public static boolean isHead(String ref)
  {
    return ref.startsWith(REF_HEAD_PREFIX);
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public static boolean isValidObjectId(ObjectId id)
  {
    return (id != null) &&!id.equals(ObjectId.zeroId());
  }

  /**
   * Computes the first common ancestor of two revisions, aka merge base.
   */
  public static ObjectId computeCommonAncestor(org.eclipse.jgit.lib.Repository repository, ObjectId revision1, ObjectId revision2) throws IOException {
    try (RevWalk mergeBaseWalk = new RevWalk(repository)) {
      mergeBaseWalk.setRevFilter(RevFilter.MERGE_BASE);
      mergeBaseWalk.markStart(mergeBaseWalk.lookupCommit(revision1));
      mergeBaseWalk.markStart(mergeBaseWalk.parseCommit(revision2));
      RevCommit ancestor = mergeBaseWalk.next();
      doThrow()
        .violation("revisions " + revision1.name() + " and " + revision2.name() + " are not related and therefore do not have a common ancestor", "revisions")
        .when(ancestor == null);
      return ancestor.getId();
    }
  }

  public static Optional<LfsPointer> getLfsPointer(org.eclipse.jgit.lib.Repository repo, String path, RevCommit commit, TreeWalk treeWalk) throws IOException {
    Attributes attributes = LfsFactory.getAttributesForPath(repo, path, commit);

    Attribute filter = attributes.get("filter");
    if (filter != null && "lfs".equals(filter.getValue())) {
      ObjectId blobId = treeWalk.getObjectId(0);
      try (InputStream is = repo.open(blobId, Constants.OBJ_BLOB).openStream()) {
        return of(LfsPointer.parseLfsPointer(is));
      }
    } else {
      return empty();
    }
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

/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitIncomingCommand extends AbstractGitCommand
  implements IncomingCommand
{

  /** Field description */
  private static final String REFSPEC = "+refs/heads/*:refs/remote/scm/%s/*";

  /** Field description */
  private static final String REMOTE_REF = "refs/remote/scm/%s/%s";

  /** Field description */
  private static final String REMOTE_REF_PREFIX = "refs/remote/scm/%s/";

  /** Field description */
  private static final int TIMEOUT = 5;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   */
  GitIncomingCommand(GitRepositoryHandler handler, GitContext context,
    Repository repository)
  {
    super(context, repository);
    this.handler = handler;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getIncomingChangesets(
    IncomingCommandRequest request)
    throws IOException, RepositoryException
  {
    Repository remoteRepository = request.getRemoteRepository();

    Git git = Git.wrap(open());
    FetchCommand fetch = git.fetch();

    fetch.setRemote(handler.getDirectory(remoteRepository).getAbsolutePath());
    fetch.setRefSpecs(createRefSpec(remoteRepository));
    fetch.setTimeout((int) TimeUnit.MINUTES.toSeconds(TIMEOUT));

    try
    {
      fetch.call();
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryException("could not fetch", ex);
    }

    ObjectId local = GitUtil.getRepositoryHead(git.getRepository());

    Ref remoteBranch = getRemoteBranch(git.getRepository(), local,
                         remoteRepository);

    // TODO paging
    List<Changeset> changesets = Lists.newArrayList();

    if (remoteBranch != null)
    {

      GitChangesetConverter converter = null;
      RevWalk walk = null;

      try
      {
        walk = new RevWalk(git.getRepository());
        converter = new GitChangesetConverter(git.getRepository(), walk);

        org.eclipse.jgit.api.LogCommand log = git.log();

        if (local != null)
        {
          log.not(local);
        }

        Iterable<RevCommit> commits =
          log.add(remoteBranch.getObjectId()).call();

        for (RevCommit commit : commits)
        {
          changesets.add(converter.createChangeset(commit));
        }

        changesets = Lists.reverse(changesets);
      }
      catch (Exception ex)
      {
        throw new RepositoryException("could not execute incoming command", ex);
      }
      finally
      {
        Closeables.close(converter, true);
        GitUtil.release(walk);
      }

    }

    return new ChangesetPagingResult(changesets.size(), changesets);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  private RefSpec createRefSpec(Repository repository)
  {
    return new RefSpec(String.format(REFSPEC, repository.getId()));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param local
   * @param remoteRepository
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private Ref getRemoteBranch(org.eclipse.jgit.lib.Repository repository,
    ObjectId local, Repository remoteRepository)
    throws IOException, RepositoryException
  {
    Ref ref = null;

    if (local != null)
    {
      Ref localBranch = GitUtil.getRefForCommit(repository, local);

      if (localBranch != null)
      {
        ref = repository.getRef(getScmRemoteRefName(remoteRepository,
          localBranch));
      }
    }
    else
    {
      ref = repository.getRef(getScmRemoteRefName(remoteRepository, "master"));

      if (ref == null)
      {
        String prefix = String.format(REMOTE_REF_PREFIX,
                          remoteRepository.getId());

        for (Entry<String, Ref> e : repository.getAllRefs().entrySet())
        {
          if (e.getKey().startsWith(prefix))
          {
            if (ref != null)
            {
              throw new RepositoryException("could not find remote branch");
            }

            ref = e.getValue();

            break;
          }
        }
      }
    }

    return ref;
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
  private String getScmRemoteRefName(Repository repository, Ref localBranch)
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
  private String getScmRemoteRefName(Repository repository, String localBranch)
  {
    return String.format(REMOTE_REF, repository.getId(), localBranch);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;
}

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


import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;


public abstract class AbstractGitIncomingOutgoingCommand
  extends AbstractGitCommand
{

  private static final String REMOTE_REF_PREFIX = "refs/remote/scm/%s/";

  private GitRepositoryHandler handler;
  private final GitChangesetConverterFactory converterFactory;

  AbstractGitIncomingOutgoingCommand(GitContext context, GitRepositoryHandler handler, GitChangesetConverterFactory converterFactory) {
    super(context);
    this.handler = handler;
    this.converterFactory = converterFactory;
  }

  protected abstract void prepareLogCommand(
    org.eclipse.jgit.api.LogCommand logCommand, ObjectId localId,
    ObjectId remoteId)
    throws IOException;


  protected abstract boolean retrieveChangesets(ObjectId localId,
    ObjectId remoteId);


  protected ChangesetPagingResult getIncomingOrOutgoingChangesets(PagedRemoteCommandRequest request) throws IOException {
    Repository remoteRepository = request.getRemoteRepository();

    Git git = Git.wrap(open());

    GitUtil.fetch(git, handler.getDirectory(remoteRepository.getId()), remoteRepository);

    ObjectId localId = getDefaultBranch(git.getRepository());
    ObjectId remoteId = null;

    Ref remoteBranch = getRemoteBranch(git.getRepository(), localId, remoteRepository);

    if (remoteBranch != null)
    {
      remoteId = remoteBranch.getObjectId();
    }

    // TODO paging
    List<Changeset> changesets = Lists.newArrayList();

    if (retrieveChangesets(localId, remoteId))
    {

      GitChangesetConverter converter = null;
      RevWalk walk = null;

      try
      {
        walk = new RevWalk(git.getRepository());
        converter = converterFactory.create(git.getRepository(), walk);

        org.eclipse.jgit.api.LogCommand log = git.log();

        prepareLogCommand(log, localId, remoteId);

        Iterable<RevCommit> commits = log.call();

        for (RevCommit commit : commits)
        {
          changesets.add(converter.createChangeset(commit));
        }

        changesets = Lists.reverse(changesets);
      }
      catch (Exception ex)
      {
        throw new InternalRepositoryException(repository, "could not execute incoming command", ex);
      }
      finally
      {
        Closeables.close(converter, true);
        GitUtil.release(walk);
      }

    }

    return new ChangesetPagingResult(changesets.size(), changesets);
  }

  private Ref getRemoteBranch(org.eclipse.jgit.lib.Repository repository, ObjectId local, Repository remoteRepository) throws IOException {
    Ref ref = null;

    if (local != null)
    {
      Ref localBranch = GitUtil.getRefForCommit(repository, local);

      if (localBranch != null)
      {
        ref = repository.findRef(GitUtil.getScmRemoteRefName(remoteRepository,
          localBranch));
      }
    }
    else
    {
      ref = repository.findRef(GitUtil.getScmRemoteRefName(remoteRepository,
        "master"));

      if (ref == null)
      {
        String prefix = String.format(REMOTE_REF_PREFIX,
                          remoteRepository.getId());

        for (Entry<String, Ref> e : repository.getAllRefs().entrySet())
        {
          if (e.getKey().startsWith(prefix))
          {
            ref = e.getValue();
            break;
          }
        }
      }
    }

    return ref;
  }

}

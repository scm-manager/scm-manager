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

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;

import java.io.File;
import java.util.Collection;

public abstract class AbstractGitPushOrPullCommand extends AbstractGitCommand {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractGitPushOrPullCommand.class);

  protected GitRepositoryHandler handler;

  protected AbstractGitPushOrPullCommand(GitRepositoryHandler handler, GitContext context) {
    super(context);
    this.handler = handler;
  }

  protected long push(Repository source, String remoteUrl, String username, String password) {
    Git git = Git.wrap(source);
    org.eclipse.jgit.api.PushCommand push = git.push();

    if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
      push.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password.toCharArray()));
    }
    push.setPushAll().setPushTags();
    push.setRemote(remoteUrl);

    long counter = -1;

    try {
      Iterable<PushResult> results = push.call();

      if (results != null) {
        counter = 0;

        for (PushResult result : results) {
          counter += count(git, result);
        }
      }
    } catch (Exception ex) {
      throw new InternalRepositoryException(repository, "could not execute push/pull command", ex);
    }

    return counter;
  }

  protected String getRemoteUrl(RemoteCommandRequest request) {
    String url;
    sonia.scm.repository.Repository remRepo = request.getRemoteRepository();

    if (remRepo != null) {
      url = getRemoteUrl(remRepo);
    } else if (request.getRemoteUrl() != null) {
      url = request.getRemoteUrl().toExternalForm();
    } else {
      throw new IllegalArgumentException("repository or url is required");
    }

    return url;
  }

  protected String getRemoteUrl(File directory) {
    return directory.toURI().toASCIIString();
  }

  protected String getRemoteUrl(sonia.scm.repository.Repository repository) {
    return getRemoteUrl(handler.getDirectory(repository.getId()));
  }

  private long count(Git git, PushResult result) {
    long counter = 0;
    Collection<RemoteRefUpdate> updates = result.getRemoteUpdates();

    for (RemoteRefUpdate update : updates) {
      counter += count(git, update);
    }

    return counter;
  }

  private long count(Git git, RemoteRefUpdate update) {
    long counter = 0;

    if (GitUtil.isHead(update.getRemoteName())) {
      try {
        org.eclipse.jgit.api.LogCommand log = git.log();
        ObjectId oldId = update.getExpectedOldObjectId();

        if (GitUtil.isValidObjectId(oldId)) {
          log.not(oldId);
        }

        ObjectId newId = update.getNewObjectId();

        if (GitUtil.isValidObjectId(newId)) {
          log.add(newId);
        }

        Iterable<RevCommit> commits = log.call();

        if (commits != null) {
          counter += Iterables.size(commits);
        }

        LOG.trace("counting {} commits for ref update {}", counter, update);
      } catch (Exception ex) {
        LOG.error("could not count pushed/pulled changesets", ex);
      }
    } else {
      LOG.debug("do not count non branch ref update {}", update);
    }

    return counter;
  }
}

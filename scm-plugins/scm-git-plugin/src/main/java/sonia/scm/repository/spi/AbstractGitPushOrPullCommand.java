/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.PushFailedException;

import java.io.File;
import java.util.Collection;
import java.util.List;

public abstract class AbstractGitPushOrPullCommand extends AbstractGitCommand {

  private static final List<RemoteRefUpdate.Status> REJECTED_STATUSES = List.of(
    RemoteRefUpdate.Status.REJECTED_NODELETE,
    RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD,
    RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED,
    RemoteRefUpdate.Status.REJECTED_OTHER_REASON
  );
  private static final Logger LOG = LoggerFactory.getLogger(AbstractGitPushOrPullCommand.class);

  protected GitRepositoryHandler handler;

  protected AbstractGitPushOrPullCommand(GitRepositoryHandler handler, GitContext context) {
    super(context);
    this.handler = handler;
  }

  protected long push(Repository source, String remoteUrl, String username, String password, boolean force) {
    Git git = Git.wrap(source);
    org.eclipse.jgit.api.PushCommand push = git.push();

    if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
      push.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password.toCharArray()));
    }
    push.setPushAll().setPushTags();
    push.setRemote(remoteUrl);
    push.setForce(force);

    long counter;

    try {
      Iterable<PushResult> results = push.call();
      if (hasPushFailed(results)) {
        throw new PushFailedException(repository);
      }

      counter = 0;

      for (PushResult result : results) {
        counter += count(git, result);
      }
    } catch (PushFailedException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InternalRepositoryException(repository, "could not execute push/pull command", ex);
    }

    return counter;
  }

  private boolean hasPushFailed(Iterable<PushResult> pushResults) {
    if (pushResults == null) {
      return true;
    }

    for (PushResult nextResult : pushResults) {
      for(RemoteRefUpdate remoteUpdate : nextResult.getRemoteUpdates()) {
        if(REJECTED_STATUSES.contains(remoteUpdate.getStatus())) {
          return true;
        }
      }
    }

    return false;
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

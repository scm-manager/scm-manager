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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.ContextEntry;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.WorkingCopy;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.NON_EXISTING;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.OK;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD;
import static org.eclipse.jgit.transport.RemoteRefUpdate.Status.UP_TO_DATE;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;
import static sonia.scm.repository.GitUtil.getBranchIdOrCurrentHead;
import static sonia.scm.repository.spi.IntegrateChangesFromWorkdirException.forMessage;


class AbstractGitCommand {


  private static final Logger logger = LoggerFactory.getLogger(AbstractGitCommand.class);
  private static final Collection<RemoteRefUpdate.Status> ACCEPTED_UPDATE_STATUS = asList(OK, UP_TO_DATE, NON_EXISTING);

  protected GitContext context;

  protected sonia.scm.repository.Repository repository;

  AbstractGitCommand(GitContext context) {
    this.repository = context.getRepository();
    this.context = context;
  }

  static ObjectId resolveRevisionOrThrowNotFound(Repository repository, String revision, sonia.scm.repository.Repository scmRepository) throws IOException {
    ObjectId resolved = repository.resolve(revision);
    if (resolved == null) {
      throw notFound(entity("Revision", revision).in(scmRepository));
    } else {
      return resolved;
    }
  }

  Repository open() {
    return context.open();
  }

  ObjectId getCommitOrDefault(Repository gitRepository, String requestedCommit) throws IOException {
    ObjectId commit;
    if (Strings.isNullOrEmpty(requestedCommit)) {
      commit = getDefaultBranch(gitRepository);
    } else {
      commit = gitRepository.resolve(requestedCommit);
    }
    return commit;
  }

  ObjectId getDefaultBranch(Repository gitRepository) throws IOException {
    Ref ref = getBranchOrDefault(gitRepository, null);
    if (ref == null) {
      return null;
    } else {
      return ref.getObjectId();
    }
  }

  Ref getBranchOrDefault(Repository gitRepository, String requestedBranch) throws IOException {
    if (Strings.isNullOrEmpty(requestedBranch)) {
      String defaultBranchName = context.getConfig().getDefaultBranch();
      return getBranchIdOrCurrentHead(gitRepository, defaultBranchName);
    } else {
      return getBranchIdOrCurrentHead(gitRepository, requestedBranch);
    }
  }

  <R, W extends GitCloneWorker<R>> R inClone(Function<Git, W> workerSupplier, GitWorkingCopyFactory workingCopyFactory, String initialBranch) {
    try (WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(context, initialBranch)) {
      Repository repository = workingCopy.getWorkingRepository();
      logger.debug("cloned repository to folder {}", repository.getWorkTree());
      return workerSupplier.apply(new Git(repository)).run();
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository", e);
    }
  }

  ObjectId resolveRevisionOrThrowNotFound(Repository repository, String revision) throws IOException {
    sonia.scm.repository.Repository scmRepository = context.getRepository();
    return resolveRevisionOrThrowNotFound(repository, revision, scmRepository);
  }

  abstract static class GitCloneWorker<R> {
    private final Git clone;
    private final GitContext context;
    private final sonia.scm.repository.Repository repository;

    GitCloneWorker(Git clone, GitContext context, sonia.scm.repository.Repository repository) {
      this.clone = clone;
      this.context = context;
      this.repository = repository;
    }

    abstract R run() throws IOException;

    Git getClone() {
      return clone;
    }

    sonia.scm.repository.Repository getRepository() {
      return repository;
    }

    ObjectId resolveRevision(String revision) throws IOException {
      ObjectId resolved = clone.getRepository().resolve(revision);
      if (resolved == null) {
        return resolveRevisionOrThrowNotFound(clone.getRepository(), "origin/" + revision, context.getRepository());
      } else {
        return resolved;
      }
    }

    void push(String... refSpecs) {
      push(false, refSpecs);
    }

    void forcePush(String... refSpecs) {
      push(true, refSpecs);
    }

    private void push(boolean force, String... refSpecs) {
      logger.trace("Pushing mirror result to repository {} with refspec '{}'", repository, refSpecs);
      try {
        Iterable<PushResult> pushResults =
          clone
            .push()
            .setRefSpecs(stream(refSpecs).map(RefSpec::new).collect(toList()))
            .setForce(force)
            .call();
        Iterator<PushResult> pushResultIterator = pushResults.iterator();
        if (!pushResultIterator.hasNext()) {
          throw new InternalRepositoryException(repository, "got no result from push");
        }
        PushResult pushResult = pushResultIterator.next();
        Collection<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates();
        if (remoteUpdates.isEmpty()) {
          throw new InternalRepositoryException(repository, "push created no update");
        }
        remoteUpdates
          .stream()
          .filter(remoteRefUpdate -> !ACCEPTED_UPDATE_STATUS.contains(remoteRefUpdate.getStatus()))
          .findAny()
          .ifPresent(remoteRefUpdate -> {
            if (remoteRefUpdate.getStatus() == REJECTED_NONFASTFORWARD) {
              logger.debug("non fast-forward change detected; probably the remote {} has been changed during the modification: {}", remoteRefUpdate.getRemoteName(), pushResult.getMessages());
              throw new ConcurrentModificationException(ContextEntry.ContextBuilder.entity("Branch", remoteRefUpdate.getRemoteName()).in(repository).build());
            } else {
              logger.info("message for unexpected push result {} for remote {}: {}", remoteRefUpdate.getStatus(), remoteRefUpdate.getRemoteName(), pushResult.getMessages());
              throw forMessage(repository, pushResult.getMessages());
            }
          });
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(repository, "could not push changes into central repository", e);
      }
      logger.debug("pushed changes");
    }
  }
}

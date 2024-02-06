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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.ContextEntry;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
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

  Repository open() throws IOException {
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

  static ObjectId resolveRevisionOrThrowNotFound(Repository repository, String revision, sonia.scm.repository.Repository scmRepository) throws IOException {
    ObjectId resolved = repository.resolve(revision);
    if (resolved == null) {
      throw notFound(entity("Revision", revision).in(scmRepository));
    } else {
      return resolved;
    }
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

    GitContext getContext() {
      return context;
    }

    sonia.scm.repository.Repository getRepository() {
      return repository;
    }

    void checkOutBranch(String branchName) throws IOException {
      try {
        clone.checkout().setName(branchName).call();
      } catch (RefNotFoundException e) {
        logger.trace("could not checkout branch {} directly; trying to create local branch", branchName, e);
        checkOutTargetAsNewLocalBranch(branchName);
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(repository, "could not checkout branch: " + branchName, e);
      }
    }

    private void checkOutTargetAsNewLocalBranch(String branchName) throws IOException {
      try {
        ObjectId targetRevision = resolveRevision(branchName);
        clone.checkout().setStartPoint(targetRevision.getName()).setName(branchName).setCreateBranch(true).call();
      } catch (RefNotFoundException e) {
        logger.debug("could not checkout branch {} as local branch", branchName, e);
        throw notFound(entity("Revision", branchName).in(repository));
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(repository, "could not checkout branch as local branch: " + branchName, e);
      }
    }

    ObjectId resolveRevision(String revision) throws IOException {
      ObjectId resolved = clone.getRepository().resolve(revision);
      if (resolved == null) {
        return resolveRevisionOrThrowNotFound(clone.getRepository(), "origin/" + revision, context.getRepository());
      } else {
        return resolved;
      }
    }

    void failIfNotChanged(Supplier<RuntimeException> doThrow) {
      try {
        if (clone.status().call().isClean()) {
          throw doThrow.get();
        }
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(repository, "could not read status of repository", e);
      }
    }

    Optional<RevCommit> doCommit(String message, Person author, boolean sign) {
      Person authorToUse = determineAuthor(author);
      try {
        Status status = clone.status().call();
        if (!status.isClean() || isInMerge()) {
          return of(clone.commit()
            .setAuthor(authorToUse.getName(), authorToUse.getMail())
            .setCommitter(authorToUse.getName(), authorToUse.getMail())
            .setMessage(message)
            .setSign(sign)
            .setSigningKey(sign ? "SCM-MANAGER-DEFAULT-KEY" : null)
            .call());
        } else {
          return empty();
        }
      } catch (GitAPIException | IOException e) {
        throw new InternalRepositoryException(repository, "could not commit changes", e);
      }
    }

    private boolean isInMerge() throws IOException {
      return clone.getRepository().readMergeHeads() != null && !clone.getRepository().readMergeHeads().isEmpty();
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

    ObjectId getCurrentObjectId() throws IOException {
      return getClone().getRepository().getRefDatabase().findRef("HEAD").getObjectId();
    }

    private Person determineAuthor(Person author) {
      if (author == null) {
        Subject subject = SecurityUtils.getSubject();
        User user = subject.getPrincipals().oneByType(User.class);
        String name = user.getDisplayName();
        String email = user.getMail();
        logger.debug("no author set; using logged in user: {} <{}>", name, email);
        return new Person(name, email);
      } else {
        return author;
      }
    }
  }
}

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


package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.WorkingCopy;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
class AbstractGitCommand
{
  
  /**
   * the logger for AbstractGitCommand
   */
  private static final Logger logger = LoggerFactory.getLogger(AbstractGitCommand.class);

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param repository
   */
  AbstractGitCommand(GitContext context,
                               sonia.scm.repository.Repository repository)
  {
    this.repository = repository;
    this.context = context;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  Repository open() throws IOException
  {
    return context.open();
  }
  
  ObjectId getCommitOrDefault(Repository gitRepository, String requestedCommit) throws IOException {
    ObjectId commit;
    if ( Strings.isNullOrEmpty(requestedCommit) ) {
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
    if ( Strings.isNullOrEmpty(requestedBranch) ) {
      String defaultBranchName = context.getConfig().getDefaultBranch();
      if (!Strings.isNullOrEmpty(defaultBranchName)) {
        return GitUtil.getBranchId(gitRepository, defaultBranchName);
      } else {
        logger.trace("no default branch configured, use repository head as default");
        Optional<Ref> repositoryHeadRef = GitUtil.getRepositoryHeadRef(gitRepository);
        return repositoryHeadRef.orElse(null);
      }
    } else {
      return GitUtil.getBranchId(gitRepository, requestedBranch);
    }
  }

  <R, W extends GitCloneWorker<R>> R inClone(Function<Git, W> workerSupplier, GitWorkdirFactory workdirFactory, String initialBranch) {
    try (WorkingCopy<Repository, Repository> workingCopy = workdirFactory.createWorkingCopy(context, initialBranch)) {
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

    void checkOutBranch(String branchName) throws IOException {
      try {
        clone.checkout().setName(branchName).call();
      } catch (RefNotFoundException e) {
        logger.trace("could not checkout branch {} directly; trying to create local branch", branchName, e);
        checkOutTargetAsNewLocalBranch(branchName);
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not checkout branch: " + branchName, e);
      }
    }

    private void checkOutTargetAsNewLocalBranch(String branchName) throws IOException {
      try {
        ObjectId targetRevision = resolveRevision(branchName);
        clone.checkout().setStartPoint(targetRevision.getName()).setName(branchName).setCreateBranch(true).call();
      } catch (RefNotFoundException e) {
        logger.debug("could not checkout branch {} as local branch", branchName, e);
        throw notFound(entity("Revision", branchName).in(context.getRepository()));
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not checkout branch as local branch: " + branchName, e);
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
        throw new InternalRepositoryException(context.getRepository(), "could not read status of repository", e);
      }
    }

    Optional<RevCommit> doCommit(String message, Person author) {
      Person authorToUse = determineAuthor(author);
      try {
        Status status = clone.status().call();
        if (!status.isClean() || isInMerge()) {
          return of(clone.commit()
            .setAuthor(authorToUse.getName(), authorToUse.getMail())
            .setMessage(message)
            .call());
        } else {
          return empty();
        }
      } catch (GitAPIException | IOException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not commit changes", e);
      }
    }

    private boolean isInMerge() throws IOException {
      return clone.getRepository().readMergeHeads() != null && !clone.getRepository().readMergeHeads().isEmpty();
    }

    void push() {
      try {
        clone.push().call();
      } catch (GitAPIException e) {
        throw new IntegrateChangesFromWorkdirException(repository,
          "could not push changes into central repository", e);
      }
      logger.debug("pushed changes");
    }

    Ref getCurrentRevision() throws IOException {
      return getClone().getRepository().getRefDatabase().findRef("HEAD");
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected GitContext context;

  /** Field description */
  protected sonia.scm.repository.Repository repository;
}

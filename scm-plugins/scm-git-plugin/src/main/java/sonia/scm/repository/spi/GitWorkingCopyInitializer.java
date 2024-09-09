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

import com.google.common.base.Stopwatch;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.work.SimpleWorkingCopyFactory.ParentAndClone;

import java.io.File;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

class GitWorkingCopyInitializer {

  private static final Logger LOG = LoggerFactory.getLogger(GitWorkingCopyInitializer.class);

  private final SimpleGitWorkingCopyFactory simpleGitWorkingCopyFactory;
  private final GitContext context;

  public GitWorkingCopyInitializer(SimpleGitWorkingCopyFactory simpleGitWorkingCopyFactory, GitContext context) {
    this.simpleGitWorkingCopyFactory = simpleGitWorkingCopyFactory;
    this.context = context;
  }

  public ParentAndClone<Repository, Repository> initialize(File target, String initialBranch) {
    LOG.trace("clone repository {}", context.getRepository());
    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      Repository clone = Git.cloneRepository()
        .setURI(simpleGitWorkingCopyFactory.createScmTransportProtocolUri(context.getDirectory()))
        .setDirectory(target)
        .setBranch(initialBranch)
        .call()
        .getRepository();

      Ref head = clone.exactRef(Constants.HEAD);

      if (head == null || !head.isSymbolic() || (initialBranch != null && !head.getTarget().getName().endsWith(initialBranch))) {
        if (clone.getRefDatabase().getRefs().isEmpty()) {
          LOG.warn("could not initialize empty clone with given branch {}; this has to be handled later on", initialBranch);
        } else {
          throw notFound(entity("Branch", initialBranch).in(context.getRepository()));
        }
      }

      return new ParentAndClone<>(null, clone, target);
    } catch (TransportException e) {
      String message = e.getMessage();
      if (initialBranch != null && message.contains(initialBranch) && message.contains("not found")) {
        throw notFound(entity("Branch", initialBranch).in(context.getRepository()));
      } else {
        throw new InternalRepositoryException(context.getRepository(), "could not clone working copy of repository", e);
      }
    } catch (GitAPIException | IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone working copy of repository", e);
    } finally {
      LOG.trace("took {} to clone repository {}", stopwatch.stop(), context.getRepository());
    }
  }
}

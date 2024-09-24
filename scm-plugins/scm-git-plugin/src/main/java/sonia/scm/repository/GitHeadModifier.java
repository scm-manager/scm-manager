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

package sonia.scm.repository;

import jakarta.inject.Inject;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * The GitHeadModifier is able to modify the head of a git repository.
 *
 * @since 1.61
 */
public class GitHeadModifier {

  private static final Logger LOG = LoggerFactory.getLogger(GitHeadModifier.class);

  private final GitRepositoryHandler repositoryHandler;

  @Inject
  public GitHeadModifier(GitRepositoryHandler repositoryHandler) {
    this.repositoryHandler = repositoryHandler;
  }

  /**
   * Ensures that the repositories head points to the given branch. The method will return {@code false} if the
   * repositories head points already to the given branch.
   *
   * @param repository repository to modify
   * @param newHead branch which should be the new head of the repository
   *
   * @return {@code true} if the head has changed
   */
  public boolean ensure(Repository repository, String newHead)  {
    try (org.eclipse.jgit.lib.Repository gitRepository = open(repository)) {
      String currentHead = resolve(gitRepository);
      if (!Objects.equals(currentHead, newHead)) {
        return modify(gitRepository, newHead);
      }
    } catch (IOException ex) {
      LOG.warn("failed to change head of repository", ex);
    }
    return false;
  }

  private String resolve(org.eclipse.jgit.lib.Repository gitRepository) throws IOException {
    Ref ref = gitRepository.getRefDatabase().getRef(Constants.HEAD);
    if ( ref.isSymbolic() ) {
      ref = ref.getTarget();
    }
    return GitUtil.getBranch(ref);
  }

  private boolean modify(org.eclipse.jgit.lib.Repository gitRepository, String newHead) throws IOException {
    RefUpdate refUpdate = gitRepository.getRefDatabase().newUpdate(Constants.HEAD, true);
    refUpdate.setForceUpdate(true);
    RefUpdate.Result result = refUpdate.link(Constants.R_HEADS + newHead);
    return result == RefUpdate.Result.FORCED;
  }

  private org.eclipse.jgit.lib.Repository open(Repository repository) throws IOException {
    File directory = repositoryHandler.getDirectory(repository.getId());
    return GitUtil.open(directory);
  }
}

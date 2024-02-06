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

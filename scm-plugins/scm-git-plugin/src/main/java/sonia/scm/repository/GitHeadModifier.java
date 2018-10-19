/**
 * Copyright (c) 2014, Sebastian Sdorra
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
package sonia.scm.repository;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * The GitHeadModifier is able to modify the head of a git repository.
 *
 * @author Sebastian Sdorra
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
    File directory = repositoryHandler.getDirectory(repository);
    return GitUtil.open(directory);
  }
}

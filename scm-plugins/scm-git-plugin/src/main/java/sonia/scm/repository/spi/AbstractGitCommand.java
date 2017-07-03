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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitConstants;
import sonia.scm.repository.GitUtil;

import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class AbstractGitCommand
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
  protected AbstractGitCommand(GitContext context,
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
  protected Repository open() throws IOException
  {
    return context.open();
  }
  
  protected ObjectId getCommitOrDefault(Repository gitRepository, String requestedCommit) throws IOException {
    ObjectId commit;
    if ( Strings.isNullOrEmpty(requestedCommit) ) {
      commit = getDefaultBranch(gitRepository);
    } else {
      commit = gitRepository.resolve(requestedCommit);
    }
    return commit;
  }
  
  protected ObjectId getBranchOrDefault(Repository gitRepository, String requestedBranch) throws IOException {
    ObjectId head;
    if ( Strings.isNullOrEmpty(requestedBranch) ) {
      head = getDefaultBranch(gitRepository);
    } else {
      head = GitUtil.getBranchId(gitRepository, requestedBranch);
    }
    return head;
  }
  
  protected ObjectId getDefaultBranch(Repository gitRepository) throws IOException {
    ObjectId head;
    String defaultBranchName = repository.getProperty(GitConstants.PROPERTY_DEFAULT_BRANCH);
    if (!Strings.isNullOrEmpty(defaultBranchName)) {
      head = GitUtil.getBranchId(gitRepository, defaultBranchName);
    } else {
      logger.trace("no default branch configured, use repository head as default");
      head = GitUtil.getRepositoryHead(gitRepository);
    }
    return head;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected GitContext context;

  /** Field description */
  protected sonia.scm.repository.Repository repository;
}

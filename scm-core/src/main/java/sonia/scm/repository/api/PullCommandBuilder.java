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


package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.PullCommand;
import sonia.scm.repository.spi.PullCommandRequest;
import sonia.scm.security.RepositoryPermission;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * The pull command pull changes from a other repository.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public final class PullCommandBuilder
{

  /**
   * the logger for PullCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PullCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new PullCommandBuilder.
   *
   *
   * @param command pull command implementation
   * @param localRepository local repository
   */
  PullCommandBuilder(PullCommand command, Repository localRepository)
  {
    this.command = command;
    this.localRepository = localRepository;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Pull all changes from the given remote repository.
   *
   *
   * @param remoteRepository remote repository
   *
   * @return informations over the executed pull command
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public PullResponse pull(Repository remoteRepository)
    throws IOException, RepositoryException
  {
    Subject subject = SecurityUtils.getSubject();

    //J-
    subject.checkPermission(
      new RepositoryPermission(localRepository, PermissionType.WRITE)
    );
    subject.checkPermission(
      new RepositoryPermission(remoteRepository, PermissionType.READ)
    );
    //J+

    request.setRemoteRepository(remoteRepository);

    logger.info("pull changes from {}", remoteRepository);

    return command.pull(request);
  }

  //~--- fields ---------------------------------------------------------------

  /** pull command implementation */
  private PullCommand command;

  /** local repository */
  private Repository localRepository;

  /** pull command request */
  private PullCommandRequest request = new PullCommandRequest();
}

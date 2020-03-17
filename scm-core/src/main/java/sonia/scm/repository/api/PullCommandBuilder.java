/**
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

package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.PullCommand;
import sonia.scm.repository.spi.PullCommandRequest;

import java.io.IOException;
import java.net.URL;

//~--- JDK imports ------------------------------------------------------------

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
   * Pull all changes from the given remote url.
   *
   *
   * @param url remote url
   *
   * @return informations over the executed pull command
   *
   * @throws IOException
   *
   * @since 1.43
   */
  public PullResponse pull(String url) throws IOException {
    Subject subject = SecurityUtils.getSubject();
    //J-
    subject.isPermitted(RepositoryPermissions.push(localRepository).asShiroString());
    //J+
    
    URL remoteUrl = new URL(url);
    request.reset();
    request.setRemoteUrl(remoteUrl);
    
    logger.info("pull changes from url {}", url);
    
    return command.pull(request);
  }
  
  /**
   * Pull all changes from the given remote repository.
   *
   *
   * @param remoteRepository remote repository
   *
   * @return informations over the executed pull command
   *
   * @throws IOException
   */
  public PullResponse pull(Repository remoteRepository) throws IOException {
    Subject subject = SecurityUtils.getSubject();

    //J-
    subject.isPermitted(RepositoryPermissions.push(localRepository).asShiroString());
    subject.isPermitted(RepositoryPermissions.push(remoteRepository).asShiroString());
    //J+

    request.reset();
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

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

package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.PushCommand;
import sonia.scm.repository.spi.PushCommandRequest;

import java.io.IOException;
import java.net.URL;

//~--- JDK imports ------------------------------------------------------------

/**
 * The push command push changes to a other repository.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public final class PushCommandBuilder
{

  /**
   * the logger for PushCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PushCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new PushCommandBuilder.
   *
   * @param command implementation of the {@link PushCommand}
   */
  PushCommandBuilder(PushCommand command)
  {
    this.command = command;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Push all changes to the given remote repository.
   *
   * @param remoteRepository remote repository
   *
   * @return informations of the executed push command
   *
   * @throws IOException
   */
  public PushResponse push(Repository remoteRepository) throws IOException {
    Subject subject = SecurityUtils.getSubject();

    //J-
    subject.isPermitted(RepositoryPermissions.push(remoteRepository).asShiroString());
    //J+

    logger.info("push changes to repository {}", remoteRepository);

    request.reset();
    request.setRemoteRepository(remoteRepository);

    return command.push(request);
  }

  /**
   * Push all changes to the given remote url.
   *
   * @param url url of a remote repository
   *
   * @return informations of the executed push command
   *
   * @throws IOException
   *
   * @since 1.43
   */
  public PushResponse push(String url) throws IOException {

    URL remoteUrl = new URL(url);

    logger.info("push changes to url {}", url);

    request.reset();
    request.setRemoteUrl(remoteUrl);

    return command.push(request);
  }

  //~--- fields ---------------------------------------------------------------

  /** push command implementation */
  private PushCommand command;

  /** push command request */
  private PushCommandRequest request = new PushCommandRequest();
}

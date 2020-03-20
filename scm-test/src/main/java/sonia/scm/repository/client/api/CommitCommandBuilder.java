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
    
package sonia.scm.repository.client.api;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.spi.CommitCommand;
import sonia.scm.repository.client.spi.CommitRequest;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class CommitCommandBuilder
{

  /**
   * the logger for CommitCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(CommitCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   * @param command
   */
  CommitCommandBuilder(CommitCommand command)
  {
    this.command = command;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param author
   * @param message
   *
   * @return
   *
   * @throws IOException
   */
  public Changeset commit(Person author, String message) throws IOException
  {
    request.setAuthor(author);
    request.setMessage(message);

    if (logger.isDebugEnabled())
    {
      logger.debug("commit {}", request);
    }

    Changeset changeset = command.commit(request);

    request.reset();

    return changeset;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CommitCommand command;

  /** Field description */
  private CommitRequest request = new CommitRequest();
}

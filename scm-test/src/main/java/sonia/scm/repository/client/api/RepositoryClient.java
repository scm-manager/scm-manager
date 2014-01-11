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



package sonia.scm.repository.client.api;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.client.spi.RepositoryClientProvider;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class RepositoryClient implements Closeable
{

  /**
   * the logger for RepositoryClient
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryClient.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   * @param clientProvider
   */
  RepositoryClient(RepositoryClientProvider clientProvider)
  {
    this.clientProvider = clientProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void close()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("close client provider");
    }

    IOUtil.close(clientProvider);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public AddCommandBuilder getAddCommand()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("create add command");
    }

    return new AddCommandBuilder(clientProvider.getAddCommand());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public BranchCommandBuilder getBranchCommand()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("create branch command");
    }

    return new BranchCommandBuilder(clientProvider.getBranchCommand());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public CommitCommandBuilder getCommitCommand()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("create commit command");
    }

    return new CommitCommandBuilder(clientProvider.getCommitCommand());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PushCommandBuilder getPushCommand()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("create push command");
    }

    return new PushCommandBuilder(clientProvider.getPushCommand());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public RemoveCommandBuilder getRemoveCommand()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("create remove command");
    }

    return new RemoveCommandBuilder(clientProvider.getRemoveCommand());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TagCommandBuilder getTagCommand()
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("create tag command");
    }

    return new TagCommandBuilder(clientProvider.getTagCommand());
  }

  /**
   * Method description
   *
   *
   * @param command
   *
   * @return
   */
  public boolean isCommandSupported(ClientCommand command)
  {
    return clientProvider.getSupportedClientCommands().contains(command);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final RepositoryClientProvider clientProvider;
}

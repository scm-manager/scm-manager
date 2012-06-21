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

import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.CommandNotSupportedException;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public abstract class RepositoryServiceProvider implements Closeable
{

  /**
   * Method description
   *
   *
   * @return
   */
  public abstract Set<Command> getSupportedCommands();

  //~--- methods --------------------------------------------------------------

  /**
   * The default implementation of this method does nothing. If you need to
   * free resources, close connections or release locks than you have to
   * override this method.
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // should be implmentented from a service provider
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public BlameCommand getBlameCommand()
  {
    throw new CommandNotSupportedException(Command.BLAME);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public BrowseCommand getBrowseCommand()
  {
    throw new CommandNotSupportedException(Command.BROWSE);
  }

  /**
   *  Method description
   *
   *
   *  @return
   */
  public CatCommand getCatCommand()
  {
    throw new CommandNotSupportedException(Command.CAT);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public DiffCommand getDiffCommand()
  {
    throw new CommandNotSupportedException(Command.DIFF);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public LogCommand getLogCommand()
  {
    throw new CommandNotSupportedException(Command.LOG);
  }
}

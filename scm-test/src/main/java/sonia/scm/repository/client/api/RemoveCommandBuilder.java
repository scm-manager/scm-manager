/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.client.api;

//~--- non-JDK imports --------------------------------------------------------

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.client.spi.RemoveCommand;
import sonia.scm.util.Util;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class RemoveCommandBuilder
{

  /**
   * the logger for RemoveCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RemoveCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param directory
   * @param command
   */
  RemoveCommandBuilder(RemoveCommand command)
  {
    this.command = command;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   * @param pathes
   *
   * @return
   */
  public RemoveCommandBuilder remove(String path, String... pathes)  throws IOException
  {
    remove(path);

    if (Util.isNotEmpty(pathes))
    {
      for (String p : pathes)
      {
        remove(p);
      }
    }

    return this;
  }

  /**
   * Method description
   *
   *
   * @param path
   */
  private void remove(String path) throws IOException
  {
    if (Util.isNotEmpty(path))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("add path {}", path);
      }

      command.remove(path);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RemoveCommand command;
}

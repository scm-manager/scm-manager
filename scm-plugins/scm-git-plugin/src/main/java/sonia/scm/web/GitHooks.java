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


package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.RepositoryHookType;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public final class GitHooks
{

  /** Field description */
  public static final String PREFIX_MSG = "[SCM] ";

  /**
   * the logger for GitHooks
   */
  private static final Logger logger = LoggerFactory.getLogger(GitHooks.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param rpack
   * @param commands
   */
  public static void abortIfPossible(RepositoryHookType type,
    ReceivePack rpack, Iterable<ReceiveCommand> commands)
  {
    abortIfPossible(type, rpack, commands, null);
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param rpack
   * @param commands
   * @param message
   */
  public static void abortIfPossible(RepositoryHookType type,
    ReceivePack rpack, Iterable<ReceiveCommand> commands, String message)
  {
    if (type == RepositoryHookType.PRE_RECEIVE)
    {
      for (ReceiveCommand rc : commands)
      {
        rc.setResult(ReceiveCommand.Result.REJECTED_OTHER_REASON);
      }
    }

    if (message != null)
    {
      sendPrefixedError(rpack, message);
    }
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param commands
   *
   * @return
   */
  public static List<ReceiveCommand> filterReceiveable(RepositoryHookType type,
    Iterable<ReceiveCommand> commands)
  {
    List<ReceiveCommand> receiveable = Lists.newArrayList();

    for (ReceiveCommand command : commands)
    {
      if (isReceiveable(type, command))
      {
        receiveable.add(command);
      }
      else
      {
        logger.debug("skip receive command, type={}, ref={}, result={}",
          command.getType(), command.getRefName(), command.getResult());
      }
    }

    return receiveable;
  }

  /**
   * Method description
   *
   *
   * @param rpack
   * @param message
   */
  public static void sendPrefixedError(ReceivePack rpack, String message)
  {
    rpack.sendError(createPrefixedMessage(message));
  }

  /**
   * Method description
   *
   *
   * @param rpack
   * @param message
   */
  public static void sendPrefixedMessage(ReceivePack rpack, String message)
  {
    rpack.sendMessage(createPrefixedMessage(message));
  }

  /**
   * Method description
   *
   *
   * @param message
   *
   * @return
   */
  private static String createPrefixedMessage(String message)
  {
    return PREFIX_MSG.concat(Strings.nullToEmpty(message));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param rc
   * @param type
   *
   * @return
   */
  private static boolean isReceiveable(RepositoryHookType type,
    ReceiveCommand rc)
  {
    //J-
    return ((RepositoryHookType.PRE_RECEIVE == type) && 
            (rc.getResult() == ReceiveCommand.Result.NOT_ATTEMPTED)) || 
           ((RepositoryHookType.POST_RECEIVE == type) && 
            (rc.getResult() == ReceiveCommand.Result.OK));
    //J+
  }
}

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
    
package sonia.scm.web;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.RepositoryHookType;

import java.util.List;


public final class GitHooks
{

  public static final String PREFIX_MSG = "[SCM] ";

 
  private static final Logger logger = LoggerFactory.getLogger(GitHooks.class);

  public static void abortIfPossible(RepositoryHookType type,
    ReceivePack rpack, Iterable<ReceiveCommand> commands)
  {
    abortIfPossible(type, rpack, commands, null);
  }

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


  public static void sendPrefixedError(ReceivePack rpack, String message)
  {
    rpack.sendError(createPrefixedMessage(message));
  }


  public static void sendPrefixedMessage(ReceivePack rpack, String message)
  {
    rpack.sendMessage(createPrefixedMessage(message));
  }


  private static String createPrefixedMessage(String message)
  {
    return PREFIX_MSG.concat(Strings.nullToEmpty(message));
  }



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

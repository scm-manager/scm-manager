/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

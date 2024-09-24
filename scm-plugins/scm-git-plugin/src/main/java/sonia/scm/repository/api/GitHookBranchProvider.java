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

package sonia.scm.repository.api;


import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceiveCommand.Type;

import sonia.scm.repository.GitUtil;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects created, modified and deleted git branches during a hook.
 * 
 */
public class GitHookBranchProvider implements HookBranchProvider
{
    
  private static final Logger logger = LoggerFactory.getLogger(GitHookBranchProvider.class);

  private final List<String> createdOrModified;

  private final List<String> deletedOrClosed;

  public GitHookBranchProvider(List<ReceiveCommand> commands)
  {
    Builder<String> createdOrModifiedBuilder = ImmutableList.builder();
    Builder<String> deletedOrClosedBuilder = ImmutableList.builder();

    for (ReceiveCommand command : commands)
    {
      Type type = command.getType();
      String ref = command.getRefName();
      String branch = GitUtil.getBranch(ref);

      if (Strings.isNullOrEmpty(branch))
      {
        logger.debug("ref {} is not a branch", ref);
      }
      else if (isCreateOrUpdate(type))
      {
        createdOrModifiedBuilder.add(branch);
      }
      else if (command.getType() == Type.DELETE)
      {
        deletedOrClosedBuilder.add(branch);
      }
    }

    createdOrModified = createdOrModifiedBuilder.build();
    deletedOrClosed = deletedOrClosedBuilder.build();
  }
  
  private boolean isCreateOrUpdate(Type type){
    return type == Type.CREATE || type == Type.UPDATE || type == Type.UPDATE_NONFASTFORWARD;
  }


  @Override
  public List<String> getCreatedOrModified()
  {
    return createdOrModified;
  }

  @Override
  public List<String> getDeletedOrClosed()
  {
    return deletedOrClosed;
  }

}

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

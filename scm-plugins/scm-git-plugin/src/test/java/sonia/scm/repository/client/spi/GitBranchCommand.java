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

package sonia.scm.repository.client.spi;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import sonia.scm.repository.Branch;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;
import org.eclipse.jgit.lib.Ref;
import sonia.scm.repository.GitUtil;


public class GitBranchCommand implements BranchCommand
{

 
  GitBranchCommand(Git git)
  {
    this.git = git;
  }


  @Override
  public Branch branch(String name) throws IOException
  {
    try
    {
      Ref ref = git.branchCreate().setName(name).call();
      return Branch.normalBranch(name, GitUtil.getId(ref.getObjectId()));
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryClientException("could not create branch", ex);
    }
  }

  //~--- fields ---------------------------------------------------------------

  private Git git;
}

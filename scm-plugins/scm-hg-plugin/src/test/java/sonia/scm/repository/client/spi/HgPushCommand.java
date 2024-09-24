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

import org.javahg.Repository;
import org.javahg.commands.ExecutionException;
import sonia.scm.repository.client.api.RepositoryClientException;

import java.io.IOException;

/**
 * Mercurial implementation of the {@link PushCommand}.
 *
 */
public class HgPushCommand implements PushCommand
{
  
  private final Repository repository;
  private final String url;

  HgPushCommand(Repository repository, String url)
  {
    this.repository = repository;
    this.url = url;
  }

  @Override
  public void push() throws IOException
  {
    org.javahg.commands.PushCommand cmd = org.javahg.commands.PushCommand.on(repository);
    cmd.cmdAppend("--new-branch");
    try {
      cmd.execute(url);
    } catch (ExecutionException ex) {
      throw new RepositoryClientException("push to repository failed", ex);
    }
  }

  @Override
  public void pushTags() throws IOException {
    push();
  }

}

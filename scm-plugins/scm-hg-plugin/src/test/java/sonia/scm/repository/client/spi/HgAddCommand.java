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
import java.io.IOException;

/**
 * Mercurial implementation of the {@link AddCommand}.
 * 
 */
public final class HgAddCommand implements AddCommand
{
  
  private final Repository repository;

  HgAddCommand(Repository repository)
  {
    this.repository = repository;
  }
  
  @Override
  public void add(String path) throws IOException
  {
    org.javahg.commands.AddCommand.on(repository).execute(path);
  }
  
}

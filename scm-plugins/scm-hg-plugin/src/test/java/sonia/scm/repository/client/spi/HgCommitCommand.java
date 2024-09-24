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
import com.google.common.collect.Lists;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;

import java.io.IOException;

/**
 * Mercurial implementation of the {@link CommitCommand}.
 * 
 */
public class HgCommitCommand implements CommitCommand
{
  
  private final Repository repository;

  HgCommitCommand(Repository repository)
  {
    this.repository = repository;
  }

  @Override
  public Changeset commit(CommitRequest request) throws IOException
  {
    org.javahg.Changeset c = org.javahg.commands.CommitCommand
      .on(repository)
      .user(request.getAuthor().toString())
      .message(request.getMessage())
      .execute();
    
    Changeset changeset = new Changeset(
      c.getNode(), 
      c.getTimestamp().getDate().getTime(), 
      Person.toPerson(c.getUser()),
      c.getMessage()
    );
    
    changeset.setBranches(Lists.newArrayList(c.getBranch()));
    changeset.setTags(c.tags());
    return changeset;
  }
  
}

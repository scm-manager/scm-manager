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
import com.google.common.base.Strings;
import sonia.scm.repository.Tag;

/**
 * Mercurial implementation of the {@link TagCommand}.
 *
 */
public class HgTagCommand implements TagCommand
{
  
  private final Repository repository;

  HgTagCommand(Repository repository)
  {
    this.repository = repository;
  }

  @Override
  public Tag tag(TagRequest request)
  {
    String rev = request.getRevision();
    if ( Strings.isNullOrEmpty(rev) ){
      rev = repository.tip().getNode();
    }
    org.javahg.commands.TagCommand.on(repository)
      .rev(rev)
      .user(request.getUserName())
      .execute(request.getName());
    return new Tag(request.getName(), rev);
  }
  
}

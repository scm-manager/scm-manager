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

package sonia.scm.repository.client.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Tag;
import sonia.scm.repository.client.spi.TagCommand;
import sonia.scm.repository.client.spi.TagRequest;

import java.io.IOException;

/**
 *
 * @since 1.18
 */
public final class TagCommandBuilder
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(TagCommandBuilder.class);

  private TagCommand command;

  private TagRequest request = new TagRequest();
 
  TagCommandBuilder(TagCommand command)
  {
    this.command = command;
  }



  public Tag tag(String name, String username) throws IOException
  {
    request.setName(name);
    request.setUsername(username);

    if (logger.isDebugEnabled())
    {
      logger.debug("tag {}", request);
    }

    Tag tag = command.tag(request);

    request.reset();

    return tag;
  }



  public TagCommandBuilder setRevision(String revision)
  {
    request.setRevision(revision);

    return this;
  }

}

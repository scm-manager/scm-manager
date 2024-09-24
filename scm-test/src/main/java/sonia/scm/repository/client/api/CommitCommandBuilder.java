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

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.spi.CommitCommand;
import sonia.scm.repository.client.spi.CommitRequest;

import java.io.IOException;

/**
 *
 * @since 1.18
 */
public final class CommitCommandBuilder
{

 
  private static final Logger logger =
    LoggerFactory.getLogger(CommitCommandBuilder.class);

  private CommitCommand command;

  private CommitRequest request = new CommitRequest();
 
  CommitCommandBuilder(CommitCommand command)
  {
    this.command = command;
  }

  public Changeset commit(Person author, String message) throws IOException
  {
    request.setAuthor(author);
    request.setMessage(message);

    if (logger.isDebugEnabled())
    {
      logger.debug("commit {}", request);
    }

    Changeset changeset = command.commit(request);

    request.reset();

    return changeset;
  }

}

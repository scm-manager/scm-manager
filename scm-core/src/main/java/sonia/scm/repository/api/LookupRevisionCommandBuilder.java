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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.LookupRevisionCommand;
import sonia.scm.repository.spi.LookupRevisionCommandRequest;

import java.util.Collection;

public class LookupRevisionCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(LookupRevisionCommandBuilder.class);

  private final Repository repository;
  private final LookupRevisionCommand command;

  private final LookupRevisionCommandRequest request = new LookupRevisionCommandRequest();

  public LookupRevisionCommandBuilder(Repository repository, LookupRevisionCommand command) {
    this.repository = repository;
    this.command = command;
  }

  /**
   * Set the prefix of the revisions to search for.
   * @param revisionPrefix The prefix of the revisions to search for.
   * @return this builder instance.
   */
  public LookupRevisionCommandBuilder setRevisionPrefix(String revisionPrefix) {
    request.setRevisionPrefix(revisionPrefix);
    return this;
  }

  /**
   * Set the maximum number of changesets to load. This is set to {@value LookupRevisionCommandRequest#DEFAULT_LIMIT}
   * by default.
   * @param limit The new maximum number of changesets to load.
   * @return this builder instance.
   */
  public LookupRevisionCommandBuilder setLimit(int limit) {
    request.setLimit(limit);
    return this;
  }

  public Collection<Changeset> getChangesets() {
      LOG.debug("Retrieve all changesets from {} with request {}", repository, request);
      return command.getChangesets(request).getChangesets();
  }
}

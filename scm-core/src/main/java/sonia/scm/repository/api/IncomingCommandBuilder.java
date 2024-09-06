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


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.IncomingCommand;
import sonia.scm.repository.spi.IncomingCommandRequest;

import java.io.IOException;

/**
 * The incoming command shows new {@link Changeset}s found in a different
 * repository location.
 *
 * @since 1.31
 */
public final class IncomingCommandBuilder
{
  private IncomingCommand command;

  /** disable the execution of pre processors */
  private boolean disablePreProcessors = false;

  /** disable the execution of pre processors */
  private PreProcessorUtil preProcessorUtil;

  private IncomingCommandRequest request = new IncomingCommandRequest();

  /**
   * Constructs a new {@link IncomingCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManger
   * @param command implementation of the {@link IncomingCommand}
   * @param repository repository to query
   * @param preProcessorUtil pre processor util
   */
  IncomingCommandBuilder(CacheManager cacheManger, IncomingCommand command,
    Repository repository, PreProcessorUtil preProcessorUtil)
  {
    this.command = command;
    this.preProcessorUtil = preProcessorUtil;
  }


  /**
   * Returns the incoming changesets for the remote repository.
   *
   *
   * @param remoteRepository remote repository
   *
   * @return incoming changesets
   *
   * @throws IOException
   */
  public ChangesetPagingResult getIncomingChangesets(
    Repository remoteRepository)
    throws IOException
  {
    Subject subject = SecurityUtils.getSubject();

    subject.isPermitted(RepositoryPermissions.pull(remoteRepository).asShiroString());

    request.setRemoteRepository(remoteRepository);

    // TODO caching
    ChangesetPagingResult cpr = command.getIncomingChangesets(request);

    if (!disablePreProcessors)
    {
      preProcessorUtil.prepareForReturn(remoteRepository, cpr);
    }

    return cpr;
  }


  /**
   * Disable the execution of pre processors.
   *
   *
   * @param disablePreProcessors true to disable the pre processors execution
   *
   * @return {@code this}
   */
  public IncomingCommandBuilder setDisablePreProcessors(
    boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

    return this;
  }

  /**
   * Set the limit for the returned incoming changesets.
   * The default value is 20.
   * Setting the value to -1 means to disable the limit.
   *
   *
   * @param pagingLimit limit for returned changesets
   *
   * @return {@code this}
   */
  public IncomingCommandBuilder setPagingLimit(int pagingLimit)
  {
    request.setPagingLimit(pagingLimit);

    return this;
  }

  /**
   * Sets the start value for paging. The value is 0.
   *
   *
   * @param pagingStart start value for paging
   *
   * @return {@code this}
   */
  public IncomingCommandBuilder setPagingStart(int pagingStart)
  {
    request.setPagingStart(pagingStart);

    return this;
  }

}

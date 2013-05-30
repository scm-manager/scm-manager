/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.IncomingCommand;
import sonia.scm.repository.spi.IncomingCommandRequest;
import sonia.scm.security.RepositoryPermission;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * The incoming command shows new {@link Changeset}s found in a different
 * repository location.
 *
 * @author Sebastian Sdorra
 * @since 1.31
 */
public final class IncomingCommandBuilder
{

  /**
   * Constructs a new {@link IncomingCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the incoming changesets for the remote repository.
   *
   *
   * @param remoteRepository remote repository
   *
   * @return incoming changesets
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getIncomingChangesets(
    Repository remoteRepository)
    throws IOException, RepositoryException
  {
    Subject subject = SecurityUtils.getSubject();

    subject.checkPermission(new RepositoryPermission(remoteRepository,
      PermissionType.READ));

    request.setRemoteRepository(remoteRepository);

    // TODO caching
    ChangesetPagingResult cpr = command.getIncomingChangesets(request);

    if (!disablePreProcessors)
    {
      preProcessorUtil.prepareForReturn(remoteRepository, cpr);
    }

    return cpr;
  }

  //~--- set methods ----------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** incoming command implementation */
  private IncomingCommand command;

  /** disable the execution of pre processors */
  private boolean disablePreProcessors = false;

  /** disable the execution of pre processors */
  private PreProcessorUtil preProcessorUtil;

  /** request object */
  private IncomingCommandRequest request = new IncomingCommandRequest();
}

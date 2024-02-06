/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.api;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.OutgoingCommand;
import sonia.scm.repository.spi.OutgoingCommandRequest;

import java.io.IOException;

/**
 * Show changesets not found in a remote repository.
 *
 * @since 1.31
 */
public final class OutgoingCommandBuilder
{
  private OutgoingCommand command;

  private boolean disablePreProcessors = false;

  private PreProcessorUtil preProcessorUtil;

  private OutgoingCommandRequest request = new OutgoingCommandRequest();

  /**
   * Constructs a new {@link OutgoingCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param command implementation of the {@link OutgoingCommand}
   * @param repository repository to query
   * @param preProcessorUtil pre processor util
   */
  OutgoingCommandBuilder(CacheManager cacheManager, OutgoingCommand command,
    Repository repository, PreProcessorUtil preProcessorUtil)
  {
    this.command = command;
    this.preProcessorUtil = preProcessorUtil;
  }


  /**
   * Returns the outgoing changesets for the remote repository.
   *
   *
   * @param remoteRepository remote repository
   *
   * @return outgoing changesets
   */
  public ChangesetPagingResult getOutgoingChangesets(
    Repository remoteRepository) throws IOException
  {
    Subject subject = SecurityUtils.getSubject();

    subject.isPermitted(RepositoryPermissions.pull(remoteRepository).asShiroString());

    request.setRemoteRepository(remoteRepository);

    // TODO caching
    ChangesetPagingResult cpr = command.getOutgoingChangesets(request);

    if (!disablePreProcessors)
    {
      preProcessorUtil.prepareForReturn(remoteRepository, cpr);
    }

    return cpr;
  }


  /**
   * Disable the execution of pre processors if set to <code>true</code>.
   */
  public OutgoingCommandBuilder setDisablePreProcessors(
    boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

    return this;
  }

  /**
   * Set the limit for the returned outgoing changesets.
   * The default value is 20.
   * Setting the value to -1 means to disable the limit.
   *
   *
   * @param pagingLimit limit for returned changesets
   *
   * @return {@code this}
   */
  public OutgoingCommandBuilder setPagingLimit(int pagingLimit)
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
  public OutgoingCommandBuilder setPagingStart(int pagingStart)
  {
    request.setPagingStart(pagingStart);

    return this;
  }
}

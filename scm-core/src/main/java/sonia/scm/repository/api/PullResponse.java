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

import lombok.Value;

/**
 * The {@link PullResponse} is the result of the methods
 * {@link PullCommandBuilder#pull(sonia.scm.repository.Repository)} and
 * {@link PullCommandBuilder#pull(String)} and
 * contains information for the executed pull command.
 *
 * @since 1.31
 */
public final class PullResponse extends AbstractPushOrPullResponse
{

  private final LfsCount lfsCount;

  public PullResponse() {
    this.lfsCount = new LfsCount(0, 0);
  }

  public PullResponse(long changesetCount)
  {
    this(changesetCount, new LfsCount(0, 0));
  }

  public PullResponse(long changesetCount, LfsCount lfsCount) {
    super(changesetCount);
    this.lfsCount = lfsCount;
  }

  /**
   * Object for the count of potentially loaded lfs files.
   */
  public LfsCount getLfsCount() {
    return lfsCount;
  }

  @Value
  public static class LfsCount {
    /**
     * Count of successfully loaded lfs files.
     */
    int successCount;
    /**
     * Count of failed lfs files.
     */
    int failureCount;
  }
}

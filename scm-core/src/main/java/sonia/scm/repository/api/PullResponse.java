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

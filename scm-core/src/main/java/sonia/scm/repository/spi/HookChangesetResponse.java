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

package sonia.scm.repository.spi;

import sonia.scm.repository.Changeset;

import static java.util.Collections.emptyList;

/**
 * Response object to retrieve {@link Changeset}s during a hook.
 *
 * @author Sebastian Sdorra
 * @since 1.33
 */
public final class HookChangesetResponse {
  private final Iterable<Changeset> addedChangesets;
  private final Iterable<Changeset> removedChangesets;

  public HookChangesetResponse(Iterable<Changeset> addedChangesets, Iterable<Changeset> removedChangesets) {
    this.addedChangesets = addedChangesets;
    this.removedChangesets = removedChangesets;
  }

  public HookChangesetResponse(Iterable<Changeset> changesets) {
    this(changesets, emptyList());
  }


  /**
   * Return added changesets.
   *
   * @return added changesets
   */
  public Iterable<Changeset> getChangesets() {
    return addedChangesets;
  }

  /**
   * Return removed changesets.
   *
   * @return removed changesets
   * @since 2.39.0
   */
  public Iterable<Changeset> getRemovedChangesets() {
    return removedChangesets;
  }

}

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

import sonia.scm.FeatureNotSupportedException;
import sonia.scm.repository.Feature;
import sonia.scm.repository.spi.DiffCommandRequest;

import java.util.Set;

abstract class AbstractDiffCommandBuilder <T extends AbstractDiffCommandBuilder, R extends DiffCommandRequest> {


  /** request for the diff command implementation */
  final R request = createRequest();

  private final Set<Feature> supportedFeatures;

  AbstractDiffCommandBuilder(Set<Feature> supportedFeatures) {
    this.supportedFeatures = supportedFeatures;
  }

  /**
   * Compute the incoming changes of the branch set with {@link #setRevision(String)} in respect to the changeset given
   * here. In other words: What changes would be new to the ancestor changeset given here when the branch would
   * be merged into it. Requires feature {@link sonia.scm.repository.Feature#INCOMING_REVISION}!
   *
   * @return {@code this}
   */
  public T setAncestorChangeset(String revision)
  {
    if (!supportedFeatures.contains(Feature.INCOMING_REVISION)) {
      throw new FeatureNotSupportedException(Feature.INCOMING_REVISION.name());
    }
    request.setAncestorChangeset(revision);

    return self();
  }

  /**
   * Show the difference only for the given path.
   *
   *
   * @param path path for difference
   *
   * @return {@code this}
   */
  public T setPath(String path)
  {
    request.setPath(path);
    return self();
  }

  /**
   * Show the difference only for the given revision or (using {@link #setAncestorChangeset(String)}) between this
   * and another revision.
   *
   *
   * @param revision revision for difference
   *
   * @return {@code this}
   */
  public T setRevision(String revision)
  {
    request.setRevision(revision);
    return self();
  }

  abstract T self();

  abstract R createRequest();

  public T setIgnoreWhitespace(IgnoreWhitespaceLevel ignoreWhitespace) {
    request.setIgnoreWhitespaceLevel(ignoreWhitespace);
    return self();
  }
}

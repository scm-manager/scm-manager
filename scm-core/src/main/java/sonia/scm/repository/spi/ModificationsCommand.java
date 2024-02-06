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

import sonia.scm.FeatureNotSupportedException;
import sonia.scm.repository.Feature;
import sonia.scm.repository.Modifications;

import java.io.IOException;

/**
 * Command to get the modifications applied to files in a revision.
 * <p>
 * Modifications are for example: Add, Update, Delete
 *
 * @since 2.0
 */
public interface ModificationsCommand {

  /**
   * Read the modifications for a single revision.
   */
  Modifications getModifications(String revision) throws IOException;

  /**
   * Read the modifications between two revisions. The result is similar to a diff between
   * these two revisions, but without details about the content.
   * <br>
   * Make sure your repository supports the feature {@link Feature#MODIFICATIONS_BETWEEN_REVISIONS},
   * because otherwise this will throw a {@link FeatureNotSupportedException}.
   *
   * @throws FeatureNotSupportedException if the repository type does not support the feature
   *                                      {@link FeatureNotSupportedException}.
   * @since 2.23.0
   */
  default Modifications getModifications(String baseRevision, String revision) throws IOException {
    throw new FeatureNotSupportedException(Feature.MODIFICATIONS_BETWEEN_REVISIONS.name());
  }

  /**
   * Execute the given {@link ModificationsCommandRequest}.
   */
  @SuppressWarnings("java:S3655") // don't know why this should be an issue here. We check 'isPresent' before 'get' on 'request.getBaseRevision()'
  default Modifications getModifications(ModificationsCommandRequest request) throws IOException {
    if (request.getBaseRevision().isPresent()) {
      return getModifications(request.getBaseRevision().get(), request.getRevision());
    } else {
      return getModifications(request.getRevision());
    }
  }
}

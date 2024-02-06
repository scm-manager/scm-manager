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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Feature;
import sonia.scm.repository.spi.DiffResultCommand;
import sonia.scm.repository.spi.DiffResultCommandRequest;

import java.io.IOException;
import java.util.Set;

public class DiffResultCommandBuilder extends AbstractDiffCommandBuilder<DiffResultCommandBuilder, DiffResultCommandRequest> {

  private static final Logger LOG = LoggerFactory.getLogger(DiffResultCommandBuilder.class);

  private final DiffResultCommand diffResultCommand;

  DiffResultCommandBuilder(DiffResultCommand diffResultCommand, Set<Feature> supportedFeatures) {
    super(supportedFeatures);
    this.diffResultCommand = diffResultCommand;
  }

  /**
   * Sets an offset for the first file diff entry that will be created in the result. If there are fewer entries than the
   * given offset, an empty result will be created.
   *
   * @param offset The number of the first diff file entry that will be added to the result.
   * @return This builder instance.
   * @since 2.15.0
   */
  public DiffResultCommandBuilder setOffset(Integer offset) {
    request.setOffset(offset);
    return this;
  }

  /**
   * Sets a limit for the file diff entries that will be created.
   *
   * @param limit The maximum number of file diff entries that will be created in the result.
   * @return This builder instance.
   * @since 2.15.0
   */
  public DiffResultCommandBuilder setLimit(Integer limit) {
    request.setLimit(limit);
    return this;
  }

  /**
   * Returns the content of the difference as parsed objects.
   */
  public DiffResult getDiffResult() throws IOException {
    Preconditions.checkArgument(request.isValid(),
      "path and/or revision is required");

    LOG.debug("create diff result for {}", request);

    return diffResultCommand.getDiffResult(request);
  }

  @Override
  DiffResultCommandBuilder self() {
    return this;
  }

  @Override
  DiffResultCommandRequest createRequest() {
    return new DiffResultCommandRequest();
  }
}

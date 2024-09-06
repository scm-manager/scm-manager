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

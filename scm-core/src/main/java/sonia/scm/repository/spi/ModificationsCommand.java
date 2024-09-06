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

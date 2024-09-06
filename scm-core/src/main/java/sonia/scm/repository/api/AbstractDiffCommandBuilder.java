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

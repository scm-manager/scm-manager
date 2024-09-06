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

package sonia.scm;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static sonia.scm.util.Util.createSubCollection;

/**
 * This represents the result of a page request. Contains the results for
 * the page and the overall count of all elements.
 */
public class PageResult<T extends ModelObject> {

  private final Collection<T> entities;
  private final int overallCount;

  public static <T extends ModelObject> PageResult<T> createPage(Collection<T> allEntities, int pageNumber, int pageSize) {
    checkArgument(pageSize > 0, "pageSize must be at least 1");
    checkArgument(pageNumber >= 0, "pageNumber must be non-negative");

    Collection<T> pagedEntities = createSubCollection(allEntities, pageNumber * pageSize, pageSize);

    return new PageResult<>(pagedEntities, allEntities.size());
  }

  public PageResult(Collection<T> entities, int overallCount) {
    this.entities = entities;
    this.overallCount = overallCount;
  }

  /**
   * The entities for the current page.
   */
  public Collection<T> getEntities() {
    return Collections.unmodifiableCollection(entities);
  }

  /**
   * The overall count of all available elements.
   */
  public int getOverallCount() {
    return overallCount;
  }
}

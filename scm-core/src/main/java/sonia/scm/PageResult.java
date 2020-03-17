/**
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

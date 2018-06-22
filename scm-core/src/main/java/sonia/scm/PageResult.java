package sonia.scm;

import java.util.Collection;
import java.util.Collections;

/**
 * This represents the result of a page request. Contains the results for
 * the page and the overall count of all elements.
 */
public class PageResult<T extends ModelObject> {

  private final Collection<T> entities;
  private final int overallCount;

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

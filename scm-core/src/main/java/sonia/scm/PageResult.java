package sonia.scm;

import java.util.Collection;
import java.util.Collections;

/**
 * This represents the result of a page request. Contains the results for
 * the page and a flag whether there are more pages or not.
 */
public class PageResult<T extends ModelObject> {

  private final Collection<T> entities;
  private final boolean hasMore;

  public PageResult(Collection<T> entities, boolean hasMore) {
    this.entities = entities;
    this.hasMore = hasMore;
  }

  /**
   * The entities for the current page.
   */
  public Collection<T> getEntities() {
    return Collections.unmodifiableCollection(entities);
  }

  /**
   * If this is <code>true</code>, there are more pages (that is, more entities).
   */
  public boolean hasMore() {
    return hasMore;
  }
}

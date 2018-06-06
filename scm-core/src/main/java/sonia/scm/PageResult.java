package sonia.scm;

import java.util.Collection;
import java.util.Collections;

public class PageResult<T extends ModelObject> {

  private final Collection<T> entities;
  private final boolean hasMore;

  public PageResult(Collection<T> entities, boolean hasMore) {
    this.entities = entities;
    this.hasMore = hasMore;
  }

  public Collection<T> getEntities() {
    return Collections.unmodifiableCollection(entities);
  }

  public boolean hasMore() {
    return hasMore;
  }
}

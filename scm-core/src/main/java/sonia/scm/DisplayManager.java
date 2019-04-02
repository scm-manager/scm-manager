package sonia.scm;

import java.util.Collection;
import java.util.Optional;

public interface DisplayManager<T extends ReducedModelObject> {

  int DEFAULT_LIMIT = 5;

  /**
   * Returns a {@link Collection} of filtered objects
   *
   * @param filter the searched string
   * @return filtered object from the store
   */
  Collection<T> autocomplete(String filter);

  Optional<T> get(String id);
}

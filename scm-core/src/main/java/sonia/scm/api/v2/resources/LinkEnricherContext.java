package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Context object for the {@link LinkEnricher}. The context holds the source object for the json and all related
 * objects, which can be useful for the link creation.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class LinkEnricherContext {

  private final Map<Class, Object> instanceMap;

  private LinkEnricherContext(Map<Class,Object> instanceMap) {
    this.instanceMap = instanceMap;
  }

  /**
   * Creates a context with the given entries
   *
   * @param instances entries of the context
   *
   * @return context of given entries
   */
  public static LinkEnricherContext of(Object... instances) {
    ImmutableMap.Builder<Class, Object> builder = ImmutableMap.builder();
    for (Object instance : instances) {
      builder.put(instance.getClass(), instance);
    }
    return new LinkEnricherContext(builder.build());
  }

  /**
   * Returns the registered object from the context. The method will return an empty optional, if no object with the
   * given type was registered.
   *
   * @param type type of instance
   * @param <T> type of instance
   * @return optional instance
   */
  public <T> Optional<T> oneByType(Class<T> type) {
    Object instance = instanceMap.get(type);
    if (instance != null) {
      return Optional.of(type.cast(instance));
    }
    return Optional.empty();
  }

  /**
   * Returns the registered object from the context, but throws an {@link NoSuchElementException} if the type was not
   * registered.
   *
   * @param type type of instance
   * @param <T> type of instance
   * @return instance
   */
  public <T> T oneRequireByType(Class<T> type) {
    Optional<T> instance = oneByType(type);
    if (instance.isPresent()) {
      return instance.get();
    } else {
      throw new NoSuchElementException("No instance for given type present");
    }
  }

}

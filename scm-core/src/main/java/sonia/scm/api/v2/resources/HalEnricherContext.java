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

package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Context object for the {@link HalEnricher}. The context holds the source object for the json and all related
 * objects, which can be useful for the enrichment.
 *
 * @since 2.0.0
 */
public final class HalEnricherContext {

  private final Map<Class<?>, Object> instanceMap;

  private HalEnricherContext(Map<Class<?>,Object> instanceMap) {
    this.instanceMap = instanceMap;
  }

  /**
   * Creates a context with the given entries
   *
   * @param instances entries of the context
   */
  public static HalEnricherContext of(Object... instances) {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
    for (Object instance : instances) {
      builder.put(instance.getClass(), instance);
    }
    return new HalEnricherContext(builder.build());
  }

  /**
   * Return builder for {@link HalEnricherContext}.
   * @since 2.23.0
   */
  public static Builder builder() {
    return new Builder();
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

  /**
   * Builder for {@link HalEnricherContext}.
   *
   * @since 2.23.0
   */
  public static class Builder {

    private final ImmutableMap.Builder<Class<?>, Object> mapBuilder = ImmutableMap.builder();

    /**
     * Add an entry with the given type to the context.
     * @param type type of the object
     * @param object object
     * @param <T> type of object
     * @return {@code this}
     */
    public <T> Builder put(Class<? super T> type, T object) {
      mapBuilder.put(type, object);
      return this;
    }

    /**
     * Returns the {@link HalEnricherContext}.
     */
    public HalEnricherContext build() {
      return new HalEnricherContext(mapBuilder.build());
    }

  }

}

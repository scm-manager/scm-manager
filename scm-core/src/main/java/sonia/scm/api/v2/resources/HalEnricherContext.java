/*
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

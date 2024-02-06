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

package sonia.scm.store;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * ConfigurationStore for configuration objects. <strong>Note:</strong> the default
 * implementation use JAXB to marshall the configuration objects.
 *
 * @param <T> type of the configuration objects
 */
public interface ConfigurationStore<T> {

  /**
   * Returns the configuration object from store.
   *
   * @return configuration object from store
   */
  T get();

  /**
   * Returns the configuration object from store.
   *
   * @return configuration object from store
   */
  default Optional<T> getOptional() {
    return ofNullable(get());
  }

  /**
   * Stores the given configuration object to the store.
   *
   * @param object configuration object to store
   */
  void set(T object);

  /**
   * Deletes the configuration.
   * @since 2.24.0
   */
  default void delete() {
    throw new StoreException("Delete operation is not implemented by the store");
  }
}

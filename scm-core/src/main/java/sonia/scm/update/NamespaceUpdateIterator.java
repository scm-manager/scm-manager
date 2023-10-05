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

package sonia.scm.update;

import sonia.scm.migration.UpdateException;

import java.util.function.Consumer;

/**
 * Implementations of this interface can be used to iterate all namespaces in update steps.
 *
 * @since 2.47.0
 */
public interface NamespaceUpdateIterator {

  /**
   * Calls the given consumer with each namespace.
   *
   * @since 2.47.0
   */
  void forEachNamespace(Consumer<String> namespace);

  /**
   * Equivalent to {@link #forEachNamespace(Consumer)} with the difference, that you can throw exceptions in the given
   * update code, that will then be wrapped in a {@link UpdateException}.
   *
   * @since 2.47.0
   */
  default void updateEachNamespace(Updater updater) {
    forEachNamespace(
      namespace -> {
        try {
          updater.update(namespace);
        } catch (Exception e) {
          throw new UpdateException("failed to update namespace " + namespace, e);
        }
      }
    );
  }

  /**
   * Simple callback with the name of an existing namespace with the possibility to throw exceptions.
   *
   * @since 2.47.0
   */
  interface Updater {
    /**
     * Implements the update logic for a single namespace, denoted by its name.
     */
    @SuppressWarnings("java:S112") // We explicitly want to allow arbitrary exceptions here
    void update(String namespace) throws Exception;
  }
}

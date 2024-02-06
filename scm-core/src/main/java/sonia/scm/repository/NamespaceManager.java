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

package sonia.scm.repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages namespaces. Mind that namespaces do not have a lifecycle on their own, but only do exist through
 * repositories. Therefore you cannot create or delete namespaces, but just change related settings like permissions
 * associated with them.
 *
 * @since 2.6.0
 */
public interface NamespaceManager {

  /**
   * Returns {@link Optional} with the namespace for the given name, or an empty {@link Optional} if there is no such
   * namespace (that is, there is no repository with this namespace).
   */
  Optional<Namespace> get(String namespace);

  /**
   * Returns a {@link java.util.Collection} of all namespaces.
   */
  Collection<Namespace> getAll();

  /**
   * Modifies the given namespace.
   */
  void modify(Namespace namespace);
}

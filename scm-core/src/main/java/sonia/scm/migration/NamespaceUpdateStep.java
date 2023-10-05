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

package sonia.scm.migration;

import sonia.scm.plugin.ExtensionPoint;

/**
 * This is the main interface for "namespace specific" data migration/update. Using this interface, SCM-Manager
 * provides the possibility to change data structures between versions for a given type of data. This class should be
 * used only for namespace specific data (eg. the store is created with a call of <code>forNamespace</code> in a store
 * factory. To migrate global data, use a {@link UpdateStep}.
 * <p>For information about {@link #getAffectedDataType()} and {@link #getTargetVersion()}, see the package
 * documentation.</p>
 *
 * @see sonia.scm.migration
 *
 * @since 2.47.0
 */
@ExtensionPoint
public interface NamespaceUpdateStep extends UpdateStepTarget {
  /**
   * Implement this to update the data to the new version for a specific namespace. If any {@link Exception} is thrown,
   * SCM-Manager will not start up.
   *
   * @param namespaceUpdateContext A context providing specifics about the namespace, whose data should be migrated
   *                                (eg. its name).
   */
  @SuppressWarnings("java:S112") // we suppress this one, because an implementation should feel free to throw any exception it deems necessary
  void doUpdate(NamespaceUpdateContext namespaceUpdateContext) throws Exception;
}

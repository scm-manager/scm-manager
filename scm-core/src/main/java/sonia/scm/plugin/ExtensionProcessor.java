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

package sonia.scm.plugin;


import com.google.inject.Binder;
import sonia.scm.config.ConfigBinding;

import static java.util.Collections.emptySet;

/**
 * Process and resolve extensions.
 *
 * @since 2.0.0
 */
public interface ExtensionProcessor
{

  /**
   * Collect extension classes by extension point.
   *
   *
   * @param <T> type of extension
   * @param extensionPoint extension point
   *
   * @return extensions
   */
  <T> Iterable<Class<? extends T>> byExtensionPoint(Class<T> extensionPoint);

  /**
   * Returns single extension by its extension point.
   *
   *
   * @param <T> type of extension
   * @param extensionPoint extension point
   *
   * @return extension
   */
  <T> Class<? extends T> oneByExtensionPoint(Class<T> extensionPoint);

  /**
   * Process auto bind extensions.
   */
  void processAutoBindExtensions(Binder binder);


  /**
   * Returns all collected web elements (servlets and filters).
   */
  Iterable<WebElementExtension> getWebElements();

  /**
   * @since 3.0.0
   */
  default Iterable<ConfigBinding> getConfigBindings() {
    return emptySet();
  }

  /**
   * Returns all collected indexable types.
   * @since 2.21.0
   */
  default Iterable<Class<?>> getIndexedTypes() {
    return emptySet();
  }

  /**
   * Returns all queryable types.
   * @since 3.8.0
   */
  default Iterable<QueryableTypeDescriptor> getQueryableTypes() {
    return emptySet();
  }
}

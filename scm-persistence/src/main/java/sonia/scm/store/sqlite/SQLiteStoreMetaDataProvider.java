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

package sonia.scm.store.sqlite;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.QueryableTypeDescriptor;
import sonia.scm.store.StoreException;
import sonia.scm.store.StoreMetaDataProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
public class SQLiteStoreMetaDataProvider implements StoreMetaDataProvider {

  private final Map<Collection<String>, Collection<Class<?>>> typesForParents = new HashMap<>();
  private final ClassLoader classLoader;

  @Inject
  SQLiteStoreMetaDataProvider(PluginLoader pluginLoader) {
    classLoader = pluginLoader.getUberClassLoader();
    Iterable<QueryableTypeDescriptor> queryableTypes = pluginLoader.getExtensionProcessor().getQueryableTypes();
    queryableTypes.forEach(this::initializeType);
  }

  private void initializeType(QueryableTypeDescriptor descriptor) {
    for (int i = 0; i < descriptor.getTypes().length; i++) {
      Collection<String> parentClasses =
        Arrays.stream(Arrays.copyOf(descriptor.getTypes(), i + 1))
          .map(SQLiteStoreMetaDataProvider::removeTrailingClass)
          .toList();
      Collection<Class<?>> classes = typesForParents.computeIfAbsent(parentClasses, k -> new LinkedList<>());
      try {
        classes.add(classLoader.loadClass(descriptor.getClazz()));
      } catch (ClassNotFoundException e) {
        throw new StoreException("Failed to load class '" + descriptor.getClazz() + "' for queryable type descriptor " + descriptor.getName(), e);
      }
    }
  }

  private static String removeTrailingClass(String parentClass) {
    return parentClass.endsWith(".class") ? parentClass.substring(0, parentClass.length() - ".class".length()) : parentClass;
  }

  @Override
  public Collection<Class<?>> getTypesWithParent(Class<?>... classes) {
    Collection<String> classNames =
      Arrays.stream(classes)
        .map(Class::getName)
        .toList();
    return typesForParents.getOrDefault(classNames, List.of());
  }
}

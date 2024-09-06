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

package sonia.scm.search;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.plugin.PluginLoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Singleton
class SearchableTypeResolver {

  private final Map<Class<?>, LuceneSearchableType> classToSearchableType = new HashMap<>();
  private final Map<String, Class<?>> nameToClass = new HashMap<>();

  @Inject
  public SearchableTypeResolver(PluginLoader pluginLoader) {
    this(pluginLoader.getExtensionProcessor().getIndexedTypes());
  }

  @VisibleForTesting
  SearchableTypeResolver(Class<?>... indexedTypes) {
    this(Arrays.asList(indexedTypes));
  }

  @VisibleForTesting
  SearchableTypeResolver(Iterable<Class<?>> indexedTypes) {
    fillMaps(convert(indexedTypes));
  }

  private void fillMaps(Iterable<LuceneSearchableType> types) {
    for (LuceneSearchableType type : types) {
      classToSearchableType.put(type.getType(), type);
      nameToClass.put(type.getName(), type.getType());
    }
  }

  @Nonnull
  private Set<LuceneSearchableType> convert(Iterable<Class<?>> indexedTypes) {
    return StreamSupport.stream(indexedTypes.spliterator(), false)
      .map(SearchableTypes::create)
      .collect(Collectors.toSet());
  }

  public Collection<LuceneSearchableType> getSearchableTypes() {
    return Collections.unmodifiableCollection(classToSearchableType.values());
  }

  public LuceneSearchableType resolve(Object object) {
    return resolve(object.getClass());
  }

  public LuceneSearchableType resolve(Class<?> type) {
    LuceneSearchableType searchableType = classToSearchableType.get(type);
    if (searchableType == null) {
      throw notFound(entity("type", type.getName()));
    }
    return searchableType;
  }

  public LuceneSearchableType resolveByName(String typeName) {
    Class<?> type = nameToClass.get(typeName);
    if (type == null) {
      throw notFound(entity("type", typeName));
    }
    return resolve(type);
  }

}

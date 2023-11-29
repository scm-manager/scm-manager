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

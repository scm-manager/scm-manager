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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@SuppressWarnings("unchecked")
public class LuceneIndexFactory {

  private final IndexManager indexManager;
  @SuppressWarnings("rawtypes")
  private final Map<IndexKey, LuceneIndex> indexes = new ConcurrentHashMap<>();

  @Inject
  public LuceneIndexFactory(IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  public <T> LuceneIndex<T> create(IndexParams indexParams) {
    return indexes.compute(keyOf(indexParams), (key, index) -> {
      if (index != null) {
        index.open();
        return index;
      }
      return new LuceneIndex<>(
        indexParams,
        () -> indexManager.openForWrite(indexParams)
      );
    });
  }

  private IndexKey keyOf(IndexParams indexParams) {
    return new IndexKey(
      indexParams.getSearchableType().getName(), indexParams.getIndex()
    );
  }

  @EqualsAndHashCode
  @AllArgsConstructor
  private static class IndexKey {
    String type;
    String name;
  }
}

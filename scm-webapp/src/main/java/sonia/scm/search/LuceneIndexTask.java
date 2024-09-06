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

import com.google.inject.Injector;
import jakarta.inject.Inject;

import java.io.Serializable;

public abstract class LuceneIndexTask implements Runnable, Serializable {

  private final Class<?> type;
  private final String indexName;

  private transient LuceneIndexFactory indexFactory;
  private transient SearchableTypeResolver searchableTypeResolver;
  private transient Injector injector;

  protected LuceneIndexTask(IndexParams params) {
    this.type = params.getSearchableType().getType();
    this.indexName = params.getIndex();
  }

  @Inject
  public void setIndexFactory(LuceneIndexFactory indexFactory) {
    this.indexFactory = indexFactory;
  }

  @Inject
  public void setSearchableTypeResolver(SearchableTypeResolver searchableTypeResolver) {
    this.searchableTypeResolver = searchableTypeResolver;
  }

  @Inject
  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public abstract IndexTask<?> task(Injector injector);

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void run() {
    LuceneSearchableType searchableType = searchableTypeResolver.resolve(type);
    IndexTask<?> task = task(injector);
    try (LuceneIndex index = indexFactory.create(new IndexParams(indexName, searchableType))) {
      task.update(index);
    }
    task.afterUpdate();
  }


}

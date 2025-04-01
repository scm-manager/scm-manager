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

package sonia.scm.store.file;

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.repository.ClearRepositoryCacheEvent;
import sonia.scm.store.TypedStoreParameters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

@Singleton
public class StoreCacheFactory {

  private final StoreCacheConfigProvider storeCacheConfigProvider;
  private final Collection<StoreCache<?>> caches = new LinkedList<>();

  @Inject
  public StoreCacheFactory(StoreCacheConfigProvider storeCacheConfigProvider) {
    this.storeCacheConfigProvider = storeCacheConfigProvider;
  }

  <S> StoreCache<S> createStoreCache(Function<TypedStoreParameters<?>, S> storeCreator) {
    StoreCache<S> cache = new StoreCache<>(storeCreator, storeCacheConfigProvider.isStoreCacheEnabled());
    caches.add(cache);
    return cache;
  }

  @Subscribe(async = false)
  public void clearCache(ClearRepositoryCacheEvent event) {
    caches.forEach(storeCache -> storeCache.clearCache(event.getRepository().getId()));
  }
}

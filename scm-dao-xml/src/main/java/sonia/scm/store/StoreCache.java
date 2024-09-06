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

package sonia.scm.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.synchronizedMap;

class StoreCache <S> {

  private static final Logger LOG = LoggerFactory.getLogger(StoreCache.class);


  private final Function<TypedStoreParameters<?>, S> cachingStoreCreator;

  StoreCache(Function<TypedStoreParameters<?>, S> storeCreator, Boolean storeCacheEnabled) {
    if (storeCacheEnabled) {
      LOG.info("store cache enabled");
      Map<TypedStoreParameters<?>, S> storeCache = synchronizedMap(new HashMap<>());
      cachingStoreCreator = storeParameters -> storeCache.computeIfAbsent(storeParameters, storeCreator);
    } else {
      cachingStoreCreator = storeCreator;
    }
  }

  S getStore(TypedStoreParameters<?> storeParameters) {
    return cachingStoreCreator.apply(storeParameters);
  }
}

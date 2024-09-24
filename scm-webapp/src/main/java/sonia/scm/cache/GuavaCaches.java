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

package sonia.scm.cache;

import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class GuavaCaches {

  private static final Logger LOG = LoggerFactory.getLogger(GuavaCaches.class);

  private GuavaCaches() {
  }

  public static <K, V> com.google.common.cache.Cache<K, V> create(
    GuavaCacheConfiguration configuration, String name) {
    CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

    // Collect guava cache statistics
    builder.recordStats();

    if (configuration.getConcurrencyLevel() != null) {
      builder.concurrencyLevel(configuration.getConcurrencyLevel());
    }

    if (configuration.getExpireAfterAccess() != null) {
      builder.expireAfterAccess(configuration.getExpireAfterAccess(),
        TimeUnit.SECONDS);
    }

    if (configuration.getExpireAfterWrite() != null) {
      builder.expireAfterWrite(configuration.getExpireAfterWrite(),
        TimeUnit.SECONDS);
    }

    if (configuration.getInitialCapacity() != null) {
      builder.initialCapacity(configuration.getInitialCapacity());
    }

    if (configuration.getMaximumSize() != null) {
      builder.maximumSize(configuration.getMaximumSize());
    }

    if (configuration.getMaximumWeight() != null) {
      builder.maximumWeight(configuration.getMaximumWeight());
    }

    if (isEnabled(configuration.getRecordStats())) {
      builder.recordStats();
    }

    if (isEnabled(configuration.getSoftValues())) {
      builder.softValues();
    }

    if (isEnabled(configuration.getWeakKeys())) {
      builder.weakKeys();
    }

    if (isEnabled(configuration.getWeakValues())) {
      builder.weakKeys();
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace("create new cache {} from builder: {}", name, builder);
    }

    return builder.build();
  }

  private static boolean isEnabled(Boolean v) {
    return (v != null) && v;
  }
}

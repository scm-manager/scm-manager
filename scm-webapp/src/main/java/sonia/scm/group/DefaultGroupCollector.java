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

package sonia.scm.group;

import com.cronutils.utils.VisibleForTesting;
import com.github.legman.Subscribe;
import com.google.common.collect.ImmutableSet;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.security.Authentications;
import sonia.scm.security.LogoutEvent;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.UserEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Collect groups for a certain principal.
 * <strong>Warning</strong>: The class is only for internal use and should never used directly.
 */
@Singleton
public class DefaultGroupCollector implements GroupCollector {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultGroupCollector.class);

  @VisibleForTesting
  static final String CACHE_NAME = "sonia.cache.externalGroups";

  private final GroupDAO groupDAO;
  private final Cache<String, Set<String>> cache;
  private final Set<GroupResolver> groupResolvers;

  private final ConfigurationStore<UserGroupCache> store;

  @Inject
  public DefaultGroupCollector(GroupDAO groupDAO, CacheManager cacheManager, Set<GroupResolver> groupResolvers, ConfigurationStoreFactory configurationStoreFactory) {
    this.groupDAO = groupDAO;
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.groupResolvers = groupResolvers;
    this.store = configurationStoreFactory.withType(UserGroupCache.class).withName("user-group-cache").build();
  }

  @Override
  public Set<String> collect(String principal) {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    if (Authentications.isSubjectAnonymous(principal)) {
      appendInternalGroups(principal, builder);
    } else if (Authentications.isSubjectSystemAccount(principal)) {
      builder.add(AUTHENTICATED);
    } else {
      builder.add(AUTHENTICATED);
      builder.addAll(resolveExternalGroups(principal));
      appendInternalGroups(principal, builder);
    }

    Set<String> groups = builder.build();
    LOG.debug("collected following groups for principal {}: {}", principal, groups);

    UserGroupCache persistentCache = getPersistentCache();
    if (persistentCache.put(principal, groups)) {
      store.set(persistentCache);
    }

    return groups;
  }

  @Override
  public Set<String> fromLastLoginPlusInternal(String principal) {
    Set<String> cached = new HashSet<>(getPersistentCache().get(principal));
    computeInternalGroups(principal).forEach(cached::add);
    return cached;
  }

  private UserGroupCache getPersistentCache() {
    return store.getOptional().orElseGet(UserGroupCache::new);
  }

  @Subscribe(async = false)
  public void clearCacheOnLogOut(LogoutEvent event) {
    String principal = event.getPrimaryPrincipal();
    cache.remove(principal);
  }

  @Subscribe(async = false)
  public void clearCacheOnUserDeletion(UserEvent event) {
    if (event.getEventType().equals(HandlerEventType.DELETE)) {
      cache.remove(event.getItem().getName());
    }
  }

  private Stream<String> computeInternalGroups(String principal) {
    return groupDAO.getAll().stream().filter(group -> group.isMember(principal)).map(Group::getName);
  }

  private void appendInternalGroups(String principal, ImmutableSet.Builder<String> builder) {
    computeInternalGroups(principal).forEach(builder::add);
  }

  private Set<String> resolveExternalGroups(String principal) {
    Set<String> externalGroups = cache.get(principal);

    if (externalGroups == null) {
      ImmutableSet.Builder<String> newExternalGroups = ImmutableSet.builder();

      for (GroupResolver groupResolver : groupResolvers) {
        newExternalGroups.addAll(groupResolver.resolve(principal));
      }
      externalGroups = newExternalGroups.build();
      cache.put(principal, externalGroups);
    }
    return externalGroups;
  }
}

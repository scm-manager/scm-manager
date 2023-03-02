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

package sonia.scm.group;

import com.cronutils.utils.VisibleForTesting;
import com.github.legman.Subscribe;
import com.google.common.collect.ImmutableSet;
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

import javax.inject.Inject;
import javax.inject.Singleton;
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

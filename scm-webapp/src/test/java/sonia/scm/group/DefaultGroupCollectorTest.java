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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.cache.MapCache;
import sonia.scm.cache.MapCacheManager;
import sonia.scm.security.LogoutEvent;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.security.Authentications.PRINCIPAL_ANONYMOUS;
import static sonia.scm.security.Authentications.PRINCIPAL_SYSTEM;

@ExtendWith(MockitoExtension.class)
class DefaultGroupCollectorTest {

  @Mock
  private GroupDAO groupDAO;

  @Mock
  private GroupResolver groupResolver;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStoreFactory configurationStoreFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStore<UserGroupCache> configurationStore;

  private MapCacheManager mapCacheManager;

  private Set<GroupResolver> groupResolvers;

  private DefaultGroupCollector collector;

  private UserGroupCache userGroupCache;

  @BeforeEach
  void initCollector() {
    groupResolvers = new HashSet<>();
    mapCacheManager = new MapCacheManager();
    when(configurationStoreFactory.withType(UserGroupCache.class).withName("user-group-cache").build())
      .thenReturn(configurationStore);
    lenient().when(configurationStore.getOptional()).thenAnswer(invocation -> Optional.ofNullable(userGroupCache));
    lenient().doAnswer(invocation -> {
      userGroupCache = invocation.getArgument(0, UserGroupCache.class);
      return null;
    }).when(configurationStore).set(any());
    collector = new DefaultGroupCollector(groupDAO, mapCacheManager, groupResolvers, configurationStoreFactory);
  }

  @Test
  void shouldAlwaysReturnAuthenticatedGroup() {
    Iterable<String> groupNames = collector.collect("trillian");
    assertThat(groupNames).containsOnly("_authenticated");
  }

  @Test
  void shouldReturnGroupsFromCache() {
    MapCache<String, Set<String>> cache = mapCacheManager.getCache(DefaultGroupCollector.CACHE_NAME);
    cache.put("trillian", ImmutableSet.of("awesome", "incredible"));

    Set<String> groups = collector.collect("trillian");
    assertThat(groups).containsOnly("_authenticated", "awesome", "incredible");
  }

  @Test
  void shouldNotCallResolverIfExternalGroupsAreCached() {
    groupResolvers.add(groupResolver);

    MapCache<String, Set<String>> cache = mapCacheManager.getCache(DefaultGroupCollector.CACHE_NAME);
    cache.put("trillian", ImmutableSet.of("awesome", "incredible"));

    Set<String> groups = collector.collect("trillian");
    assertThat(groups).containsOnly("_authenticated", "awesome", "incredible");

    verify(groupResolver, never()).resolve("trillian");
  }

  @Test
  void shouldClearCacheOnLogout() {
    MapCache<String, Set<String>> cache = mapCacheManager.getCache(DefaultGroupCollector.CACHE_NAME);
    cache.put("trillian", ImmutableSet.of("awesome", "incredible"));

    collector.clearCacheOnLogOut(new LogoutEvent("trillian"));

    assertThat(cache.get("trillian")).isNull();
  }


  @Test
  void shouldClearCacheOnUserDeletion() {
    MapCache<String, Set<String>> cache = mapCacheManager.getCache(DefaultGroupCollector.CACHE_NAME);
    cache.put("trillian", ImmutableSet.of("awesome", "incredible"));

    collector.clearCacheOnUserDeletion(new UserEvent(HandlerEventType.DELETE, new User("trillian")));

    assertThat(cache.get("trillian")).isNull();
  }

  @Test
  void shouldNotCallResolverForAnonymous() {
    groupResolvers.add(groupResolver);
    collector.collect(PRINCIPAL_ANONYMOUS);
    verify(groupResolver, never()).resolve(PRINCIPAL_ANONYMOUS);
  }

  @Test
  void shouldNotCallResolverForSystemAccount() {
    groupResolvers.add(groupResolver);
    collector.collect(PRINCIPAL_SYSTEM);
    verify(groupResolver, never()).resolve(PRINCIPAL_SYSTEM);
  }

  @Test
  void shouldNotResolveInternalGroupsForSystemAccount() {
    collector.collect(PRINCIPAL_SYSTEM);
    verify(groupDAO, never()).getAll();
  }

  @Test
  void shouldGetCachedGroupsFromLastLogin() {
    UserGroupCache cache = new UserGroupCache();
    cache.put("trillian", Set.of("hog"));
    when(configurationStore.getOptional()).thenReturn(Optional.of(cache));

    Set<String> cachedGroups = collector.fromLastLoginPlusInternal("trillian");

    assertThat(cachedGroups).contains("hog");
  }

  @Test
  void shouldCacheGroups() {
    collector.collect("trillian");

    Set<String> groups = userGroupCache.get("trillian");
    assertThat(groups).contains("_authenticated");
  }

  @Test
  void shouldNotPersistUnchangedGroups() {
    collector.collect("trillian");
    collector.collect("trillian");

    verify(configurationStore, times(1)).set(any());
  }

  @Nested
  class WithGroupsFromDao {

    @BeforeEach
    void setUpGroupsDao() {
      List<Group> groups = Lists.newArrayList(
        new Group("xml", "heartOfGold", "trillian"),
        new Group("xml", "g42", "dent", "prefect"),
        new Group("xml", "fjordsOfAfrican", "dent", "trillian")
      );
      when(groupDAO.getAll()).thenReturn(groups);
    }

    @Test
    void shouldReturnGroupsFromDao() {
      Iterable<String> groupNames = collector.collect("trillian");
      assertThat(groupNames).containsOnly("_authenticated", "heartOfGold", "fjordsOfAfrican");
    }

    @Test
    void shouldCombineWithResolvers() {
      when(groupResolver.resolve("trillian")).thenReturn(ImmutableSet.of("awesome", "incredible"));
      groupResolvers.add(groupResolver);
      Iterable<String> groupNames = collector.collect("trillian");
      assertThat(groupNames).containsOnly("_authenticated", "heartOfGold", "fjordsOfAfrican", "awesome", "incredible");
    }

    @Test
    void shouldGetScmGroupsForLastLoginWhenNothingCached() {
      Set<String> cachedGroups = collector.fromLastLoginPlusInternal("trillian");

      assertThat(cachedGroups).contains("heartOfGold", "fjordsOfAfrican");
    }

    @Test
    void shouldGetCachedGroupsFromLastLoginWithInternalGroups() {
      UserGroupCache cache = new UserGroupCache();
      cache.put("trillian", Set.of("earth"));
      when(configurationStore.getOptional()).thenReturn(Optional.of(cache));

      Set<String> cachedGroups = collector.fromLastLoginPlusInternal("trillian");

      assertThat(cachedGroups).contains("earth", "heartOfGold", "fjordsOfAfrican");
    }
  }
}

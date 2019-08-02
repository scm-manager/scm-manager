package sonia.scm.group;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.MapCache;
import sonia.scm.cache.MapCacheManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultGroupCollectorTest {

  @Mock
  private GroupDAO groupDAO;

  @Mock
  private GroupResolver groupResolver;

  private MapCacheManager mapCacheManager;

  private Set<GroupResolver> groupResolvers;

  private DefaultGroupCollector collector;

  @BeforeEach
  void initCollector() {
    groupResolvers = new HashSet<>();
    mapCacheManager = new MapCacheManager();
    collector = new DefaultGroupCollector(groupDAO, mapCacheManager, groupResolvers);
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
  }
}

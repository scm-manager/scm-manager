package sonia.scm.group;

import com.cronutils.utils.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

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

  @Inject
  public DefaultGroupCollector(GroupDAO groupDAO, CacheManager cacheManager, Set<GroupResolver> groupResolvers) {
    this.groupDAO = groupDAO;
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.groupResolvers = groupResolvers;
  }

  @Override
  public Set<String> collect(String principal) {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    if (principal != "_anonymous") {
      builder.add(AUTHENTICATED);
    }

    builder.addAll(resolveExternalGroups(principal));
    appendInternalGroups(principal, builder);

    Set<String> groups = builder.build();
    LOG.debug("collected following groups for principal {}: {}", principal, groups);
    return groups;
  }

  private void appendInternalGroups(String principal, ImmutableSet.Builder<String> builder) {
    for (Group group : groupDAO.getAll()) {
      if (group.isMember(principal)) {
        builder.add(group.getName());
      }
    }
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

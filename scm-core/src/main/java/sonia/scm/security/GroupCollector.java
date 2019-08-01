package sonia.scm.security;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupNames;

import java.util.Set;

/**
 * Collect groups for a certain principal.
 * <strong>Warning</strong>: The class is only for internal use and should never used directly.
 */
class GroupCollector {

  private static final Logger LOG = LoggerFactory.getLogger(GroupCollector.class);

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.externalGroups";

  /** Field description */
  private final Cache<String, Set> cache;
  private Set<GroupResolver> groupResolvers;

  private final GroupDAO groupDAO;

  GroupCollector(GroupDAO groupDAO, CacheManager cacheManager, Set<GroupResolver> groupResolvers) {
    this.groupDAO = groupDAO;
    this.cache  = cacheManager.getCache(CACHE_NAME);
    this.groupResolvers = groupResolvers;
  }

  Iterable<String> collect(String principal) {

    Set<String> externalGroups = cache.get(principal);

    if (externalGroups == null) {
      ImmutableSet.Builder<String> newExternalGroups = ImmutableSet.builder();

      for (GroupResolver groupResolver : groupResolvers) {
       Iterable<String> groups = groupResolver.resolveGroups(principal);
       groups.forEach(newExternalGroups::add);
      }

      cache.put(principal, newExternalGroups.build());
    }

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    builder.add(GroupNames.AUTHENTICATED);

    for (String group : externalGroups) {
      builder.add(group);
    }

    for (Group group : groupDAO.getAll()) {
      if (group.isMember(principal)) {
        builder.add(group.getName());
      }
    }

    GroupNames groups = new GroupNames(builder.build());
    LOG.debug("collected following groups for principal {}: {}", principal, groups);
    return groups;
  }
}

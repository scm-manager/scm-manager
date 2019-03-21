package sonia.scm.security;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupNames;

/**
 * Collect groups for a certain principal.
 * <strong>Warning</strong>: The class is only for internal use and should never used directly.
 */
class GroupCollector {

  private static final Logger LOG = LoggerFactory.getLogger(GroupCollector.class);

  private final GroupDAO groupDAO;

  GroupCollector(GroupDAO groupDAO) {
    this.groupDAO = groupDAO;
  }

  GroupNames collect(String principal, Iterable<String> groupNames) {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    builder.add(GroupNames.AUTHENTICATED);

    for (String group : groupNames) {
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

package sonia.scm.group;

import java.util.Collection;

/**
 * This class represents all associated groups which are provided by external systems for a certain user.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public class ExternalGroupNames extends GroupNames {
  public ExternalGroupNames() {
  }

  public ExternalGroupNames(String groupName, String... groupNames) {
    super(groupName, groupNames);
  }

  public ExternalGroupNames(Collection<String> collection) {
    super(collection);
  }
}

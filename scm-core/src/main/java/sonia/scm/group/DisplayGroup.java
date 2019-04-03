package sonia.scm.group;

import sonia.scm.ReducedModelObject;

public class DisplayGroup implements ReducedModelObject {

  private final String id;
  private final String displayName;

  public static DisplayGroup from(Group group) {
    return new DisplayGroup(group.getId(), group.getDescription());
  }

  private DisplayGroup(String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }
}

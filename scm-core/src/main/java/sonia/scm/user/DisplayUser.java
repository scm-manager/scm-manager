package sonia.scm.user;

import sonia.scm.ReducedModelObject;

public class DisplayUser implements ReducedModelObject {

  private final String id;
  private final String displayName;
  private final String mail;

  public static DisplayUser from(User user) {
    return new DisplayUser(user.getId(), user.getDisplayName(), user.getMail());
  }

  private DisplayUser(String id, String displayName, String mail) {
    this.id = id;
    this.displayName = displayName;
    this.mail = mail;
  }

  public String getId() {
    return id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getMail() {
    return mail;
  }
}

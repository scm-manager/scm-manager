package sonia.scm.update.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "permissions")
class V1Permission {
  private boolean groupPermission;
  private String name;
  private String type;

  public boolean isGroupPermission() {
    return groupPermission;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }
}

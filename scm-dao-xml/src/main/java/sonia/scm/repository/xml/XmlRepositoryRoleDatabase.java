package sonia.scm.repository.xml;

import sonia.scm.repository.RepositoryRole;
import sonia.scm.xml.XmlDatabase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement(name = "user-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryRoleDatabase implements XmlDatabase<RepositoryRole> {

  private Long creationTime;
  private Long lastModified;

  @XmlJavaTypeAdapter(XmlRepositoryRoleMapAdapter.class)
  @XmlElement(name = "roles")
  private Map<String, RepositoryRole> roleMap = new LinkedHashMap<String, RepositoryRole>();

  public XmlRepositoryRoleDatabase() {
    long c = System.currentTimeMillis();

    creationTime = c;
    lastModified = c;
  }

  @Override
  public void add(RepositoryRole role) {
    roleMap.put(role.getName(), role);
  }

  @Override
  public boolean contains(String name) {
    return roleMap.containsKey(name);
  }

  @Override
  public RepositoryRole remove(String name) {
    return roleMap.remove(name);
  }

  @Override
  public Collection<RepositoryRole> values() {
    return roleMap.values();
  }

  @Override
  public RepositoryRole get(String name) {
    return roleMap.get(name);
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  @Override
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }
}

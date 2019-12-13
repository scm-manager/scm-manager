package sonia.scm.update.repository;

import sonia.scm.update.V1Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "repositories")
public class V1Repository {
  private String contact;
  private long creationDate;
  private Long lastModified;
  private String description;
  private String id;
  private String name;
  @XmlElement(name="public")
  private boolean isPublic;
  private boolean archived;
  private String type;
  private List<V1Permission> permissions;
  private V1Properties properties;

  public V1Repository() {
  }

  public V1Repository(String id, String type, String name) {
    this.id = id;
    this.type = type;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getContact() {
    return contact;
  }

  public long getCreationDate() {
    return creationDate;
  }

  public Long getLastModified() {
    return lastModified;
  }

  public String getDescription() {
    return description;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public boolean isArchived() {
    return archived;
  }

  public List<V1Permission> getPermissions() {
    return permissions;
  }

  public V1Properties getProperties() {
    return properties;
  }

  @Override
  public String toString() {
    return "V1Repository{" +
      ", contact='" + contact + '\'' +
      ", creationDate=" + creationDate +
      ", lastModified=" + lastModified +
      ", description='" + description + '\'' +
      ", id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", isPublic=" + isPublic +
      ", archived=" + archived +
      ", type='" + type + '\'' +
      '}';
  }
}

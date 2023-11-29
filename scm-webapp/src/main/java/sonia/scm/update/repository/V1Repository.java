/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.update.repository;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.update.V1Properties;

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

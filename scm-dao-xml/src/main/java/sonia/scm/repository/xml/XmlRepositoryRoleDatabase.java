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

package sonia.scm.repository.xml;

import sonia.scm.repository.RepositoryRole;
import sonia.scm.xml.XmlDatabase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

@XmlRootElement(name = "user-db")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryRoleDatabase implements XmlDatabase<RepositoryRole> {

  private Long creationTime;
  private Long lastModified;

  @XmlJavaTypeAdapter(XmlRepositoryRoleMapAdapter.class)
  @XmlElement(name = "roles")
  private Map<String, RepositoryRole> roleMap = new TreeMap<>();

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

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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlRepositoryRoleList implements Iterable<RepositoryRole> {

  public XmlRepositoryRoleList() {}

  public XmlRepositoryRoleList(Map<String, RepositoryRole> roleMap) {
    this.roles = new LinkedList<RepositoryRole>(roleMap.values());
  }

  @Override
  public Iterator<RepositoryRole> iterator()
  {
    return roles.iterator();
  }

  public LinkedList<RepositoryRole> getRoles()
  {
    return roles;
  }

  public void setRoles(LinkedList<RepositoryRole> roles)
  {
    this.roles = roles;
  }

  @XmlElement(name = "role")
  private LinkedList<RepositoryRole> roles;
}

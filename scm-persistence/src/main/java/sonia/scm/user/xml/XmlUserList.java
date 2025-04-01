/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.user.xml;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import sonia.scm.user.User;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUserList implements Iterable<User>
{
  @XmlElement(name = "user")
  private LinkedList<User> users;

  public XmlUserList() {}


  public XmlUserList(Map<String, User> userMap)
  {
    this.users = new LinkedList<>(userMap.values());
  }



  @Override
  public Iterator<User> iterator()
  {
    return users.iterator();
  }



  public LinkedList<User> getUsers()
  {
    return users;
  }



  public void setUsers(LinkedList<User> users)
  {
    this.users = users;
  }

}

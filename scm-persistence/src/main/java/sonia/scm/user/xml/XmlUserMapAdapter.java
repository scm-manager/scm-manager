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

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import sonia.scm.user.User;

import java.util.Map;
import java.util.TreeMap;


public class XmlUserMapAdapter
        extends XmlAdapter<XmlUserList, Map<String, User>>
{


  @Override
  public XmlUserList marshal(Map<String, User> userMap) throws Exception
  {
    return new XmlUserList(userMap);
  }


  @Override
  public Map<String, User> unmarshal(XmlUserList users) throws Exception
  {
    Map<String, User> userMap = new TreeMap<>();

    for (User user : users)
    {
      userMap.put(user.getName(), user);
    }

    return userMap;
  }
}

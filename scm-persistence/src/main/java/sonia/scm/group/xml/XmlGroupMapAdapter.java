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

package sonia.scm.group.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import sonia.scm.group.Group;

import java.util.Map;
import java.util.TreeMap;


public class XmlGroupMapAdapter
        extends XmlAdapter<XmlGroupList, Map<String, Group>>
{


  @Override
  public XmlGroupList marshal(Map<String, Group> groupMap) throws Exception
  {
    return new XmlGroupList(groupMap);
  }


  @Override
  public Map<String, Group> unmarshal(XmlGroupList groups) throws Exception
  {
    Map<String, Group> groupMap = new TreeMap<>();

    for (Group group : groups)
    {
      groupMap.put(group.getName(), group);
    }

    return groupMap;
  }
}

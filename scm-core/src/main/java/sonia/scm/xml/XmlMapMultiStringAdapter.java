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

package sonia.scm.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import sonia.scm.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XmlMapMultiStringAdapter
        extends XmlAdapter<XmlMapMultiStringElement[], Map<String, Set<String>>> {

  @Override
  public XmlMapMultiStringElement[] marshal(Map<String, Set<String>> map) throws Exception {
    XmlMapMultiStringElement[] elements;

    if (Util.isNotEmpty(map)) {
      int i = 0;
      int s = map.size();

      elements = new XmlMapMultiStringElement[s];

      for (Map.Entry<String, Set<String>> e : map.entrySet()) {
        elements[i] = new XmlMapMultiStringElement(e.getKey(), e.getValue());
        i++;
      }
    } else {
      elements = new XmlMapMultiStringElement[0];
    }

    return elements;
  }

  @Override
  public Map<String, Set<String>> unmarshal(XmlMapMultiStringElement[] elements)
          throws Exception
  {
    Map<String, Set<String>> map = new HashMap<>();

    if (elements != null)
    {
      for (XmlMapMultiStringElement e : elements)
      {
        map.put(e.getKey(), e.getValue());
      }
    }

    return map;
  }
}

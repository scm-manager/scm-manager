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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class XmlSetStringAdapter extends XmlAdapter<String, Set<String>>
{

 
  @Override
  public String marshal(Set<String> value) throws Exception
  {
    StringBuilder buffer = new StringBuilder();
    Iterator<String> it = value.iterator();

    while (it.hasNext())
    {
      buffer.append(it.next());

      if (it.hasNext())
      {
        buffer.append(",");
      }
    }

    return buffer.toString();
  }

  @Override
  public Set<String> unmarshal(String rawString) throws Exception
  {
    Set<String> tokens = new HashSet<>();

    for (String token : rawString.split(","))
    {
      token = token.trim();

      if (token.length() > 0)
      {
        tokens.add(token);
      }
    }

    return tokens;
  }
}

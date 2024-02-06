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
    
package sonia.scm.xml;


import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import sonia.scm.util.Util;

import java.util.HashMap;
import java.util.Map;


public class XmlMapStringAdapter
        extends XmlAdapter<XmlMapStringElement[], Map<String, String>>
{

  @Override
  public XmlMapStringElement[] marshal(Map<String, String> map) throws Exception
  {
    XmlMapStringElement[] elements = null;

    if (Util.isNotEmpty(map))
    {
      int i = 0;
      int s = map.size();

      elements = new XmlMapStringElement[s];

      for (Map.Entry<String, String> e : map.entrySet())
      {
        elements[i] = new XmlMapStringElement(e.getKey(), e.getValue());
        i++;
      }
    }
    else
    {
      elements = new XmlMapStringElement[0];
    }

    return elements;
  }

  @Override
  public Map<String, String> unmarshal(XmlMapStringElement[] elements)
          throws Exception
  {
    Map<String, String> map = new HashMap<>();

    if (elements != null)
    {
      for (XmlMapStringElement e : elements)
      {
        map.put(e.getKey(), e.getValue());
      }
    }

    return map;
  }
}

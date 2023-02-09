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

import sonia.scm.util.Util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
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

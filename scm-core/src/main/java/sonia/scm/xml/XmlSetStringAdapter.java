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

//~--- JDK imports ------------------------------------------------------------

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
public class XmlSetStringAdapter extends XmlAdapter<String, Set<String>>
{

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   *
   * @throws Exception
   */
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

  /**
   * Method description
   *
   *
   * @param rawString
   *
   * @return
   *
   * @throws Exception
   */
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

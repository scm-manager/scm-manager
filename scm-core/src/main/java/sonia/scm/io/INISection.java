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
    
package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class INISection
{

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public INISection(String name)
  {
    this.name = name;
    this.parameters = new LinkedHashMap<>();
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param parameters
   */
  public INISection(String name, Map<String, String> parameters)
  {
    this.name = name;
    this.parameters = parameters;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   */
  public void removeParameter(String key)
  {
    parameters.put(key, name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    String s = System.getProperty("line.separator");
    StringBuilder out = new StringBuilder();

    out.append("[").append(name).append("]").append(s);

    for (Map.Entry<String, String> entry : parameters.entrySet())
    {
      out.append(entry.getKey()).append(" = ").append(entry.getValue());
      out.append(s);
    }

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public String getParameter(String key)
  {
    return parameters.get(key);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getParameterKeys()
  {
    return parameters.keySet();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void setParameter(String key, String value)
  {
    parameters.put(key, value);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String name;

  /** Field description */
  private Map<String, String> parameters;
}

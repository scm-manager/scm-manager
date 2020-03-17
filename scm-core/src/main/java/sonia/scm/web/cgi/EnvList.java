/**
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


package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableSet;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class EnvList
{

  /** Field description */
  private static final ImmutableSet<String> SENSITIVE =
    ImmutableSet.of("HTTP_AUTHORIZATION", "SCM_CHALLENGE", "SCM_CREDENTIALS");

  //~--- constructors ---------------------------------------------------------

  /**
   *    Constructs ...
   *
   */
  public EnvList()
  {
    envMap = new HashMap<>();
  }

  /**
   * Constructs ...
   *
   *
   * @param l
   */
  public EnvList(EnvList l)
  {
    envMap = new HashMap<>(l.envMap);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns environment as mutable map.
   *
   * @return environment as mutable map
   * @since 1.31
   */
  public Map<String, String> asMutableMap()
  {
    return new MapDelegate(envMap);
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public boolean containsKey(String key)
  {
    return envMap.containsKey(key);
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
    StringBuilder out = new StringBuilder("Environment:");

    Iterator<String> it = envMap.values().iterator();

    String v;

    while (it.hasNext())
    {
      v = converSensitive(it.next());
      out.append(s).append("  ").append(v);
    }

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Get representation suitable for passing to exec.
   *
   * @return
   */
  public String[] getEnvArray()
  {
    return envMap.values().toArray(new String[envMap.size()]);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Set a name/value pair, null values will be treated as an empty String
   *
   * @param name
   * @param value
   */
  public void set(String name, String value)
  {
    envMap.put(name, name.concat("=").concat(Util.nonNull(value)));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private String converSensitive(String v)
  {
    String result = v;

    for (String s : SENSITIVE)
    {
      if (v.startsWith(s))
      {
        result = s.concat("=(is set)");

        break;
      }
    }

    return result;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 13/05/15
   * @author         Enter your name here...
   */
  private static class MapDelegate extends ForwardingMap<String, String>
  {

    /**
     * Constructs ...
     *
     *
     * @param delegate
     */
    private MapDelegate(Map<String, String> delegate)
    {
      this.delegate = delegate;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param key
     * @param value
     *
     * @return
     */
    @Override
    public String put(String key, String value)
    {
      return super.put(key, key.concat("=").concat(Strings.nullToEmpty(value)));
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    protected Map<String, String> delegate()
    {
      return delegate;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Map<String, String> delegate;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, String> envMap;
}

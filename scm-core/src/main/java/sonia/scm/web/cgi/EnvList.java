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

package sonia.scm.web.cgi;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class EnvList {

  private static final ImmutableSet<String> SENSITIVE =
    ImmutableSet.of("HTTP_AUTHORIZATION", "SCM_CHALLENGE", "SCM_CREDENTIALS", "SCM_BEARER_TOKEN");

  private final Map<String, String> envMap;

  public EnvList() {
    envMap = new HashMap<>();
  }

  public EnvList(EnvList list) {
    envMap = new HashMap<>(list.envMap);
  }


  /**
   * Set a name/value pair, null values will be treated as an empty String
   *
   * @param name name of environment variable
   * @param value value of environment variable
   */
  public void set(String name, String value) {
    envMap.put(name, Strings.nullToEmpty(value));
  }

  /**
   * Return {@code true} if the list contains an environment variable with the given key.
   *
   * @param key name of environment variable
   */
  public boolean containsKey(String key)
  {
    return envMap.containsKey(key);
  }

  /**
   * Representation suitable for passing to exec.
   *
   * @return array of environment variables
   * @since 2.12.0
   */
  public String[] asArray() {
    return envMap.entrySet()
      .stream()
      .map(e -> e.getKey() + "=" + e.getValue())
      .toArray(String[]::new);
  }

  /**
   * Representation suitable for passing to process builder.
   *
   * @return environment as immutable map
   * @since 2.12.0
   */
  public Map<String, String> asMap() {
    return Collections.unmodifiableMap(envMap);
  }

  @Override
  public String toString() {
    String s = System.getProperty("line.separator");
    StringBuilder out = new StringBuilder("Environment:");
    for (Map.Entry<String, String> e : envMap.entrySet()) {
      out
        .append(s).append("  ")
        .append(e.getKey()).append("=").append(convertSensitive(e.getKey(), e.getValue()));
    }
    return out.toString();
  }

  private String convertSensitive(String name, String value) {
    if (SENSITIVE.contains(name)) {
      return "(is set)";
    }
    return value;
  }

  /**
   * Get representation suitable for passing to exec.
   *
   * @return array of environment variables
   * @deprecated use {@link #asArray()} instead
   */
  @Deprecated
  public String[] getEnvArray() {
    return asArray();
  }

  /**
   * Returns environment as mutable map.
   *
   * @return environment as mutable map
   * @since 1.31
   *
   * @deprecated the environment should only be modified by {@link #set(String, String)}.
   *             Of a {@link Map} is required, a immutable {@link Map} can be created with {@link #asMap()}.
   */
  @Deprecated
  public Map<String, String> asMutableMap() {
    return envMap;
  }
}

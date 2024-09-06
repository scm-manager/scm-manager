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

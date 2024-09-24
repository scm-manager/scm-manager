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

package sonia.scm.io;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A section of {@link INIConfiguration}.
 * The section consists of keys and values.
 *
 */
public class INISection {

  private final String name;
  private final Map<String, String> parameters;

  /**
   * Constructs a new empty section with the given name.
   * @param name name of the section
   */
  public INISection(String name) {
    this.name = name;
    this.parameters = new LinkedHashMap<>();
  }

  /**
   * Constructs a new section with the given name and parameters.
   *
   * @param name name of the section
   * @param initialParameters initial parameter
   */
  public INISection(String name, Map<String, String> initialParameters) {
    this.name = name;
    this.parameters = new LinkedHashMap<>(initialParameters);
  }

  /**
   * Returns the name of the section.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the value of the parameter with the given key or {@code null} if the given parameter does not exist.
   */
  public String getParameter(String key) {
    return parameters.get(key);
  }

  /**
   * Returns all parameter keys of the section.
   */
  public Collection<String> getParameterKeys() {
    return ImmutableList.copyOf(parameters.keySet());
  }

  /**
   * Sets the parameter with the given key to the given value.
   */
  public void setParameter(String key, String value) {
    parameters.put(key, value);
  }

  /**
   * Remove parameter with the given name from the section.
   */
  public void removeParameter(String key) {
    parameters.remove(key);
  }

  @Override
  public String toString() {
    String s = System.getProperty("line.separator");
    StringBuilder out = new StringBuilder();

    out.append("[").append(name).append("]").append(s);

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      out.append(entry.getKey()).append(" = ").append(entry.getValue());
      out.append(s);
    }

    return out.toString();
  }

}

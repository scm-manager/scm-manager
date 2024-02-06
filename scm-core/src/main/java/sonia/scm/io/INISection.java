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

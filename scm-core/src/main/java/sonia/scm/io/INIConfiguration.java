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
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration in the ini format.
 * The format consists of sections, keys and values.
 *
 * @see <a href="https://en.wikipedia.org/wiki/INI_file">Wikipedia article</a>
 */
public class INIConfiguration {

  private final Map<String, INISection> sectionMap = new LinkedHashMap<>();

  /**
   * Add a new section to the configuration.
   */
  public void addSection(INISection section) {
    sectionMap.put(section.getName(), section);
  }

  /**
   * Remove an existing section from the configuration.
   */
  public void removeSection(String name) {
    sectionMap.remove(name);
  }

  /**
   * Returns a section by its name or {@code null} if the section does not exists.
   * @param name name of the section
   * @return section or null
   */
  @Nullable
  public INISection getSection(String name) {
    return sectionMap.get(name);
  }

  /**
   * Returns all sections of the configuration.
   */
  public Collection<INISection> getSections() {
    return ImmutableList.copyOf(sectionMap.values());
  }

}

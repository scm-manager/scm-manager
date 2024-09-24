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

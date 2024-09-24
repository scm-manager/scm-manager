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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class INIConfigurationTest {

  @Test
  void shouldAddAndGet() {
    INIConfiguration configuration = new INIConfiguration();

    INISection section = new INISection("one");
    configuration.addSection(section);

    assertThat(configuration.getSection("one")).isSameAs(section);
  }

  @Test
  void shouldRemoveExistingSection() {
    INIConfiguration configuration = new INIConfiguration();

    INISection section = new INISection("one");
    configuration.addSection(section);
    configuration.removeSection("one");

    assertThat(configuration.getSection("one")).isNull();
  }

  @Test
  void shouldAllowRemoveDuringIteration() {
    INIConfiguration configuration = new INIConfiguration();
    configuration.addSection(new INISection("one"));
    configuration.addSection(new INISection("two"));
    configuration.addSection(new INISection("three"));

    for (INISection section : configuration.getSections()) {
      if (section.getName().startsWith("t")) {
        configuration.removeSection(section.getName());
      }
    }

    assertThat(configuration.getSections()).hasSize(1);
  }

}

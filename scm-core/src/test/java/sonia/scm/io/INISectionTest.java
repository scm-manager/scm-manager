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

class INISectionTest {

  @Test
  void shouldReturnName() {
    assertThat(new INISection("test").getName()).isEqualTo("test");
  }

  @Test
  void shouldSetAndGet() {
    INISection section = new INISection("section");
    section.setParameter("one", "1");

    assertThat(section.getParameter("one")).isEqualTo("1");
  }

  @Test
  void shouldOverwriteExistingKey() {
    INISection section = new INISection("section");
    section.setParameter("one", "1");
    section.setParameter("one", "2");

    assertThat(section.getParameter("one")).isEqualTo("2");
  }

  @Test
  void shouldReturnNullForNonExistingKeys() {
    INISection section = new INISection("section");

    assertThat(section.getParameter("one")).isNull();
  }

  @Test
  void shouldRemoveExistingKey() {
    INISection section = new INISection("section");
    section.setParameter("one", "1");
    section.removeParameter("one");

    assertThat(section.getParameter("one")).isNull();
  }

  @Test
  void shouldReturnSectionInIniFormat() {
    INISection section = new INISection("section");
    section.setParameter("one", "1");
    section.setParameter("two", "2");

    String expected = String.join(System.getProperty("line.separator"),
      "[section]",
      "one = 1",
      "two = 2",
      ""
    );
    assertThat(section).hasToString(expected);
  }

  @Test
  void shouldAllowRemoveDuringIteration() {
    INISection section = new INISection("section");
    section.setParameter("one", "one");
    section.setParameter("two", "two");
    section.setParameter("three", "three");

    for (String key : section.getParameterKeys()) {
      if (key.startsWith("t")) {
        section.removeParameter(key);
      }
    }

    assertThat(section.getParameterKeys()).hasSize(1);
  }

}

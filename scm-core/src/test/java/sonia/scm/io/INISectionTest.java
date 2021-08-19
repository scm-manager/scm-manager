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

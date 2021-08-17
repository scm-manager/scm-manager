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

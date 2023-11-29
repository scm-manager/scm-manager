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

package sonia.scm.plugin;

import com.google.common.io.Resources;
import jakarta.xml.bind.JAXB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class InstalledPluginDescriptorTest {

  private static InstalledPluginDescriptor descriptor;

  @BeforeAll
  @SuppressWarnings("UnstableApiUsage")
  static void unmarshal() {
    URL resource = Resources.getResource("sonia/scm/plugin/review-plugin.xml");
    descriptor = JAXB.unmarshal(resource, InstalledPluginDescriptor.class);
  }

  @Test
  void shouldUnmarshallDependencies() {
    assertThat(descriptor.getDependencies()).containsOnly("scm-mail-plugin");
    assertThat(descriptor.getOptionalDependencies()).containsOnly("scm-editor-plugin", "scm-landingpage-plugin");
    assertThat(descriptor.getDependenciesInclusiveOptionals()).containsOnly("scm-mail-plugin", "scm-editor-plugin", "scm-landingpage-plugin");
  }

  @Test
  void shouldUnmarshallDependenciesWithVersion() {
    assertThat(descriptor.getDependenciesWithVersion()).containsOnly(new NameAndVersion("scm-mail-plugin", "2.1.0"));
    assertThat(descriptor.getOptionalDependenciesWithVersion()).containsOnly(
      new NameAndVersion("scm-landingpage-plugin", "1.0.0"),
      new NameAndVersion("scm-editor-plugin")
    );
  }

}

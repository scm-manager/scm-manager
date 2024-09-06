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

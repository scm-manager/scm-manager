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

package sonia.scm.importexport;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvironmentInformationXmlGeneratorTest {

  @Mock
  SCMContextProvider contextProvider;

  @Mock
  PluginManager pluginManager;

  @InjectMocks
  EnvironmentInformationXmlGenerator generator;

  @Test
  void shouldGenerateXmlContent() {
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class, Answers.RETURNS_DEEP_STUBS);
    when(descriptor.getInformation().getName()).thenReturn("scm-exporter-test-plugin");
    when(descriptor.getInformation().getVersion()).thenReturn("42.0");
    when(contextProvider.getVersion()).thenReturn("2.13.0");
    InstalledPlugin installedPlugin = new InstalledPlugin(descriptor, null, null, null, false);
    when(pluginManager.getInstalled()).thenReturn(ImmutableList.of(installedPlugin));

    byte[] content = generator.generate();

    String xmlContent = new String(content);
    assertThat(xmlContent).contains(
      "<scm-environment>",
      "    <plugins>\n" +
        "        <plugin>\n" +
        "            <name>scm-exporter-test-plugin</name>\n" +
        "            <version>42.0</version>\n" +
        "        </plugin>\n" +
        "    </plugins>",
      "<coreVersion>2.13.0</coreVersion>",
      "<arch>",
      "<os>");
  }

}

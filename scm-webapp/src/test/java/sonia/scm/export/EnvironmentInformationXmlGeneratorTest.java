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

package sonia.scm.export;

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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;

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
  void shouldGenerateXmlContent() throws TransformerException, ParserConfigurationException {
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class, Answers.RETURNS_DEEP_STUBS);
    when(descriptor.getInformation().getName()).thenReturn("scm-exporter-test-plugin");
    when(descriptor.getInformation().getVersion()).thenReturn("42.0");
    when(contextProvider.getVersion()).thenReturn("2.13.0");
    InstalledPlugin installedPlugin = new InstalledPlugin(descriptor, null, null, null, false);
    when(pluginManager.getInstalled()).thenReturn(ImmutableList.of(installedPlugin));

    ByteArrayOutputStream outputStream = generator.generate();

    String xmlContent = outputStream.toString();
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

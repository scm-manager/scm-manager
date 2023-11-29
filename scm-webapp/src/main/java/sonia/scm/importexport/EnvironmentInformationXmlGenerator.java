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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXB;
import sonia.scm.ContextEntry;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.util.SystemUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class EnvironmentInformationXmlGenerator {

  private final PluginManager pluginManager;
  private final SCMContextProvider contextProvider;

  @Inject
  public EnvironmentInformationXmlGenerator(PluginManager pluginManager, SCMContextProvider contextProvider) {
    this.pluginManager = pluginManager;
    this.contextProvider = contextProvider;
  }

  public byte[] generate() {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ScmEnvironment scmEnvironment = new ScmEnvironment();
      writeCoreInformation(scmEnvironment);
      writePluginInformation(scmEnvironment);
      JAXB.marshal(scmEnvironment, baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new ExportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        "Could not generate SCM-Manager environment description.",
        e
      );
    }
  }

  private void writeCoreInformation(ScmEnvironment scmEnvironment) {
    scmEnvironment.setCoreVersion(contextProvider.getVersion());
    scmEnvironment.setArch(SystemUtil.getArch());
    scmEnvironment.setOs(SystemUtil.getOS());
  }

  private void writePluginInformation(ScmEnvironment scmEnvironment) {
    List<EnvironmentPluginDescriptor> plugins = new ArrayList<>();
    for (InstalledPlugin plugin : pluginManager.getInstalled()) {
      PluginInformation pluginInformation = plugin.getDescriptor().getInformation();
      plugins.add(new EnvironmentPluginDescriptor(pluginInformation.getName(), pluginInformation.getVersion()));
    }
    scmEnvironment.setPlugins(new EnvironmentPluginsDescriptor(plugins));
  }
}

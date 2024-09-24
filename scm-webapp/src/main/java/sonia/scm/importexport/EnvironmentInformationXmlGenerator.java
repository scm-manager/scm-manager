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

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

package sonia.scm.update.plugin;

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginSetConfigStore;
import sonia.scm.plugin.PluginSetsConfig;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.version.Version;

import java.util.Collections;

@Extension
public class PluginSetsConfigInitializationUpdateStep implements UpdateStep {
  private final PluginSetConfigStore pluginSetConfigStore;
  private final XmlUserDAO userDAO;

  @Inject
  public PluginSetsConfigInitializationUpdateStep(PluginSetConfigStore pluginSetConfigStore, XmlUserDAO userDAO) {
    this.pluginSetConfigStore = pluginSetConfigStore;
    this.userDAO = userDAO;
  }

  @Override
  public void doUpdate() throws Exception {
    if (!userDAO.getAll().isEmpty() && pluginSetConfigStore.getPluginSets().isEmpty()) {
        pluginSetConfigStore.setPluginSets(new PluginSetsConfig(Collections.emptySet()));
    }
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.PluginSetsConfig";
  }
}

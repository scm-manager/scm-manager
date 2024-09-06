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

package sonia.scm.lifecycle;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.config.WebappConfigProvider;
import sonia.scm.initialization.InitializationStep;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.PluginSetConfigStore;

@Extension
@Singleton
public class PluginWizardStartupAction implements InitializationStep {

  private final PluginSetConfigStore store;

  @Inject
  public PluginWizardStartupAction(PluginSetConfigStore pluginSetConfigStore) {
    this.store = pluginSetConfigStore;
  }

  @Override
  public String name() {
    return "pluginWizard";
  }

  @Override
  public int sequence() {
    return 1;
  }

  @Override
  public boolean done() {
    return WebappConfigProvider.resolveAsString("initialPassword").isPresent() || store.getPluginSets().isPresent();
  }

}

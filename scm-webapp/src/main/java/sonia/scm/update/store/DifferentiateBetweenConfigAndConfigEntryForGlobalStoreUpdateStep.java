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

package sonia.scm.update.store;

import jakarta.inject.Inject;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;

import java.io.File;
import java.nio.file.Path;

@Extension
public class DifferentiateBetweenConfigAndConfigEntryForGlobalStoreUpdateStep extends DifferentiateBetweenConfigAndConfigEntryUpdateStep implements UpdateStep {

  private final SCMContextProvider contextProvider;

  @Inject
  public DifferentiateBetweenConfigAndConfigEntryForGlobalStoreUpdateStep(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public void doUpdate() throws Exception {
    Path configPath = new File(contextProvider.getBaseDirectory(), "config").toPath();
    updateAllInDirectory(configPath);
  }
}

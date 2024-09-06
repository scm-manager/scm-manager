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

package sonia.scm.update;

import com.google.inject.Injector;
import com.google.inject.Module;
import sonia.scm.lifecycle.modules.ModuleProvider;
import sonia.scm.update.repository.XmlRepositoryV1UpdateStep;

import java.util.Collection;
import java.util.Collections;

public class MigrationWizardModuleProvider implements ModuleProvider {

  private final Injector bootstrapInjector;

  public MigrationWizardModuleProvider(Injector bootstrapInjector) {
    this.bootstrapInjector = bootstrapInjector;
  }

  public boolean wizardNecessary() {
    return !bootstrapInjector.getInstance(XmlRepositoryV1UpdateStep.class).getRepositoriesWithoutMigrationStrategies().isEmpty();
  }

  @Override
  public Collection<Module> createModules() {
    return Collections.singleton(new MigrationWizardModule());
  }
}

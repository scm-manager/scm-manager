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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.IncompatibleEnvironmentForImportException;
import sonia.scm.web.security.AdministrationContext;

import java.io.InputStream;

import static sonia.scm.importexport.FullScmRepositoryExporter.SCM_ENVIRONMENT_FILE_NAME;

class EnvironmentCheckStep implements ImportStep {

  @SuppressWarnings("java:S115") // we like this name here
  private static final int _1_MB = 1024*1024;

  private final ScmEnvironmentCompatibilityChecker compatibilityChecker;
  private final AdministrationContext administrationContext;

  @Inject
  EnvironmentCheckStep(ScmEnvironmentCompatibilityChecker compatibilityChecker, AdministrationContext administrationContext) {
    this.compatibilityChecker = compatibilityChecker;
    this.administrationContext = administrationContext;
  }

  @Override
  public boolean handle(TarArchiveEntry environmentEntry, ImportState state, InputStream inputStream) {
    if (environmentEntry.getName().equals(SCM_ENVIRONMENT_FILE_NAME) && !environmentEntry.isDirectory()) {
      if (environmentEntry.getSize() > _1_MB) {
        throw new ImportFailedException(
          ContextEntry.ContextBuilder.entity(state.getRepository()).build(),
          "Invalid import format. SCM-Manager environment description file 'scm-environment.xml' too big."
        );
      }
      administrationContext.runAsAdmin(() -> {
        boolean validEnvironment = compatibilityChecker.check(JAXB.unmarshal(new NoneClosingInputStream(inputStream), ScmEnvironment.class));
        if (!validEnvironment) {
          throw new IncompatibleEnvironmentForImportException();
        }
      });
      state.getLogger().step("checked environment");
      state.environmentChecked();
      return true;
    }
    return false;
  }

  @Override
  public void finish(ImportState state) {
    if (!state.isEnvironmentChecked()) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(state.getRepository()).build(),
        "Invalid import format. Missing SCM-Manager environment description file 'scm-environment.xml'."
      );
    }
  }
}

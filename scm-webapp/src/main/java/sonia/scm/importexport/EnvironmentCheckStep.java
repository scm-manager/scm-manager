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

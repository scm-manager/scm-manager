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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static sonia.scm.importexport.FullScmRepositoryExporter.METADATA_FILE_NAME;

class MetadataImportStep implements ImportStep {

  private static final Logger LOG = LoggerFactory.getLogger(MetadataImportStep.class);

  private final RepositoryManager repositoryManager;

  @Inject
  MetadataImportStep(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @Override
  public boolean handle(TarArchiveEntry metadataEntry, ImportState state, InputStream inputStream) {
    if (metadataEntry.getName().equals(METADATA_FILE_NAME)) {
      LOG.trace("Importing metadata from tar");
      RepositoryMetadataXmlGenerator.RepositoryMetadata metadata = JAXB.unmarshal(new NoneClosingInputStream(inputStream), RepositoryMetadataXmlGenerator.RepositoryMetadata.class);
      if (metadata != null && metadata.getPermissions() != null) {
        state.setPermissions(new HashSet<>(metadata.getPermissions()));
        state.getLogger().step("reading repository metadata with permissions");
      } else {
        state.setPermissions(Collections.emptySet());
      }
      return true;
    }
    return false;
  }

  @Override
  public void finish(ImportState state) {
    LOG.trace("Saving permissions for imported repository");
    state.getLogger().step("setting permissions for repository from import");
    importRepositoryPermissions(state.getRepository(), state.getRepositoryPermissions());
  }

  private void importRepositoryPermissions(Repository repository, Collection<RepositoryPermission> importedPermissions) {
    Collection<RepositoryPermission> existingPermissions = repository.getPermissions();
    RepositoryImportPermissionMerger permissionMerger = new RepositoryImportPermissionMerger();
    Collection<RepositoryPermission> permissions = permissionMerger.merge(existingPermissions, importedPermissions);
    repository.setPermissions(permissions);
    repositoryManager.modify(repository);
  }
}

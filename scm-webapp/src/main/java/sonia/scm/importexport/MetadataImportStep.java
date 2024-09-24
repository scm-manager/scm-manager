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

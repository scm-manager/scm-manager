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
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

public class ExportFileExtensionResolver {

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ExportFileExtensionResolver(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  public String resolve(Repository repository, boolean withMetadata, boolean compressed, boolean encrypted) {
    StringBuilder builder = new StringBuilder();
    if (withMetadata) {
      builder.append("tar.gz");
    } else {
      try (RepositoryService service = serviceFactory.create(repository)) {
        builder.append(service.getBundleCommand().getFileExtension());
      }
      if (compressed) {
        builder.append(".gz");
      }
    }
    if (encrypted) {
      builder.append(".enc");
    }
    return builder.toString();
  }
}

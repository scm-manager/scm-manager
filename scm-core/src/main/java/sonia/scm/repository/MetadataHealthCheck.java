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

package sonia.scm.repository;

import jakarta.inject.Inject;
import sonia.scm.plugin.Extension;

import java.nio.file.Files;
import java.nio.file.Path;

@Extension
public final class MetadataHealthCheck implements HealthCheck {

  public static final HealthCheckFailure REPOSITORY_Directory_NOT_WRITABLE =
    new HealthCheckFailure("9cSV1eaVF1",
      "repository directory not writable",
      "The system user has no permissions to create or delete files in the repository directory.");
  public static final HealthCheckFailure METADATA_NOT_WRITABLE =
    new HealthCheckFailure("6bSUg4dZ41",
      "metadata file not writable",
      "The system user has no permissions to modify the metadata file for the repository.");
  private final RepositoryLocationResolver locationResolver;

  @Inject
  public MetadataHealthCheck(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public HealthCheckResult check(Repository repository) {
    Path repositoryLocation = locationResolver.forClass(Path.class).getLocation(repository.getId());
    if (!Files.isWritable(repositoryLocation)) {
      return HealthCheckResult.unhealthy(REPOSITORY_Directory_NOT_WRITABLE);
    }
    Path metadata = repositoryLocation.resolve("metadata.xml");
    if (!Files.isWritable(metadata)) {
      return HealthCheckResult.unhealthy(METADATA_NOT_WRITABLE);
    }
    return HealthCheckResult.healthy();
  }
}

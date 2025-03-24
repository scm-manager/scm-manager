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

package sonia.scm.repository.update;

import jakarta.inject.Inject;
import sonia.scm.migration.UpdateException;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitConfigHelper;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;
import sonia.scm.version.Version;

import java.io.IOException;
import java.nio.file.Path;

import static sonia.scm.repository.update.GitUpdateStepHelper.build;
import static sonia.scm.repository.update.GitUpdateStepHelper.determineEffectiveGitFolder;
import static sonia.scm.repository.update.GitUpdateStepHelper.isGitDirectory;
import static sonia.scm.version.Version.parse;

@Extension
public class GitV2UpdateStep implements UpdateStep {

  private final RepositoryLocationResolver locationResolver;
  private final UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess;

  @Inject
  public GitV2UpdateStep(RepositoryLocationResolver locationResolver, UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess) {
    this.locationResolver = locationResolver;
    this.repositoryMetadataAccess = repositoryMetadataAccess;
  }

  @Override
  public void doUpdate() {
    locationResolver.forClass(Path.class).forAllLocations(
      (repositoryId, path) -> {
        Repository repository = repositoryMetadataAccess.read(path);
        if (isGitDirectory(repository)) {
          final Path effectiveGitPath = determineEffectiveGitFolder(path);
          try (org.eclipse.jgit.lib.Repository gitRepository = build(effectiveGitPath.toFile())) {
            new GitConfigHelper().createScmmConfig(repository, gitRepository);
          } catch (IOException e) {
            throw new UpdateException("could not update repository with id " + repositoryId + " in path " + path, e);
          }
        }
      }
    );
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.git";
  }
}

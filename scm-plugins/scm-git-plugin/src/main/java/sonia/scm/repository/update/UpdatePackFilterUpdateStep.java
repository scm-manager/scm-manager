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
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitConfigHelper;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;
import sonia.scm.version.Version;

import java.io.File;
import java.nio.file.Path;

import static sonia.scm.repository.update.GitUpdateStepHelper.build;
import static sonia.scm.repository.update.GitUpdateStepHelper.determineEffectiveGitFolder;
import static sonia.scm.repository.update.GitUpdateStepHelper.isGitDirectory;
import static sonia.scm.version.Version.parse;

@Extension
public class UpdatePackFilterUpdateStep implements RepositoryUpdateStep {

  private final RepositoryLocationResolver locationResolver;
  private final UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess;

  @Inject
  public UpdatePackFilterUpdateStep(RepositoryLocationResolver locationResolver, UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess) {
    this.locationResolver = locationResolver;
    this.repositoryMetadataAccess = repositoryMetadataAccess;
  }

  @Override
  public void doUpdate(RepositoryUpdateContext repositoryUpdateContext) throws Exception {
    Path scmmRepositoryLocation = locationResolver.forClass(Path.class).getLocation(repositoryUpdateContext.getRepositoryId());
    Repository scmmRepository = repositoryMetadataAccess.read(scmmRepositoryLocation);

    if (isGitDirectory(scmmRepository)) {
      File gitFile = determineEffectiveGitFolder(scmmRepositoryLocation).toFile();
      org.eclipse.jgit.lib.Repository gitRepository = build(gitFile);
      GitConfigHelper gitConfigHelper = new GitConfigHelper();
      gitConfigHelper.createScmmConfig(scmmRepository, gitRepository);
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("3.7.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin.git";
  }

}

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

import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;

public class GitConfigHelper {

  private static final String CONFIG_SECTION_SCMM = "scmm";
  private static final String CONFIG_KEY_REPOSITORY_ID = "repositoryid";

  public void createScmmConfig(Repository repository, org.eclipse.jgit.lib.Repository gitRepository) throws IOException {
    StoredConfig config = gitRepository.getConfig();
    config.setString(CONFIG_SECTION_SCMM, null, CONFIG_KEY_REPOSITORY_ID, repository.getId());
    config.save();
  }

  public String getRepositoryId(StoredConfig gitConfig) {
    return gitConfig.getString(CONFIG_SECTION_SCMM, null, CONFIG_KEY_REPOSITORY_ID);
  }
}

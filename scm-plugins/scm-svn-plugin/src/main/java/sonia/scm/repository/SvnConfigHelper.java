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

import sonia.scm.ContextEntry;
import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationReader;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;

import java.io.File;
import java.io.IOException;

class SvnConfigHelper {

  private static final String CONFIG_FILE_NAME = "scm-manager.conf";
  private static final String CONFIG_SECTION_SCMM = "scmm";
  private static final String CONFIG_KEY_REPOSITORY_ID = "repositoryid";

  void writeRepositoryId(Repository repository, File directory) throws IOException {
    INISection iniSection = new INISection(CONFIG_SECTION_SCMM);
    iniSection.setParameter(CONFIG_KEY_REPOSITORY_ID, repository.getId());
    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.addSection(iniSection);
    new INIConfigurationWriter().write(iniConfiguration, new File(directory, CONFIG_FILE_NAME));
  }

  String getRepositoryId(File directory) {
    try {
      return new INIConfigurationReader().read(new File(directory, CONFIG_FILE_NAME)).getSection(CONFIG_SECTION_SCMM).getParameter(CONFIG_KEY_REPOSITORY_ID);
    } catch (IOException e) {
      throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity("Directory", directory.toString()), "could not read scm configuration file", e);
    }
  }
}

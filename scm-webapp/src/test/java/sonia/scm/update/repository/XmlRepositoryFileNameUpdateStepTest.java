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

package sonia.scm.update.repository;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.repository.xml.XmlRepositoryDAO;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XmlRepositoryFileNameUpdateStepTest {

  SCMContextProvider contextProvider = mock(SCMContextProvider.class);
  XmlRepositoryDAO repositoryDAO = mock(XmlRepositoryDAO.class);

  @TempDir
  Path tempDir;

  @BeforeEach
  void mockScmHome() {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
  }

  @Test
  void shouldCopyRepositoriesFileToRepositoryPathsFile() throws IOException {
    XmlRepositoryFileNameUpdateStep updateStep = new XmlRepositoryFileNameUpdateStep(contextProvider, repositoryDAO);
    URL url = Resources.getResource("sonia/scm/update/repository/formerV2RepositoryFile.xml");
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    Files.copy(url.openStream(), configDir.resolve("repositories.xml"));

    updateStep.doUpdate();

    assertThat(configDir.resolve(PathBasedRepositoryLocationResolver.STORE_NAME + ".xml")).exists();
    assertThat(configDir.resolve("repositories.xml")).doesNotExist();
    verify(repositoryDAO).refresh();
  }
}

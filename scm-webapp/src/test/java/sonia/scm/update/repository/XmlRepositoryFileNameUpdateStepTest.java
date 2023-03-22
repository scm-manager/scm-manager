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

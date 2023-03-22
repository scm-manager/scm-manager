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

package sonia.scm.update.security;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.store.InMemoryConfigurationEntryStoreFactory.create;

@ExtendWith(MockitoExtension.class)
class XmlSecurityV1UpdateStepTest {

  @Mock
  SCMContextProvider contextProvider;

  XmlSecurityV1UpdateStep updateStep;
  ConfigurationEntryStore<AssignedPermission> assignedPermissionStore;

  @TempDir
  Path tempDir;

  @BeforeEach
  void mockScmHome() {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    InMemoryConfigurationEntryStoreFactory inMemoryConfigurationEntryStoreFactory = create();
    assignedPermissionStore = inMemoryConfigurationEntryStoreFactory.get("security");
    updateStep = new XmlSecurityV1UpdateStep(contextProvider, inMemoryConfigurationEntryStoreFactory);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void createConfigV1XML() throws IOException {
      Path configDir = tempDir.resolve("config");
      Files.createDirectories(configDir);
      copyTestDatabaseFile(configDir, "config.xml");
    }

    @Test
    void shouldCreatePermissionForUsersConfiguredAsAdmin() throws JAXBException {
      updateStep.doUpdate();
      List<String> assignedPermission =
        assignedPermissionStore.getAll().values()
          .stream()
          .filter(a -> a.getPermission().getValue().equals("*"))
          .filter(a -> !a.isGroupPermission())
          .map(AssignedPermission::getName)
          .collect(toList());
      assertThat(assignedPermission).contains("arthur", "dent", "ldap-admin");
    }

    @Test
    void shouldCreatePermissionForGroupsConfiguredAsAdmin() throws JAXBException {
      updateStep.doUpdate();
      List<String> assignedPermission =
        assignedPermissionStore.getAll().values()
          .stream()
          .filter(a -> a.getPermission().getValue().equals("*"))
          .filter(AssignedPermission::isGroupPermission)
          .map(AssignedPermission::getName)
          .collect(toList());
      assertThat(assignedPermission).contains("admins", "vogons");
    }

  }

  @Nested
  class WithExistingSecurityXml {

    private Path configDir;

    @BeforeEach
    void createSecurityV1XML() throws IOException {
      configDir = tempDir.resolve("config");
      Files.createDirectories(configDir);
    }

    @Test
    void shouldMapV1PermissionsFromSecurityV1XML() throws IOException, JAXBException {
      copyTestDatabaseFile(configDir, "securityV1.xml");
      updateStep.doUpdate();
      List<String> assignedPermission =
        assignedPermissionStore.getAll().values()
          .stream()
          .filter(a -> a.getPermission().getValue().contains("repository:"))
          .map(AssignedPermission::getName)
          .collect(toList());
      assertThat(assignedPermission).contains("scmadmin");
      assertThat(assignedPermission).contains("test");
    }

    @Test
    void shouldNotFailOnEmptyV1SecurityXml() throws IOException, JAXBException {
      copyTestDatabaseFile(configDir, "emptySecurityV1.xml", "securityV1.xml");
      updateStep.doUpdate();
      assertThat(assignedPermissionStore.getAll()).isEmpty();
    }

  }

  private void copyTestDatabaseFile(Path configDir, String fileName) throws IOException {
    copyTestDatabaseFile(configDir, fileName, fileName);
  }

  private void copyTestDatabaseFile(Path configDir, String sourceFileName, String targetFileName) throws IOException {
    URL url = Resources.getResource("sonia/scm/update/security/" + sourceFileName);
    Files.copy(url.openStream(), configDir.resolve(targetFileName));
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
    assertThat(assignedPermissionStore.getAll()).isEmpty();
  }
}

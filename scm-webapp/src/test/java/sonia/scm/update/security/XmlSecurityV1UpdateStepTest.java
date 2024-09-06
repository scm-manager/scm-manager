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

package sonia.scm.update.security;

import com.google.common.io.Resources;
import jakarta.xml.bind.JAXBException;
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
import sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory.create;

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
    InMemoryByteConfigurationEntryStoreFactory inMemoryConfigurationEntryStoreFactory = create();
    assignedPermissionStore = inMemoryConfigurationEntryStoreFactory.get(AssignedPermission.class, "security");
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
      assertThat(assignedPermission)
        .contains("scmadmin")
        .contains("test");
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

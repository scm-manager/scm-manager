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

package sonia.scm.update.user;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory;
import sonia.scm.update.UpdateStepTestUtil;
import sonia.scm.update.V1Properties;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static sonia.scm.store.InMemoryByteConfigurationEntryStoreFactory.create;

@ExtendWith(MockitoExtension.class)
class XmlUserV1UpdateStepTest {

  @Mock
  XmlUserDAO userDAO;

  @Captor
  ArgumentCaptor<User> userCaptor;

  InMemoryByteConfigurationEntryStoreFactory storeFactory = create();

  XmlUserV1UpdateStep updateStep;

  private UpdateStepTestUtil testUtil;

  @BeforeEach
  void mockScmHome(@TempDir Path tempDir) {
    testUtil = new UpdateStepTestUtil(tempDir);
    updateStep = new XmlUserV1UpdateStep(testUtil.getContextProvider(), userDAO, storeFactory);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(userDAO).add(userCaptor.capture());
    }

    @BeforeEach
    void createUserV1XML() throws IOException {
      testUtil.copyConfigFile("sonia/scm/update/user/users.xml");
    }

    @Test
    void shouldCreateNewPermissionsForV1AdminUser() throws JAXBException {
      updateStep.doUpdate();
      Optional<AssignedPermission> assignedPermission =
        storeFactory.<AssignedPermission>get(AssignedPermission.class, "security")
          .getAll()
          .values()
          .stream()
          .filter(a -> a.getName().equals("scmadmin"))
          .findFirst();
      assertThat(assignedPermission.get().getPermission().getValue()).contains("*");
      assertThat(assignedPermission.get().isGroupPermission()).isFalse();
    }

    @Test
    void shouldCreateNewUserFromUsersV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(userDAO, times(5)).add(any());
    }

    @Test
    void shouldMapAttributesFromUsersV1Xml() throws JAXBException {
      updateStep.doUpdate();
      Optional<User> user = userCaptor.getAllValues().stream().filter(u -> u.getName().equals("scmadmin")).findFirst();
      assertThat(user)
        .get()
        .hasFieldOrPropertyWithValue("name", "scmadmin")
        .hasFieldOrPropertyWithValue("mail", "scm-admin@scm-manager.com")
        .hasFieldOrPropertyWithValue("displayName", "SCM Administrator")
        .hasFieldOrPropertyWithValue("active", false)
        .hasFieldOrPropertyWithValue("password", "ff8f5c593a01f9fcd3ed48b09a4b013e8d8f3be7")
        .hasFieldOrPropertyWithValue("type", "xml")
        .hasFieldOrPropertyWithValue("lastModified", 1558597367492L)
        .hasFieldOrPropertyWithValue("creationDate", 1558597074732L);
    }

    @Test
    void shouldExtractProperties() throws JAXBException {
      updateStep.doUpdate();
      ConfigurationEntryStore<V1Properties> propertiesStore = storeFactory.<V1Properties>get(V1Properties.class, "user-properties-v1");
      V1Properties properties = propertiesStore.get("dent");
      assertThat(properties).isNotNull();
      assertThat(properties.get("born.on")).isEqualTo("earth");
      assertThat(properties.get("last.seen")).isEqualTo("end of the universe");
    }
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
  }
}

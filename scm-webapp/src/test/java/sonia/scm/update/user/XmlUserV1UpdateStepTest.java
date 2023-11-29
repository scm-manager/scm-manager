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

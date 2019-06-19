package sonia.scm.update.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.update.UpdateStepTestUtil;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.linesOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlUserV1UpdateStepTest {

  @Mock
  XmlUserDAO userDAO;

  @Captor
  ArgumentCaptor<User> userCaptor;

  XmlUserV1UpdateStep updateStep;

  private UpdateStepTestUtil testUtil;

  @BeforeEach
  void mockScmHome(@TempDirectory.TempDir Path tempDir) {
    testUtil = new UpdateStepTestUtil(tempDir);
    JAXBConfigurationEntryStoreFactory storeFactory = new JAXBConfigurationEntryStoreFactory(testUtil.getContextProvider(), null, new DefaultKeyGenerator());
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
        getStoreForConfigFile("security")
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
      Path propertiesFile = testUtil.getFile("user-properties-v1.xml");
      assertThat(propertiesFile)
        .exists();
      assertThat(linesOf(propertiesFile.toFile()))
        .extracting(String::trim)
        .containsSequence(
          "<key>dent</key>",
          "<value>",
          "<item>",
          "<key>born.on</key>",
          "<value>earth</value>",
          "</item>",
          "<item>",
          "<key>last.seen</key>",
          "<value>end of the universe</value>",
          "</item>",
          "</value>");
    }

    private ConfigurationEntryStore<AssignedPermission> getStoreForConfigFile(String name) {
      return new JAXBConfigurationEntryStoreFactory(testUtil.getContextProvider(), null, new DefaultKeyGenerator())
        .withType(AssignedPermission.class)
        .withName(name)
        .build();
    }
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
  }
}

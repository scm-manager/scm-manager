package sonia.scm.user.update;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlUserV1UpdateStepTest {

  @Mock
  SCMContextProvider contextProvider;
  @Mock
  XmlUserDAO userDAO;

  @Captor
  ArgumentCaptor<User> userCaptor;

  XmlUserV1UpdateStep updateStep;
  ConfigurationEntryStore<AssignedPermission> assignedPermissionStore;

  @BeforeEach
  void mockScmHome(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    assignedPermissionStore = new InMemoryConfigurationEntryStore<>();
    ConfigurationEntryStoreFactory inMemoryConfigurationEntryStoreFactory = new InMemoryConfigurationEntryStoreFactory(assignedPermissionStore);
    updateStep = new XmlUserV1UpdateStep(contextProvider,userDAO, inMemoryConfigurationEntryStoreFactory);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(userDAO).add(userCaptor.capture());
    }

    @BeforeEach
    void createUserV1XML(@TempDirectory.TempDir Path tempDir) throws IOException {
      URL url = Resources.getResource("sonia/scm/user/update/users.xml");
      Path configDir = tempDir.resolve("config");
      Files.createDirectories(configDir);
      Files.copy(url.openStream(), configDir.resolve("users.xml"));
    }

    @Test
    void shouldCreateNewPermissionsForV1AdminUser() throws JAXBException {
      updateStep.doUpdate();
      Optional<AssignedPermission> assignedPermission = assignedPermissionStore.getAll().values().stream().filter(a -> a.getName().equals("scmadmin")).findFirst();
      assertThat(assignedPermission.get().getPermission().getValue()).contains("*");
      assertThat(assignedPermission.get().isGroupPermission()).isFalse();
    }

    @Test
    void shouldCreateNewUserFromUsersV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(userDAO, times(3)).add(any());
    }

    @Test
    void shouldMapAttributesFromUsersV1Xml() throws JAXBException {
      updateStep.doUpdate();
      Optional<User> user = userCaptor.getAllValues().stream().filter(u -> u.getName().equals("scmadmin")).findFirst();
      assertThat(user)
        .get()
        .hasFieldOrPropertyWithValue("name","scmadmin")
        .hasFieldOrPropertyWithValue("mail", "scm-admin@scm-manager.com")
        .hasFieldOrPropertyWithValue("displayName", "SCM Administrator")
        .hasFieldOrPropertyWithValue("active", false)
        .hasFieldOrPropertyWithValue("password", "ff8f5c593a01f9fcd3ed48b09a4b013e8d8f3be7")
        .hasFieldOrPropertyWithValue("type", "xml");
    }
  }
}

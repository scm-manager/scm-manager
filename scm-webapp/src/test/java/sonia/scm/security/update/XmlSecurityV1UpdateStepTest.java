package sonia.scm.security.update;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlSecurityV1UpdateStepTest {

  @Mock
  SCMContextProvider contextProvider;

  XmlSecurityV1UpdateStep updateStep;
  ConfigurationEntryStore<AssignedPermission> assignedPermissionStore;

  @BeforeEach
  void mockScmHome(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    assignedPermissionStore = new InMemoryConfigurationEntryStore<>();
    ConfigurationEntryStoreFactory inMemoryConfigurationEntryStoreFactory = new InMemoryConfigurationEntryStoreFactory(assignedPermissionStore);
    updateStep = new XmlSecurityV1UpdateStep(contextProvider, inMemoryConfigurationEntryStoreFactory);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void createConfigV1XML(@TempDirectory.TempDir Path tempDir) throws IOException {
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

  private void copyTestDatabaseFile(Path configDir, String fileName) throws IOException {
    URL url = Resources.getResource("sonia/scm/security/update/" + fileName);
    Files.copy(url.openStream(), configDir.resolve(fileName));
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
  }
}

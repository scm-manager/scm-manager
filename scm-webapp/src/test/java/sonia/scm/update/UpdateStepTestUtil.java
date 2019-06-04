package sonia.scm.update;

import com.google.common.io.Resources;
import org.mockito.Mockito;
import sonia.scm.SCMContextProvider;
import sonia.scm.security.AssignedPermission;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

public class UpdateStepTestUtil {

private final SCMContextProvider contextProvider;

  private final Path tempDir;
  private final ConfigurationEntryStoreFactory storeFactory;

  public UpdateStepTestUtil(Path tempDir) {
    this.tempDir = tempDir;
    contextProvider = Mockito.mock(SCMContextProvider.class);
    storeFactory = new JAXBConfigurationEntryStoreFactory(contextProvider, null, new DefaultKeyGenerator());
    lenient().when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    lenient().when(contextProvider.resolve(any())).thenAnswer(invocation -> tempDir.resolve(invocation.getArgument(0).toString()));
  }

  public SCMContextProvider getContextProvider() {
    return contextProvider;
  }

  public ConfigurationEntryStoreFactory getStoreFactory() {
    return storeFactory;
  }

  public void copyConfigFile(String fileName) throws IOException {
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    copyTestDatabaseFile(configDir, fileName);
  }

  public ConfigurationEntryStore<AssignedPermission> getStoreForConfigFile(String name) {
    return storeFactory
      .withType(AssignedPermission.class)
      .withName(name)
      .build();
  }

  public Path getFile(String name) {
    return tempDir.resolve("config").resolve(name);
  }

  private void copyTestDatabaseFile(Path configDir, String fileName) throws IOException {
    URL url = Resources.getResource(fileName);
    Files.copy(url.openStream(), configDir.resolve(Paths.get(fileName).getFileName()));
  }
}

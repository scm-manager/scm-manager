package sonia.scm.update;

import com.google.common.io.Resources;
import org.mockito.Mockito;
import sonia.scm.SCMContextProvider;

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

  public UpdateStepTestUtil(Path tempDir) {
    this.tempDir = tempDir;
    contextProvider = Mockito.mock(SCMContextProvider.class);
    lenient().when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    lenient().when(contextProvider.resolve(any())).thenAnswer(invocation -> tempDir.resolve(invocation.getArgument(0).toString()));
  }

  public SCMContextProvider getContextProvider() {
    return contextProvider;
  }

  public void copyConfigFile(String fileName) throws IOException {
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    copyTestDatabaseFile(configDir, fileName);
  }

  public void copyConfigFile(String fileName, String targetFileName) throws IOException {
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    copyTestDatabaseFile(configDir, fileName, targetFileName);
  }

  public Path getFile(String name) {
    return tempDir.resolve("config").resolve(name);
  }

  private void copyTestDatabaseFile(Path configDir, String fileName) throws IOException {
    Path targetFileName = Paths.get(fileName).getFileName();
    copyTestDatabaseFile(configDir, fileName, targetFileName.toString());
  }

  private void copyTestDatabaseFile(Path configDir, String fileName, String targetFileName) throws IOException {
    URL url = Resources.getResource(fileName);
    Files.copy(url.openStream(), configDir.resolve(targetFileName));
  }
}

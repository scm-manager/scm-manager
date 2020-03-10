package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TempDirectory.class)
class ModifyWorkerHelperTest {

  @Test
  void shouldKeepExecutableFlag(@TempDirectory.TempDir Path temp) throws IOException {

    File target = createFile(temp, "executable.sh");
    File newFile = createFile(temp, "other");

    target.setExecutable(true);

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    helper.modify("executable.sh", newFile);

    assertThat(target.canExecute()).isTrue();
  }

  private File createFile(Path temp, String fileName) throws IOException {
    File file = new File(temp.toFile(), fileName);
    FileWriter source = new FileWriter(file);
    source.write("something");
    source.close();
    return file;
  }

  private static class MinimalModifyWorkerHelper implements ModifyWorkerHelper {

    private final Path temp;

    public MinimalModifyWorkerHelper(Path temp) {
      this.temp = temp;
    }

    @Override
    public void doScmDelete(String toBeDeleted) {

    }

    @Override
    public void addFileToScm(String name, Path file) {

    }

    @Override
    public File getWorkDir() {
      return temp.toFile();
    }

    @Override
    public Repository getRepository() {
      return null;
    }

    @Override
    public String getBranch() {
      return null;
    }
  }
}

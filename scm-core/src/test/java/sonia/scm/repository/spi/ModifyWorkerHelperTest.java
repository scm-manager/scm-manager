package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TempDirectory.class)
class ModifyWorkerHelperTest {

  @Test
  void shouldKeepExecutableFlag(@TempDirectory.TempDir Path temp) throws IOException {

    File target = createFile(temp, "executable.sh");
    File newFile = createFile(temp, "other");

    Assumptions.assumeThatThrownBy(() -> {
      Files.setPosixFilePermissions(target.toPath(),
        ImmutableSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE));
      Files.setPosixFilePermissions(newFile.toPath(),
        ImmutableSet.of(OWNER_READ, OWNER_WRITE, GROUP_READ, OTHERS_READ));
    }).doesNotThrowAnyException(); // ignore this test on systems that does not support posix file permissions

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    helper.modify("executable.sh", newFile);

    assertThat(Files.getPosixFilePermissions(target.toPath()))
      .contains(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE);
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

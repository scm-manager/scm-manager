package sonia.scm.repository.spi;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SimpleGitWorkdirFactoryTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void emptyPoolShouldCreateNewWorkdir() throws IOException {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(temporaryFolder.newFolder());
    File masterRepo = createRepositoryDirectory();

    try (WorkingCopy workingCopy = factory.createWorkingCopy(createContext())) {

      assertThat(workingCopy.get().getDirectory())
        .exists()
        .isNotEqualTo(masterRepo)
        .isDirectory();
      assertThat(new File(workingCopy.get().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a\nline for blame");
    }
  }

  @Test
  public void cloneFromPoolShouldBeClosed() throws IOException {
    PoolWithSpy factory = new PoolWithSpy(temporaryFolder.newFolder());

    try (WorkingCopy workingCopy = factory.createWorkingCopy(createContext())) {
      assertThat(workingCopy).isNotNull();
    }
    verify(factory.createdClone).close();
  }

  @Test
  public void cloneFromPoolShouldNotBeReused() throws IOException {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(temporaryFolder.newFolder());

    File firstDirectory;
    try (WorkingCopy workingCopy = factory.createWorkingCopy(createContext())) {
      firstDirectory = workingCopy.get().getDirectory();
    }
    try (WorkingCopy workingCopy = factory.createWorkingCopy(createContext())) {
      File secondDirectory = workingCopy.get().getDirectory();
      assertThat(secondDirectory).isNotEqualTo(firstDirectory);
    }
  }

  @Test
  public void cloneFromPoolShouldBeDeletedOnClose() throws IOException {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(temporaryFolder.newFolder());

    File directory;
    try (WorkingCopy workingCopy = factory.createWorkingCopy(createContext())) {
      directory = workingCopy.get().getWorkTree();
    }
    assertThat(directory).doesNotExist();
  }

  private static class PoolWithSpy extends SimpleGitWorkdirFactory {
    PoolWithSpy(File poolDirectory) {
      super(poolDirectory);
    }

    Repository createdClone;

    @Override
    protected Repository cloneRepository(File bareRepository, File destination) throws GitAPIException {
      createdClone = spy(super.cloneRepository(bareRepository, destination));
      return createdClone;
    }
  }
}

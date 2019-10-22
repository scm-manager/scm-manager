package sonia.scm.repository.spi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tmatesoft.svn.core.SVNException;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSvnWorkDirFactoryTest extends AbstractSvnCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  // keep this so that it will not be garbage collected (Transport keeps this in a week reference)
  private WorkdirProvider workdirProvider;

  @Before
  public void initWorkDirProvider() throws IOException {
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder());
  }

  @Test
  public void shouldCheckoutLatestRevision() throws SVNException, IOException {
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(workdirProvider);

    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      assertThat(new File(workingCopy.getWorkingRepository(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a and b\nline for blame test");
    }
  }

  @Test
  public void cloneFromPoolshouldNotBeReused() {
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(workdirProvider);

    File firstDirectory;
    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      firstDirectory = workingCopy.getDirectory();
    }
    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      File secondDirectory = workingCopy.getDirectory();
      assertThat(secondDirectory).isNotEqualTo(firstDirectory);
    }
  }

  @Test
  public void shouldDeleteCloneOnClose() {
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(workdirProvider);

    File directory;
    File workingRepository;
    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      directory = workingCopy.getDirectory();
      workingRepository = workingCopy.getWorkingRepository();

    }
    assertThat(directory).doesNotExist();
    assertThat(workingRepository).doesNotExist();
  }
}

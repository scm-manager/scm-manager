package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;

import static com.google.inject.util.Providers.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class SimpleGitWorkdirFactoryTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  // keep this so that it will not be garbage collected (Transport keeps this in a week reference)
  private ScmTransportProtocol proto;
  private WorkdirProvider workdirProvider;

  @Before
  public void bindScmProtocol() throws IOException {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    HookEventFacade hookEventFacade = new HookEventFacade(of(mock(RepositoryManager.class)), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    proto = new ScmTransportProtocol(of(hookEventFacade), of(gitRepositoryHandler));
    Transport.register(proto);
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder());
  }

  @Test
  public void emptyPoolShouldCreateNewWorkdir() throws IOException {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(workdirProvider);
    File masterRepo = createRepositoryDirectory();

    try (WorkingCopy<Repository> workingCopy = factory.createWorkingCopy(createContext())) {

      assertThat(workingCopy.getDirectory())
        .exists()
        .isNotEqualTo(masterRepo)
        .isDirectory();
      assertThat(new File(workingCopy.getWorkingRepository().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a\nline for blame");
    }
  }

  @Test
  public void cloneFromPoolShouldNotBeReused() throws IOException {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(workdirProvider);

    File firstDirectory;
    try (WorkingCopy<Repository> workingCopy = factory.createWorkingCopy(createContext())) {
      firstDirectory = workingCopy.getDirectory();
    }
    try (WorkingCopy<Repository> workingCopy = factory.createWorkingCopy(createContext())) {
      File secondDirectory = workingCopy.getDirectory();
      assertThat(secondDirectory).isNotEqualTo(firstDirectory);
    }
  }

  @Test
  public void cloneFromPoolShouldBeDeletedOnClose() throws IOException {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(workdirProvider);

    File directory;
    try (WorkingCopy<Repository> workingCopy = factory.createWorkingCopy(createContext())) {
      directory = workingCopy.getWorkingRepository().getWorkTree();
    }
    assertThat(directory).doesNotExist();
  }
}

package sonia.scm.repository.spi;

import org.eclipse.jgit.api.errors.GitAPIException;
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

  @Before
  public void bindScmProtocol() {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    HookEventFacade hookEventFacade = new HookEventFacade(of(mock(RepositoryManager.class)), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    proto = new ScmTransportProtocol(of(hookEventFacade), of(gitRepositoryHandler));
    Transport.register(proto);
  }

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

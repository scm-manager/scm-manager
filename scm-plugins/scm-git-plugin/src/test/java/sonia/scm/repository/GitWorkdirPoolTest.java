package sonia.scm.repository;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.spi.AbstractGitCommandTestBase;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class GitWorkdirPoolTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void emptyPoolShouldCreateNewWorkdir() throws IOException {
    GitWorkdirPool pool = new GitWorkdirPool(temporaryFolder.newFolder());
    File masterRepo = createRepositoryDirectory();

    CloseableWrapper<Repository> workingCopy = pool.getWorkingCopy(masterRepo);

    assertThat(workingCopy)
      .isNotNull()
      .extracting(w -> w.get().getDirectory())
      .isNotEqualTo(masterRepo);
  }

  @Test
  public void cloneFromPoolShouldBeClosed() throws IOException {
    PoolWithSpy pool = new PoolWithSpy(temporaryFolder.newFolder());
    File masterRepo = createRepositoryDirectory();

    try (CloseableWrapper<Repository> workingCopy = pool.getWorkingCopy(masterRepo)) {
      assertThat(workingCopy).isNotNull();
    }
    verify(pool.createdClone).close();
  }

  private static class PoolWithSpy extends GitWorkdirPool {
    PoolWithSpy(File poolDirectory) {
      super(poolDirectory);
    }

    Git createdClone;
    @Override
    protected Git cloneRepository(File bareRepository, File destination) throws GitAPIException {
      createdClone = spy(super.cloneRepository(bareRepository, destination));
      return createdClone;
    }
  }
}

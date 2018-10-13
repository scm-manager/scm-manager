package sonia.scm.repository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryUtilTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  private AbstractRepositoryHandler<SimpleRepositoryConfig> repositoryHandler;

  private SimpleRepositoryConfig repositoryConfig;

  @Before
  public void setUpMocks() {
    repositoryConfig = new SimpleRepositoryConfig();
    when(repositoryHandler.getConfig()).thenReturn(repositoryConfig);
  }

  @Test
  public void testGetRepositoryName() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    File repository = new File(repositoryTypeRoot, "abc");
    String name = RepositoryUtil.getRepositoryName(repositoryHandler, repository.getPath());
    assertEquals("abc", name);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRepositoryNameWithInvalidPath() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    File repository = new File("/etc/abc");
    String name = RepositoryUtil.getRepositoryName(repositoryHandler, repository.getPath());
    assertEquals("abc", name);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRepositoryNameWithInvalidPathButSameLength() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    File repository = new File(temporaryFolder.newFolder(), "abc");

    String name = RepositoryUtil.getRepositoryName(repositoryHandler, repository.getPath());
    assertEquals("abc", name);
  }

  @Test
  public void testGetRepositoryNameWithSubDirectory() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    File repository = new File(repositoryTypeRoot, "abc/123");
    String name = RepositoryUtil.getRepositoryName(repositoryHandler, repository.getPath());
    assertEquals("abc/123", name);
  }
}

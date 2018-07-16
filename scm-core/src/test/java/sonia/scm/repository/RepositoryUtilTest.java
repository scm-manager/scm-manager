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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryUtilTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  private AbstractRepositoryHandler<SimpleRepositoryConfig> repositoryHandler;

  private SimpleRepositoryConfig repositoryConfig = new SimpleRepositoryConfig();

  @Before
  public void setUpMocks() {
    when(repositoryHandler.getConfig()).thenReturn(repositoryConfig);
  }

  @Test
  public void testGetRepositoryId() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    File repository = new File(repositoryTypeRoot, "abc");
    String id = RepositoryUtil.getRepositoryId(repositoryHandler, repository.getPath());
    assertEquals("abc", id);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRepositoryIdWithInvalidPath() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    File repository = new File("/etc/abc");
    String id = RepositoryUtil.getRepositoryId(repositoryHandler, repository.getPath());
    assertEquals("abc", id);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRepositoryIdWithInvalidPathButSameLength() throws IOException {
    File repositoryTypeRoot = temporaryFolder.newFolder();
    repositoryConfig.setRepositoryDirectory(repositoryTypeRoot);

    System.out.println(repositoryTypeRoot);

    File repository = new File(temporaryFolder.newFolder(), "abc");
    System.out.println(repository);

    String id = RepositoryUtil.getRepositoryId(repositoryHandler, repository.getPath());
    assertEquals("abc", id);
  }

}

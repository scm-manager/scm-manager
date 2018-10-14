package sonia.scm.repository;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitHeadHandlerTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  private GitRepositoryHandler repositoryHandler;

  @InjectMocks
  private GitHeadHandler headHandler;

  @Test
  public void testResolve() throws IOException {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    create(repository, "master");

    String head = headHandler.resolve(repository);
    assertEquals("master", head);
  }

  @Test
  public void testModify() throws IOException {
    Repository repository = RepositoryTestData.createHeartOfGold("git");
    File file = create(repository, "master");

    headHandler.modify(repository, "develop");

    assertEquals("ref: refs/heads/develop", Files.readFirstLine(file, Charsets.UTF_8));
  }

  private File create(Repository repository, String head) throws IOException {
    File directory = temporaryFolder.newFolder();
    File headFile = new File(directory, "HEAD");
    Files.write(String.format("ref: refs/heads/%s\n", head), headFile, Charsets.UTF_8);

    when(repositoryHandler.getDirectory(repository)).thenReturn(directory);

    return headFile;
  }

}

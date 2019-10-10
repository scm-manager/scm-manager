package sonia.scm.repository.spi;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitModifyCommand_LFSTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();
  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @Before
  public void registerFilter() {
    new GitLfsFilterContextListener(contextProvider).contextInitialized(null);
  }

  @After
  public void unregisterFilter() {
    new GitLfsFilterContextListener(contextProvider).contextDestroyed(null);
  }

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String newRef = createCommit("new_lfs.png", "new content", "fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601", outputStream);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }

    assertThat(outputStream.toString()).isEqualTo("new content");
  }

  @Test
  public void shouldCreateSecondCommits() throws IOException, GitAPIException {
    createCommit("new_lfs.png", "new content", "fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601", new ByteArrayOutputStream());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String newRef = createCommit("more_lfs.png", "more content", "2c2316737c9313956dfc0083da3a2a62ce259f66484f3e26440f0d1b02dd4128", outputStream);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }

    assertThat(outputStream.toString()).isEqualTo("more content");
  }

  private String createCommit(String fileName, String content, String hashOfContent, ByteArrayOutputStream outputStream) throws IOException {
    BlobStore blobStore = mock(BlobStore.class);
    Blob blob = mock(Blob.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(any())).thenReturn(blobStore);
    when(blobStore.create(hashOfContent)).thenReturn(blob);
    when(blobStore.get(hashOfContent)).thenReturn(null, blob);
    when(blob.getOutputStream()).thenReturn(outputStream);
    when(blob.getSize()).thenReturn((long) content.length());

    File newFile = Files.write(temporaryFolder.newFile().toPath(), content.getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest(fileName, newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    return command.execute(request);
  }

  private RevCommit getLastCommit(Git git) throws GitAPIException {
    return git.log().setMaxCount(1).call().iterator().next();
  }

  private GitModifyCommand createCommand() {
    return new GitModifyCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()), lfsBlobStoreFactory);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-lfs-test.zip";
  }
}

package sonia.scm.repository.spi;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
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

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    BlobStore blobStore = mock(BlobStore.class);
    Blob blob = mock(Blob.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(any())).thenReturn(blobStore);
    when(blobStore.create("fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601")).thenReturn(blob);
    when(blobStore.get("fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601")).thenReturn(null, blob);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    when(blob.getOutputStream()).thenReturn(outputStream);
    when(blob.getSize()).thenReturn((long) "new content".length());

    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_lfs.png", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    String newRef = command.execute(request);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }

    assertThat(outputStream.toString()).isEqualTo("new content");
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

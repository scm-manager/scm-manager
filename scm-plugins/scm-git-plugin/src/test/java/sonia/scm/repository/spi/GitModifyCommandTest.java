package sonia.scm.repository.spi;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitModifyCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();
  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setBranch("master");
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    String newRef = command.execute(request);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }
  }

  @Test
  public void shouldCreateNewFile() throws IOException, GitAPIException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setBranch("master");
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("new_file")).isTrue();

    assertInTree(assertions);
  }

  private void assertInTree(TreeAssertions assertions) throws IOException, GitAPIException {
    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      try (RevWalk walk = new RevWalk(git.getRepository())) {
        RevCommit commit = walk.parseCommit(lastCommit);
        ObjectId treeId = commit.getTree().getId();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
          assertions.checkAssertions(new CanonicalTreeParser(null, reader, treeId));
        }
      }
    }
  }

  private RevCommit getLastCommit(Git git) throws GitAPIException {
    return git.log().setMaxCount(1).call().iterator().next();
  }

  private GitModifyCommand createCommand() {
    return new GitModifyCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()));
  }

  @FunctionalInterface
  private interface TreeAssertions {
    void checkAssertions(CanonicalTreeParser treeParser) throws CorruptObjectException;
  }
}

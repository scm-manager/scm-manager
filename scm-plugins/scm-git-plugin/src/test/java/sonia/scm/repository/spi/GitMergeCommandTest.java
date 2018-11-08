package sonia.scm.repository.spi;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.user.User;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini")
public class GitMergeCommandTest extends AbstractGitCommandTestBase {

  private static final String REALM = "AdminRealm";

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Test
  public void shouldDetectMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("mergeable");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    assertThat(mergeable).isTrue();
  }

  @Test
  public void shouldDetectNotMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    assertThat(mergeable).isFalse();
  }

  @Test
  public void shouldMergeMergeableBranches() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
    // We expect the merge result of file b.txt here by looking up the sha hash of its content.
    // If the file is missing (aka not merged correctly) this will throw a MissingObjectException:
    byte[] contentOfFileB = repository.open(repository.resolve("9513e9c76e73f3e562fd8e4c909d0607113c77c6")).getBytes();
    assertThat(new String(contentOfFileB)).isEqualTo("b\ncontent from branch\n");
  }

  @Test
  public void shouldNotMergeConflictingBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isFalse();
    assertThat(mergeCommandResult.getFilesWithConflict()).containsExactly("a.txt");
  }

  @Test
  @SubjectAware(username = "admin", password = "secret")
  public void shouldTakeAuthorFromSubjectIfNotSet() throws IOException, GitAPIException {
    shiro.setSubject(
      new Subject.Builder()
        .principals(new SimplePrincipalCollection(new User("dirk", "Dirk Gently", "dirk@holistic.det"), REALM))
        .buildSubject());
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> mergeCommit = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    PersonIdent mergeAuthor = mergeCommit.iterator().next().getAuthorIdent();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
  }

  private GitMergeCommand createCommand() {
    return new GitMergeCommand(createContext(), repository, new SimpleGitWorkdirFactory());
  }
}

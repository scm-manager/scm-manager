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
import sonia.scm.NotFoundException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.user.User;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitMergeCommandTest extends AbstractGitCommandTestBase {

  private static final String REALM = "AdminRealm";

  @Rule
  public ShiroRule shiro = new ShiroRule();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

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
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
    assertThat(message).contains("master", "mergeable");
    // We expect the merge result of file b.txt here by looking up the sha hash of its content.
    // If the file is missing (aka not merged correctly) this will throw a MissingObjectException:
    byte[] contentOfFileB = repository.open(repository.resolve("9513e9c76e73f3e562fd8e4c909d0607113c77c6")).getBytes();
    assertThat(new String(contentOfFileB)).isEqualTo("b\ncontent from branch\n");
  }

  @Test
  public void shouldAllowEmptyMergeCommit() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);
    assertThat(mergeCommandResult.isSuccess()).isTrue();

    MergeCommandResult secondMergeCommandResult = command.merge(request);
    assertThat(secondMergeCommandResult.isSuccess()).isTrue();
  }

  @Test
  public void shouldUseConfiguredCommitMessageTemplate() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setMessageTemplate("simple");

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    String message = mergeCommit.getFullMessage();
    assertThat(message).isEqualTo("simple");
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
  public void shouldTakeAuthorFromSubjectIfNotSet() throws IOException, GitAPIException {
    SimplePrincipalCollection principals = new SimplePrincipalCollection();
    principals.add("admin", REALM);
    principals.add( new User("dirk", "Dirk Gently", "dirk@holistic.det"), REALM);
    shiro.setSubject(
      new Subject.Builder()
        .principals(principals)
        .authenticated(true)
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

  @Test
  public void shouldMergeIntoNotDefaultBranch() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setTargetBranch("mergeable");
    request.setBranchToMerge("master");

    MergeCommandResult mergeCommandResult = command.merge(request);

    Repository repository = createContext().open();
    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("mergeable")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
    assertThat(message).contains("master", "mergeable");
    // We expect the merge result of file b.txt here by looking up the sha hash of its content.
    // If the file is missing (aka not merged correctly) this will throw a MissingObjectException:
    byte[] contentOfFileB = repository.open(repository.resolve("9513e9c76e73f3e562fd8e4c909d0607113c77c6")).getBytes();
    assertThat(new String(contentOfFileB)).isEqualTo("b\ncontent from branch\n");
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingSourceBranchInMerge() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("mergeable");
    request.setBranchToMerge("not_existing");

    command.merge(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingTargetBranchInMerge() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("not_existing");
    request.setBranchToMerge("master");

    command.merge(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingSourceBranchInDryRun() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("mergeable");
    request.setBranchToMerge("not_existing");

    command.dryRun(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingTargetBranchInDryRun() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("not_existing");
    request.setBranchToMerge("master");

    command.dryRun(request);
  }

  private GitMergeCommand createCommand() {
    return new GitMergeCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()));
  }
}

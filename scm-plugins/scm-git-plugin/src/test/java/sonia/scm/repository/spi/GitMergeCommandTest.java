package sonia.scm.repository.spi;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeStrategy;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.List;

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
  public void shouldAllowEmptyMergeCommit() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("empty_merge");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    assertThat(mergeCommit.getParentCount()).isEqualTo(2);
    assertThat(mergeCommit.getParent(0).name()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
    assertThat(mergeCommit.getParent(1).name()).isEqualTo("d81ad6c63d7e2162308d69637b339dedd1d9201c");
  }

  @Test
  public void shouldNotMergeTwice() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    ObjectId firstMergeCommit = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call().iterator().next().getId();

    MergeCommandResult secondMergeCommandResult = command.merge(request);

    assertThat(secondMergeCommandResult.isSuccess()).isTrue();

    ObjectId secondMergeCommit = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call().iterator().next().getId();

    assertThat(secondMergeCommit).isEqualTo(firstMergeCommit);
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

  @Test
  public void shouldSquashCommitsIfSquashIsEnabled() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setBranchToMerge("squash");
    request.setTargetBranch("master");
    request.setMessageTemplate("this is a squash");
    request.setMergeStrategy(MergeStrategy.SQUASH);

    MergeCommandResult mergeCommandResult = command.merge(request);

    Repository repository = createContext().open();
    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(message).isEqualTo("this is a squash");
  }

  @Test
  public void shouldSquashThreeCommitsIntoOne() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setBranchToMerge("squash");
    request.setTargetBranch("master");
    request.setMessageTemplate("squash three commits");
    request.setMergeStrategy(MergeStrategy.SQUASH);
    Repository gitRepository = createContext().open();
    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(gitRepository).log().add(gitRepository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(message).isEqualTo("squash three commits");

    GitModificationsCommand modificationsCommand = new GitModificationsCommand(createContext(), repository);
    List<String> changes = modificationsCommand.getModifications("master").getAdded();
    assertThat(changes.size()).isEqualTo(3);
  }


  @Test
  public void shouldMergeWithFastForward() throws IOException, GitAPIException {
    Repository repository = createContext().open();

    ObjectId featureBranchHead = new Git(repository).log().add(repository.resolve("squash")).setMaxCount(1).call().iterator().next().getId();

    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("squash");
    request.setTargetBranch("master");
    request.setMergeStrategy(MergeStrategy.FAST_FORWARD_IF_POSSIBLE);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    assertThat(mergeAuthor.getName()).isEqualTo("Philip J Fry");
    assertThat(mergeCommit.getId()).isEqualTo(featureBranchHead);
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

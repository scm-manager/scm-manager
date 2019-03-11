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
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Person;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.user.User;

import java.io.IOException;

import static com.google.inject.util.Providers.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitMergeCommandTest extends AbstractGitCommandTestBase {

  private static final String REALM = "AdminRealm";

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private ScmTransportProtocol scmTransportProtocol;

  @Before
  public void bindScmProtocol() {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    RepositoryManager repositoryManager = mock(RepositoryManager.class);
    HookEventFacade hookEventFacade = new HookEventFacade(of(repositoryManager), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    scmTransportProtocol = new ScmTransportProtocol(of(hookEventFacade), of(gitRepositoryHandler));

    Transport.register(scmTransportProtocol);

    when(gitRepositoryHandler.getRepositoryId(any())).thenReturn("1");
    when(repositoryManager.get("1")).thenReturn(new sonia.scm.repository.Repository());
  }

  @After
  public void unregisterScmProtocol() {
    Transport.unregister(scmTransportProtocol);
  }

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
    return new GitMergeCommand(createContext(), repository, new SimpleGitWorkdirFactory());
  }
}

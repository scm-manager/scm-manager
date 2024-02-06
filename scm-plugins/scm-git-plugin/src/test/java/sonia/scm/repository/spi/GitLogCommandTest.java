/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.spi;

import com.google.common.io.Files;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitLogCommand}.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GitLogCommandTest extends AbstractGitCommandTestBase
{
  @Mock
  LogCommandRequest request;

  /**
   * Tests log command with the usage of a default branch.
   */
  @Test
  public void testGetDefaultBranch() {
    // without default branch, the repository head should be used
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", result.getChangesets().get(0).getId());
    assertEquals("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1", result.getChangesets().get(1).getId());
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", result.getChangesets().get(2).getId());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getChangesets().get(3).getId());
    assertEquals("master", result.getBranchName());
    assertTrue(result.getChangesets().stream().allMatch(r -> r.getBranches().isEmpty()));

    // set default branch and fetch again
    createContext().setConfig(new GitRepositoryConfig("test-branch"));

    result = createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals("test-branch", result.getBranchName());
    assertEquals(3, result.getTotal());
    assertEquals("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", result.getChangesets().get(0).getId());
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", result.getChangesets().get(1).getId());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getChangesets().get(2).getId());
    assertTrue(result.getChangesets().stream().allMatch(r -> r.getBranches().isEmpty()));
  }

  @Test
  public void shouldNotCloseRepositoryForChangesetCollection() throws IOException {
    final GitLogCommand logCommand = createCommandWithContextSpy();
    final Repository repository = Mockito.spy(logCommand.context.open());
    logCommand.context.setGitRepository(repository);
    logCommand.getChangesets(new LogCommandRequest());

    verify(repository, never()).close();
    verify(logCommand.context, times(2)).open();
  }

  @Test
  public void shouldNotCloseRepositoryForSingleChangeset() throws IOException {
    final GitLogCommand logCommand = createCommandWithContextSpy();
    final Repository repository = Mockito.spy(logCommand.context.open());
    logCommand.context.setGitRepository(repository);
    logCommand.getChangeset("435df2f061add3589cb3", null);

    verify(repository, never()).close();
    verify(logCommand.context, times(2)).open();
  }

  @Test
  public void testGetAll()
  {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals(4, result.getChangesets().size());
  }

  @Test
  public void testGetAllByPath()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", result.getChangesets().get(0).getId());
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", result.getChangesets().get(1).getId());
  }

  @Test
  public void testGetAllWithLimit()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("fcd0ef1831e4002ac43ea539f4094334c79ea9ec", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1", c2.getId());
  }

  @Test
  public void testGetAllWithPaging()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(4, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);

    assertNotNull(c1);
    assertEquals("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1", c1.getId());

    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c2);
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", c2.getId());
  }

  @Test
  public void testGetCommit()
  {
    GitLogCommand command = createCommand();
    Changeset c = command.getChangeset("435df2f061add3589cb3", null);

    assertNotNull(c);
    String revision = "435df2f061add3589cb326cc64be9b9c3897ceca";
    assertEquals(revision, c.getId());
    assertEquals("added a and b files", c.getDescription());
    checkDate(c.getDate());
    assertEquals("Douglas Adams", c.getAuthor().getName());
    assertEquals("douglas.adams@hitchhiker.com", c.getAuthor().getMail());
    assertEquals("added a and b files", c.getDescription());

    GitModificationsCommand gitModificationsCommand = new GitModificationsCommand(createContext());
    Modifications modifications = gitModificationsCommand.getModifications(revision);

    assertNotNull(modifications);
    assertTrue("modified list should be empty", modifications.getModified().isEmpty());
    assertTrue("removed list should be empty", modifications.getRemoved().isEmpty());
    assertFalse("added list should not be empty", modifications.getAdded().isEmpty());
    assertEquals(2, modifications.getAdded().size());
    assertThat(modifications.getAdded())
      .extracting("path")
      .containsExactly("a.txt", "b.txt");
  }

  @Test
  public void commitShouldContainBranchIfLogCommandRequestHasBranch()
  {
    when(request.getBranch()).thenReturn("master");
    GitLogCommand command = createCommand();
    Changeset c = command.getChangeset("435df2f061add3589cb3", request);

    assertThat(c.getBranches()).containsOnly("master");
  }

  @Test
  public void shouldNotReturnCommitFromDifferentBranch() {
    when(request.getBranch()).thenReturn("master");
    Changeset changeset = createCommand().getChangeset("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", request);
    assertThat(changeset).isNull();
  }

  @Test
  public void testGetRange()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("592d797cd36432e59141");
    request.setEndChangeset("435df2f061add3589cb3");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(2, result.getTotal());
    assertEquals(2, result.getChangesets().size());

    Changeset c1 = result.getChangesets().get(0);
    Changeset c2 = result.getChangesets().get(1);

    assertNotNull(c1);
    assertEquals("592d797cd36432e591416e8b2b98154f4f163411", c1.getId());
    assertNotNull(c2);
    assertEquals("435df2f061add3589cb326cc64be9b9c3897ceca", c2.getId());
  }

  @Test
  public void testGetAncestor()
  {
    LogCommandRequest request = new LogCommandRequest();

    request.setBranch("test-branch");
    request.setAncestorChangeset("master");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertNotNull(result);
    assertEquals(1, result.getTotal());
    assertEquals(1, result.getChangesets().size());

    Changeset c = result.getChangesets().get(0);

    assertNotNull(c);
    assertEquals("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4", c.getId());
  }

  @Test
  public void shouldFindDefaultBranchFromHEAD() throws Exception {
    setRepositoryHeadReference("ref: refs/heads/test-branch");

    ChangesetPagingResult changesets = createCommand().getChangesets(new LogCommandRequest());

    assertEquals("test-branch", changesets.getBranchName());
  }

  @Test
  public void shouldFindMasterBranchWhenHEADisNoRef() throws Exception {
    setRepositoryHeadReference("592d797cd36432e591416e8b2b98154f4f163411");

    ChangesetPagingResult changesets = createCommand().getChangesets(new LogCommandRequest());

    assertEquals("main", changesets.getBranchName());
  }

  @Test
  public void shouldAppendCommitterAsContributor() {
    LogCommandRequest request = new LogCommandRequest();
    request.setStartChangeset("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
    request.setEndChangeset("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    ChangesetPagingResult changesets = createCommand().getChangesets(request);
    Changeset changeset = changesets.getChangesets().get(0);

    assertThat(changeset.getContributors()).hasSize(1);
    assertThat(changeset.getContributors().iterator().next().getPerson())
      .isEqualTo(new Person("Sebastian Sdorra", "s.sdorra@ostfalia.de"));
  }

  private void setRepositoryHeadReference(String s) throws IOException {
    Files.write(s, repositoryHeadReferenceFile(), defaultCharset());
  }

  private File repositoryHeadReferenceFile() {
    return new File(repositoryDirectory, "HEAD");
  }

  private GitLogCommand createCommand() {
    return new GitLogCommand(createContext(), GitTestHelper.createConverterFactory());
  }

  private GitLogCommand createCommandWithContextSpy() {
    return new GitLogCommand(Mockito.spy(createContext()), GitTestHelper.createConverterFactory());
  }
}

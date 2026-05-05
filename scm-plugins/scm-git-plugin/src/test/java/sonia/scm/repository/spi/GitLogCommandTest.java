/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import com.google.common.io.Files;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Added;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Person;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitLogCommand}.
 *
 */
@ExtendWith(MockitoExtension.class)
public class GitLogCommandTest extends AbstractGitCommandTestBase {

  @Mock
  LogCommandRequest request;

  /**
   * Tests log command with the usage of a default branch.
   */
  @Test
  public void testGetDefaultBranch() {
    // without default branch, the repository head should be used
    ChangesetPagingResult result = createCommand().getChangesets(new LogCommandRequest());

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
    assertThat(result.getBranchName()).isEqualTo("main");
    assertThat(result.getChangesets().stream().allMatch(r -> r.getBranches().isEmpty())).isTrue();

    // set default branch and fetch again
    createContext().setConfig(new GitRepositoryConfig("test-branch"));

    result = createCommand().getChangesets(new LogCommandRequest());

    assertThat(result).isNotNull();
    assertThat(result.getBranchName()).isEqualTo("test-branch");
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.isHasMore()).isFalse();

    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
    assertThat(result.getChangesets().stream().allMatch(r -> r.getBranches().isEmpty())).isTrue();
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
  public void testGetAll() {
    ChangesetPagingResult result =
      createCommand().getChangesets(new LogCommandRequest());

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets()).hasSize(4);
  }

  @Test
  public void testGetAllByPath() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPath("a.txt");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(2);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void testGetAllWithLimit() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).isEmpty();
    assertThat(result.isHasMore()).isTrue();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1"
      ));
  }

  @Test
  public void testGetAllWithPaging() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).isEmpty();
    assertThat(result.isHasMore()).isTrue();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411"
      ));
  }

  @Test
  public void testGetCommit() {
    GitLogCommand command = createCommand();
    String revision = "435df2f061add3589cb326cc64be9b9c3897ceca";
    Changeset c = command.getChangeset("435df2f061add3589cb3", null);

    assertThat(c).isNotNull();
    assertThat(c.getId()).isEqualTo(revision);
    assertThat(c.getDescription()).isEqualTo("added a and b files");
    checkDate(c.getDate());
    assertThat(c.getAuthor().getName()).isEqualTo("Douglas Adams");
    assertThat(c.getAuthor().getMail()).isEqualTo("douglas.adams@hitchhiker.com");

    GitModificationsCommand gitModificationsCommand = new GitModificationsCommand(createContext());
    Modifications modifications = gitModificationsCommand.getModifications(revision);

    assertThat(modifications).isNotNull();
    assertThat(modifications.getModified()).isEmpty();
    assertThat(modifications.getRemoved()).isEmpty();
    assertThat(modifications.getAdded().stream().map(Added::getPath)).isEqualTo(List.of("a.txt", "b.txt"));
  }

  @Test
  public void commitShouldContainBranchIfLogCommandRequestHasBranch() {
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
  public void testGetRange() {
    LogCommandRequest request = new LogCommandRequest();

    request.setStartChangeset("592d797cd36432e59141");
    request.setEndChangeset("435df2f061add3589cb3");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(2);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void testGetAncestor() {
    LogCommandRequest request = new LogCommandRequest();

    request.setBranch("test-branch");
    request.setAncestorChangeset("master");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(1);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4"
      ));
  }

  @Test
  public void shouldFindMasterBranchWhenHEADisNoRef() throws Exception {
    setRepositoryHeadReference("592d797cd36432e591416e8b2b98154f4f163411");

    ChangesetPagingResult changesets = createCommand().getChangesets(new LogCommandRequest());

    assertThat(changesets.getBranchName()).isEqualTo("main");
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

  @Test
  public void shouldFindChangesetsContainingMessage() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPartOfMessage("FiLE");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldFindChangesetsByAuthorEmail() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("BEEblebrox@hitchhiker");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(1);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec"
      ));
  }

  @Test
  public void shouldFindChangesetsByAuthorName() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("phod bee");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(1);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec"
      ));
  }

  @Test
  public void shouldFindCommiterByName() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("sebastian SDORRA");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldFindCommiterByMail() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPerson("SDORRA@ostfalia");

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldFindChangesetsBeforeInstant() {
    LogCommandRequest request = new LogCommandRequest();

    //11.06.2012 11:57:00Z
    request.setBefore(Instant.ofEpochMilli(1339415820000L));

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(1);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldFindChangesetsAfterInstant() {
    LogCommandRequest request = new LogCommandRequest();

    //11.06.2012 11:57:00Z
    request.setAfter(Instant.ofEpochMilli(1339415820000L));

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411"
      ));
  }

  @Test
  public void shouldReturnTotalCountBecauseUserIsAtLastPageWithoutProvidingEndChangeset() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(3);
    request.setPagingLimit(3);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldReturnTotalCountBecauseUserIsAtLastPageWithProvidingEndChangeset() {
    LogCommandRequest request = new LogCommandRequest();

    request.setEndChangeset("592d797cd36432e591416e8b2b98154f4f163411");
    request.setPagingStart(1);
    request.setPagingLimit(3);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411"
      ));
  }

  @Test
  public void shouldReturnTotalCountBecauseUserIsAfterLastPageWithoutProvidingEndChangeset() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(4);
    request.setPagingLimit(3);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
      ));
  }

  @Test
  public void shouldReturnTotalCountBecauseUserIsAfterLastPageWithProvidingEndChangeset() {
    LogCommandRequest request = new LogCommandRequest();

    request.setEndChangeset("592d797cd36432e591416e8b2b98154f4f163411");
    request.setPagingStart(3);
    request.setPagingLimit(3);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(3);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
      ));
  }

  @Test
  public void shouldReturnWithoutTotalCountBecauseUserIsBeforeLastPage() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(0);
    request.setPagingLimit(3);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).isEmpty();
    assertThat(result.isHasMore()).isTrue();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411"
      ));
  }

  @Test
  public void shouldReturnLastPageBecauseStartIsNegative() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(-1);
    request.setPagingLimit(2);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldReturnLastPageWithNegativeStartAndIncompleteLastPage() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(-1);
    request.setPagingLimit(3);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldReturnLastPageWithHigherLimitThanTotal() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(-1);
    request.setPagingLimit(100);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }

  @Test
  public void shouldReturnLastPageWithEveryCommitBecauseLimitIsNegative() {
    LogCommandRequest request = new LogCommandRequest();

    request.setPagingStart(-1);
    request.setPagingLimit(-1);

    ChangesetPagingResult result = createCommand().getChangesets(request);

    assertThat(result).isNotNull();
    assertThat(result.getTotal()).hasValue(4);
    assertThat(result.isHasMore()).isFalse();
    assertThat(result.getChangesets().stream().map(Changeset::getId).toList())
      .isEqualTo(List.of(
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        "86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1",
        "592d797cd36432e591416e8b2b98154f4f163411",
        "435df2f061add3589cb326cc64be9b9c3897ceca"
      ));
  }
}

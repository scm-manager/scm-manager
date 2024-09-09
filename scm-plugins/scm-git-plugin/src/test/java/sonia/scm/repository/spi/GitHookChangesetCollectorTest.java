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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.web.CollectingPackParserListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.asList;
import static org.eclipse.jgit.lib.ObjectId.fromString;
import static org.eclipse.jgit.lib.ObjectId.zeroId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitHookChangesetCollectorTest extends AbstractGitCommandTestBase {

  private final ReceivePack rpack = mock(ReceivePack.class);
  private final Collection<ReceiveCommand> receiveCommands = new ArrayList<>();
  private final CollectingPackParserListener listener = mock(CollectingPackParserListener.class);

  private final GitChangesetConverterFactory converterFactory = mock(GitChangesetConverterFactory.class);
  private final GitChangesetConverter converter = mock(GitChangesetConverter.class);

  private GitHookChangesetCollector collector;

  @Before
  public void init() throws IOException {

    GitContext context = createContext();
    Repository repository = context.open();
    RevWalk revWalk = new RevWalk(repository);
    when(rpack.getRepository()).thenReturn(repository);
    when(rpack.getRevWalk()).thenReturn(revWalk);
    when(rpack.getPackParserListener()).thenReturn(listener);
    when(converterFactory.create(repository, revWalk)).thenReturn(converter);
    when(converter.createChangeset(any(), any(String[].class)))
      .thenAnswer(invocation -> new Changeset(invocation.getArgument(0, RevCommit.class).name(), null, null));
  }

  @Test
  public void shouldCreateEmptyCollectionsWithoutChanges() {
    collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, rpack);

    assertThat(collector.getAddedChangesets()).isEmpty();
    assertThat(collector.getRemovedChangesets()).isEmpty();
  }

  @Test
  public void shouldFindAddedChangesetsFromNewBranch() {
    receiveCommands.add(
      new ReceiveCommand(
        zeroId(),
        fromString("91b99de908fcd04772798a31c308a64aea1a5523"),
        "refs/heads/mergeable")
    );
    mockNewCommits(
      "91b99de908fcd04772798a31c308a64aea1a5523",
      "3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4",
      "592d797cd36432e591416e8b2b98154f4f163411");

    collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, rpack);

    assertThat(collector.getAddedChangesets())
      .extracting("id")
      .contains(
        "91b99de908fcd04772798a31c308a64aea1a5523",
        "592d797cd36432e591416e8b2b98154f4f163411");
    assertThat(collector.getRemovedChangesets()).isEmpty();
  }

  @Test
  public void shouldFindAddedChangesetsFromNewBranchesOnce() throws IOException, GitAPIException {
    new Git(createContext().open()).branchCreate().setStartPoint("mergeable").setName("second").call();
    receiveCommands.add(
      new ReceiveCommand(
        zeroId(),
        fromString("91b99de908fcd04772798a31c308a64aea1a5523"),
        "refs/heads/mergeable")
    );
    receiveCommands.add(
      new ReceiveCommand(
        zeroId(),
        fromString("91b99de908fcd04772798a31c308a64aea1a5523"),
        "refs/heads/second")
    );
    mockNewCommits(
      "91b99de908fcd04772798a31c308a64aea1a5523",
      "3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4",
      "592d797cd36432e591416e8b2b98154f4f163411");

    collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, rpack);

    assertThat(collector.getAddedChangesets())
      .extracting("id")
      .hasSize(2)
      .contains(
        "91b99de908fcd04772798a31c308a64aea1a5523",
        "592d797cd36432e591416e8b2b98154f4f163411");
    assertThat(collector.getRemovedChangesets()).isEmpty();
  }

  @Test
  public void shouldFindAddedChangesetsFromChangedBranchWithoutIteratingOldCommits() {
    receiveCommands.add(
      new ReceiveCommand(
        fromString("592d797cd36432e591416e8b2b98154f4f163411"),
        fromString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4"),
        "refs/heads/test-branch")
    );
    mockNewCommits("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, rpack);

    assertThat(collector.getAddedChangesets())
      .extracting("id")
      .contains("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertThat(collector.getRemovedChangesets()).isEmpty();

    verify(listener, never()).isNew(argThat(argument -> argument.name().equals("592d797cd36432e591416e8b2b98154f4f163411")));
  }

  @Test
  public void shouldFindRemovedChangesetsFromDeletedBranch() throws IOException, GitAPIException {
    new Git(createContext().open()).branchDelete().setBranchNames("test-branch").setForce(true).call();
    receiveCommands.add(
      new ReceiveCommand(
        fromString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4"),
        zeroId(),
        "refs/heads/test-branch",
        ReceiveCommand.Type.DELETE)
    );
    mockNewCommits("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, rpack);

    assertThat(collector.getAddedChangesets()).isEmpty();
    assertThat(collector.getRemovedChangesets())
      .extracting("id")
      .contains("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    verify(listener, never()).isNew(argThat(argument -> argument.name().equals("592d797cd36432e591416e8b2b98154f4f163411")));
  }

  @Test
  public void shouldFindRemovedAndAddedChangesetsFromNonFastForwardChanged() throws IOException, GitAPIException {
    new Git(createContext().open()).branchDelete().setBranchNames("test-branch").setForce(true).call();
    receiveCommands.add(
      new ReceiveCommand(
        fromString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4"),
        fromString("91b99de908fcd04772798a31c308a64aea1a5523"),
        "refs/heads/test-branch",
        ReceiveCommand.Type.UPDATE_NONFASTFORWARD)
    );
    mockNewCommits("91b99de908fcd04772798a31c308a64aea1a5523");

    collector = GitHookChangesetCollector.collectChangesets(converterFactory, receiveCommands, rpack);

    assertThat(collector.getAddedChangesets())
      .extracting("id")
      .contains("91b99de908fcd04772798a31c308a64aea1a5523");
    assertThat(collector.getRemovedChangesets())
      .extracting("id")
      .contains("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
  }

  private void mockNewCommits(String... objectIds) {
    when(listener.isNew(any()))
      .thenAnswer(invocation -> asList(objectIds).contains(invocation.getArgument(0, RevCommit.class).name()));
  }
}

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

  private ReceivePack rpack;
  private Collection<ReceiveCommand> receiveCommands = new ArrayList<>();

  private CollectingPackParserListener listener;
  private GitHookChangesetCollector collector;

  @Before
  public void init() throws IOException {
    GitChangesetConverterFactory converterFactory = mock(GitChangesetConverterFactory.class);
    GitChangesetConverter converter = mock(GitChangesetConverter.class);
    rpack = mock(ReceivePack.class);
    listener = mock(CollectingPackParserListener.class);

    GitContext context = createContext();
    Repository repository = context.open();
    RevWalk revWalk = new RevWalk(repository);
    when(rpack.getRepository()).thenReturn(repository);
    when(rpack.getRevWalk()).thenReturn(revWalk);
    when(rpack.getPackParserListener()).thenReturn(listener);
    when(converterFactory.create(repository, revWalk)).thenReturn(converter);
    when(converter.createChangeset(any(), (String[]) any()))
      .thenAnswer(invocation -> new Changeset(invocation.getArgument(0, RevCommit.class).name(), null, null));

    collector = new GitHookChangesetCollector(converterFactory, rpack, receiveCommands);
  }

  @Test
  public void shouldCreateEmptyCollectionsWithoutChanges() {
    collector.collectChangesets();

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
    mockExistingCommits(
      "91b99de908fcd04772798a31c308a64aea1a5523",
      "3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4",
      "592d797cd36432e591416e8b2b98154f4f163411");

    collector.collectChangesets();

    assertThat(collector.getAddedChangesets())
      .extracting("id")
      .contains(
        "91b99de908fcd04772798a31c308a64aea1a5523",
        "592d797cd36432e591416e8b2b98154f4f163411");
    assertThat(collector.getRemovedChangesets()).isEmpty();
  }

  private void mockExistingCommits(String... objectIds) {
    when(listener.isNew(any()))
      .thenAnswer(invocation -> asList(objectIds).contains(invocation.getArgument(0, RevCommit.class).name()));
  }

  @Test
  public void shouldFindAddedChangesetsFromChangedBranchWithoutIteratingOldCommits() {
    receiveCommands.add(
      new ReceiveCommand(
        fromString("592d797cd36432e591416e8b2b98154f4f163411"),
        fromString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4"),
        "refs/heads/test-branch")
    );
    mockExistingCommits("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    collector.collectChangesets();

    assertThat(collector.getAddedChangesets())
      .extracting("id")
      .contains("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertThat(collector.getRemovedChangesets()).isEmpty();

    verify(listener, never()).isNew(argThat(argument -> argument.name().equals("592d797cd36432e591416e8b2b98154f4f163411")));
  }

  @Test
  public void shouldFindRemovedChangesetsFromChangedBranchWithoutIteratingOldCommits() throws IOException, GitAPIException {
    new Git(createContext().open()).branchDelete().setBranchNames("test-branch").setForce(true).call();
    receiveCommands.add(
      new ReceiveCommand(
        fromString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4"),
        zeroId(),
        "refs/heads/test-branch",
        ReceiveCommand.Type.DELETE)
    );
    mockExistingCommits("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    collector.collectChangesets();

    assertThat(collector.getAddedChangesets()).isEmpty();
    assertThat(collector.getRemovedChangesets())
      .extracting("id")
      .contains("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    verify(listener, never()).isNew(argThat(argument -> argument.name().equals("592d797cd36432e591416e8b2b98154f4f163411")));
  }
}

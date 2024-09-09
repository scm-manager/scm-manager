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

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookModificationsProvider;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitBranchCommandTest extends AbstractGitCommandTestBase {

  @Mock
  private PreProcessorUtil preProcessorUtil;
  private HookContextFactory hookContextFactory;
  @Mock
  private ScmEventBus eventBus;
  @Mock
  private GitChangesetConverterFactory converterFactory;

  @Before
  public void mockConverterFactory() {
    GitChangesetConverter gitChangesetConverter = mock(GitChangesetConverter.class);
    when(converterFactory.create(any(), any()))
      .thenReturn(gitChangesetConverter);
    when(gitChangesetConverter.createChangeset(any(), any(String[].class)))
      .thenAnswer(invocation -> {
        RevCommit revCommit = invocation.getArgument(0, RevCommit.class);
        Changeset changeset = new Changeset(revCommit.name(), null, null);
        return changeset;
      });
    hookContextFactory = new HookContextFactory(preProcessorUtil);
  }

  @Test
  public void shouldCreateBranchWithDefinedSourceBranch() throws IOException {
    GitContext context = createContext();

    Branch source = findBranch(context, "mergeable");

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch(source.getName());
    branchRequest.setNewBranch("new_branch");

    createCommand().branch(branchRequest);

    Branch newBranch = findBranch(context, "new_branch");
    assertThat(newBranch.getRevision()).isEqualTo(source.getRevision());
  }

  private Branch findBranch(GitContext context, String name) throws IOException {
    List<Branch> branches = readBranches(context);
    return branches.stream().filter(b -> name.equals(b.getName())).findFirst().get();
  }

  @Test
  public void shouldCreateBranch() throws IOException {
    GitContext context = createContext();

    assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setNewBranch("new_branch");

    createCommand().branch(branchRequest);

    assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  @Test
  public void shouldDeleteBranch() throws IOException {
    GitContext context = createContext();
    String branchToBeDeleted = "squash";
    createCommand().deleteOrClose(branchToBeDeleted);
    assertThat(readBranches(context)).filteredOn(b -> b.getName().equals(branchToBeDeleted)).isEmpty();
  }

  @Test
  public void shouldThrowExceptionWhenDeletingDefaultBranch() {
    String branchToBeDeleted = "master";
    GitBranchCommand command = createCommand();
    assertThrows(CannotDeleteDefaultBranchException.class, () -> command.deleteOrClose(branchToBeDeleted));
  }

  @Test
  public void shouldThrowViolationExceptionForInvalidBranchName() {
    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setNewBranch("invalid..name");

    GitBranchCommand command = createCommand();
    assertThrows(ScmConstraintViolationException.class, () -> command.branch(branchRequest));
  }

  private GitBranchCommand createCommand() {
    return new GitBranchCommand(createContext(), hookContextFactory, eventBus, converterFactory);
  }

  private List<Branch> readBranches(GitContext context) throws IOException {
    return new GitBranchesCommand(context).getBranches();
  }

  @Test
  public void shouldPostCreateEvents() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    doNothing().when(eventBus).post(captor.capture());

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch("mergeable");
    branchRequest.setNewBranch("new_branch");

    createCommand().branch(branchRequest);

    List<Object> events = captor.getAllValues();
    assertThat(events.get(0)).isInstanceOf(PreReceiveRepositoryHookEvent.class);
    assertThat(events.get(1)).isInstanceOf(PostReceiveRepositoryHookEvent.class);

    PreReceiveRepositoryHookEvent event = (PreReceiveRepositoryHookEvent) events.get(0);
    assertThat(event.getContext().getBranchProvider().getCreatedOrModified()).containsExactly("new_branch");
    assertThat(event.getContext().getBranchProvider().getDeletedOrClosed()).isEmpty();
    HookModificationsProvider modificationsProvider = event.getContext().getModificationsProvider();
    assertThat(modificationsProvider.getModifications("new_branch"))
      .extracting("modifications")
      .asList()
      .hasSize(4);
  }

  @Test
  public void shouldPostDeleteEvents() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    doNothing().when(eventBus).post(captor.capture());

    createCommand().deleteOrClose("squash");

    List<Object> events = captor.getAllValues();
    assertThat(events.get(0)).isInstanceOf(PreReceiveRepositoryHookEvent.class);
    assertThat(events.get(1)).isInstanceOf(PostReceiveRepositoryHookEvent.class);

    PreReceiveRepositoryHookEvent event = (PreReceiveRepositoryHookEvent) events.get(0);
    assertThat(event.getContext().getBranchProvider().getDeletedOrClosed()).containsExactly("squash");
    assertThat(event.getContext().getBranchProvider().getCreatedOrModified()).isEmpty();

    HookChangesetBuilder changesetProvider = event.getContext().getChangesetProvider();
    assertThat(changesetProvider.getChangesets()).isEmpty();
    assertThat(changesetProvider.getRemovedChangesets())
      .extracting("id")
      .containsExactly(
        "35597e9e98fe53167266583848bfef985c2adb27",
        "f360a8738e4a29333786c5817f97a2c912814536",
        "d1dfecbfd5b4a2f77fe40e1bde29e640f7f944be"
      );
    HookModificationsProvider modificationsProvider = event.getContext().getModificationsProvider();
    assertThat(modificationsProvider.getModifications("squash"))
      .extracting("modifications")
      .asList()
      .hasSize(8);
  }
}

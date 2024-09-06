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

package sonia.scm.repository.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.BranchCreatedEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.BranchCommand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchCommandBuilderTest {

  @Mock
  private BranchCommand command;

  @Mock
  private ScmEventBus eventBus;

  private final Repository repository = new Repository("42", "git", "spaceships", "heart-of-gold");

  private final String branchName = "feature/infinite_improbability_drive";
  private final Branch branch = Branch.normalBranch(branchName, "42");

  @Nested
  class Creation {

    @BeforeEach
    void configureMocks() {
      when(command.branch(any())).thenReturn(branch);
    }

    @Test
    void shouldDelegateCreationToCommand() {
      BranchCommandBuilder builder = new BranchCommandBuilder(repository, command, eventBus);
      Branch returnedBranch = builder.branch(branchName);
      assertThat(branch).isSameAs(returnedBranch);
    }

    @Test
    void shouldSendBranchCreatedEvent() {
      BranchCommandBuilder builder = new BranchCommandBuilder(repository, command, eventBus);
      builder.branch(branchName);

      ArgumentCaptor<BranchCreatedEvent> captor = ArgumentCaptor.forClass(BranchCreatedEvent.class);
      verify(eventBus).post(captor.capture());
      BranchCreatedEvent event = captor.getValue();
      assertThat(event.getBranchName()).isEqualTo("feature/infinite_improbability_drive");
    }

    @Test
    void shouldNotSendEventWithoutRepository() {
      BranchCommandBuilder builder = new BranchCommandBuilder(null, command, eventBus);
      builder.branch(branchName);

      verify(eventBus, never()).post(any());
    }

  }

  @Nested
  class Deletion {

    @Test
    void shouldDelegateDeletionToCommand() {
      BranchCommandBuilder builder = new BranchCommandBuilder(repository, command, eventBus);
      builder.delete(branchName);
      verify(command).deleteOrClose(branchName);
    }
  }

}

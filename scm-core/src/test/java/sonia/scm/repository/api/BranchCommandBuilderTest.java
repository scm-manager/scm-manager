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
